package com.androidace.echojournal.ui.recording

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androidace.echojournal.audio.recorder.VoiceRecorder
import com.androidace.echojournal.db.RecordedAudio
import com.androidace.echojournal.repository.RecordingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class RecordingViewModel @Inject constructor(
    private val voiceRecorder: VoiceRecorder,
    private val repository: RecordingRepository,
    @ApplicationContext val context: Context,
): ViewModel() {
    // UI states
    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> get() = _isRecording

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> get() = _isPaused

    // The file that we are currently recording to
    private var currentFile: File? = null

    fun startRecording() {
        viewModelScope.launch {
            // Create an internal file
            currentFile = createInternalFile(context = context)
            currentFile?.let { file ->
                voiceRecorder.startRecording(file.absolutePath)
                _isRecording.value = true
                _isPaused.value = false
            }
        }
    }

    fun pauseRecording() {
        viewModelScope.launch {
            voiceRecorder.pauseRecording()
            _isPaused.value = voiceRecorder.isPaused()
        }
    }

    fun resumeRecording() {
        viewModelScope.launch {
            voiceRecorder.resumeRecording()
            _isPaused.value = voiceRecorder.isPaused()
        }
    }

    fun stopRecording() {
        viewModelScope.launch {
            voiceRecorder.stopRecording()
            _isRecording.value = false
            _isPaused.value = false

            // Once stopped, insert a record in DB
            currentFile?.let { file ->
                val recordedAudio = RecordedAudio(
                    fileUri = file.absolutePath, // or a URI string if you convert it
                    timestamp = System.currentTimeMillis()
                )
                repository.insertRecording(recordedAudio)
            }
            currentFile = null
        }
    }

    private fun createInternalFile(context: Context): File {
        val internalDir = context.filesDir
        return File.createTempFile("audio_", ".m4a", internalDir)
    }
}