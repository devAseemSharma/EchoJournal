package com.androidace.echojournal.ui.newentry

import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androidace.echojournal.audio.AudioPlaybackManager
import com.androidace.echojournal.data.AudioRepository
import com.androidace.echojournal.db.RecordedAudio
import com.androidace.echojournal.db.Topic
import com.androidace.echojournal.repository.NewEntryRepository
import com.androidace.echojournal.repository.TopicRepository
import com.androidace.echojournal.ui.common.UIStateHandlerImpl
import com.androidace.echojournal.ui.common.UiStateHandler
import com.androidace.echojournal.ui.mood.model.Mood
import com.androidace.echojournal.ui.newentry.model.NewEntryScreenState
import com.androidace.echojournal.util.formatMillis
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NewEntryViewModel @Inject constructor(
    private val audioRepository: AudioRepository,
    private val playbackManager: AudioPlaybackManager,
    private val topicRepository: TopicRepository,
    private val newEntryRepository: NewEntryRepository,
    val uiStateHandlerImpl: UIStateHandlerImpl,
) : ViewModel(), UiStateHandler by uiStateHandlerImpl {

    private var _newScreenState = MutableStateFlow(NewEntryScreenState())
    val newScreenState = _newScreenState.asStateFlow()

    private var _isFormValidated = MutableStateFlow(false)
    val isFormValidated = _isFormValidated.asStateFlow()

    private var _selectedTopic = MutableStateFlow<SnapshotStateList<Topic>>(
        value = mutableStateListOf<Topic>()
    )
    val selectedTopic = _selectedTopic.asStateFlow()

    private var currentLocalAudio: RecordedAudio? = null

    init {
        // Load all topics on initialization
        viewModelScope.launch {
            _newScreenState.value =
                _newScreenState.value.copy(listTopics = topicRepository.getAllTopics())
        }
    }

    fun createTopic(name: String, onTopicAdded: (Topic) -> Unit) {
        viewModelScope.launch {
            val topic = topicRepository.insertTopicAndGet(Topic(name = name))
            onTopicAdded.invoke(topic)
            _newScreenState.value =
                _newScreenState.value.copy(listTopics = topicRepository.getAllTopics())
        }
    }

    fun removeTopic(topic: Topic) {
        viewModelScope.launch {
            topicRepository.deleteTopic(topic)
            _newScreenState.value =
                _newScreenState.value.copy(listTopics = topicRepository.getAllTopics())
        }
    }

    override fun onCleared() {
        super.onCleared()
        playbackManager.releaseController()
    }

    fun onTitleValueChange(text: String) {
        _newScreenState.value = _newScreenState.value.copy(
            newEntryTitle = text
        )
        validate()
    }

    fun updateProgress(progress: Float) {
        val position = currentLocalAudio?.timestamp?.times(progress)?.toLong() ?: 0L
        playbackManager.seekTo(position)
        _newScreenState.value = _newScreenState.value.copy(
            audioWaveFormState = _newScreenState.value.audioWaveFormState.copy(progress = progress)
        )
    }

    fun updatePlaybackState() {
        viewModelScope.launch {
            when {
                _newScreenState.value.audioWaveFormState.isPlaying -> {
                    _newScreenState.value = _newScreenState.value.copy(
                        audioWaveFormState = _newScreenState.value.audioWaveFormState.copy(isPlaying = false)
                    )
                    playbackManager.pause()
                }

                else -> {
                    currentLocalAudio?.let {
                        _newScreenState.value = _newScreenState.value.copy(
                            audioWaveFormState = _newScreenState.value.audioWaveFormState.copy(
                                isPlaying = true
                            )
                        )
                        playbackManager.play()
                    }
                }
            }
        }
    }


    fun loadAudio(contentId: Int) {
        viewModelScope.launch {
            try {
                currentLocalAudio = audioRepository.loadAudioByContentId(contentId) ?: return@launch
                currentLocalAudio?.let {
                    playbackManager.initializePlayer(it)
                }
                launch { currentLocalAudio?.let { loadAudioAmplitudes(it) } }
                launch { observePlaybackEvents() }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun onSaveEntry(onSuccess:()->Unit) {
        uiStateHandlerImpl.showLoader()
        viewModelScope.launch {
            val insertedId = newEntryRepository.insertNewEntryWithTopic(
                title = _newScreenState.value.newEntryTitle,
                description = "Sample Desc",
                mood = _newScreenState.value.selectedMood ?: Mood.NEUTRAL,
                recordedAudio = currentLocalAudio!!,
                selectedTopics = _selectedTopic.value
            )
            if (insertedId > 0L) {
                uiStateHandlerImpl.hideLoader()
               onSuccess.invoke()
            }
        }
    }

    fun onMoodSelected(mood: Mood) {
        _newScreenState.value = _newScreenState.value.copy(
            selectedMood = mood
        )
        validate()
    }

    fun validate() {
        _isFormValidated.value = _newScreenState.value.newEntryTitle.isNotEmpty()
                && _newScreenState.value.selectedMood != null
                && _selectedTopic.value.isNotEmpty()
    }


    private suspend fun loadAudioAmplitudes(localAudio: RecordedAudio) {
        try {
            val amplitudes = audioRepository.loadAudioAmplitudes(localAudio)
            _newScreenState.value = _newScreenState.value.copy(
                audioWaveFormState = _newScreenState.value.audioWaveFormState.copy(
                    amplitudes = amplitudes,
                    totalDuration = formatMillis(localAudio.timestamp)
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun observePlaybackEvents() {
        playbackManager.progressFlow.collectLatest {
            when (it) {
                is AudioPlaybackManager.Event.PositionChanged -> updatePlaybackProgress(it.position)
                is AudioPlaybackManager.Event.PlayingChanged -> updatePlayingState(it.isPlaying)
                AudioPlaybackManager.Event.OnPlayingComplete -> resetPlayer()
            }
        }
    }

    private fun updatePlaybackProgress(position: Long) {

        val audio = currentLocalAudio ?: return
        val progress = position.toFloat() / audio.timestamp
        _newScreenState.value = _newScreenState.value.copy(
            audioWaveFormState = _newScreenState.value.audioWaveFormState.copy(
                progress = progress,
                seekDuration = formatMillis(position)
            )
        )
    }

    private fun updatePlayingState(isPlaying: Boolean) {
        _newScreenState.value = _newScreenState.value.copy(
            audioWaveFormState = _newScreenState.value.audioWaveFormState.copy(isPlaying = isPlaying)
        )
    }

    private fun resetPlayer() {
        _newScreenState.value = _newScreenState.value.copy(
            audioWaveFormState = _newScreenState.value.audioWaveFormState.copy(
                progress = 0f,
                seekDuration = "00:00",
                isPlaying = false
            )
        )
    }


}