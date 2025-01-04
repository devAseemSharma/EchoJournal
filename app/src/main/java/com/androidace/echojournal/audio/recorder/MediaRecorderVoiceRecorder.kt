package com.androidace.echojournal.audio.recorder

import android.media.MediaRecorder
import android.os.Build
import java.io.IOException

class MediaRecorderVoiceRecorder : VoiceRecorder {

    private var mediaRecorder: MediaRecorder? = null
    private var currentState: State = State.IDLE

    override fun startRecording(outputPath: String) {
        if (currentState == State.RECORDING || currentState == State.PAUSED) return

        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)

            // High quality settings:
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioEncodingBitRate(128000)  // example for high quality
            setAudioSamplingRate(44100)      // example for high quality
            setOutputFile(outputPath)

            try {
                prepare()
                start()
                currentState = State.RECORDING
            } catch (e: IOException) {
                //Timber.e("MediaRecorder", "startRecording: ", e)
            }
        }
    }

    override fun pauseRecording() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (currentState == State.RECORDING) {
                mediaRecorder?.pause()
                currentState = State.PAUSED
            }
        } else {
            // On older devices, you can't natively pause MediaRecorder.
            // You would need to stop and create a new file or handle differently.
            stopRecording()
        }
    }

    override fun resumeRecording() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (currentState == State.PAUSED) {
                mediaRecorder?.resume()
                currentState = State.RECORDING
            }
        } else {
            // Similarly, for older devices, you'd need a custom approach.
        }
    }

    override fun stopRecording() {
        if (currentState == State.RECORDING || currentState == State.PAUSED) {
            mediaRecorder?.stop()
        }
        mediaRecorder?.release()
        mediaRecorder = null
        currentState = State.IDLE
    }

    override fun isRecording(): Boolean = currentState == State.RECORDING
    override fun isPaused(): Boolean = currentState == State.PAUSED

    private enum class State {
        IDLE, RECORDING, PAUSED
    }
}