package com.androidace.echojournal.audio.recorder

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.io.IOException

class MediaRecorderVoiceRecorder(
    val context: Context
) : VoiceRecorder {

    private var mediaRecorder: MediaRecorder? = null
    private var currentState: State = State.IDLE

    // Time-tracking variables
    private var recordStartTime = 0L
    private var pauseStartTime = 0L
    private var accumulatedPauseDuration = 0L

    private fun createRecorder(): MediaRecorder {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else MediaRecorder()
    }

    override fun startRecording(outputPath: String) {
        if (currentState == State.RECORDING || currentState == State.PAUSED) return

        mediaRecorder = createRecorder().apply {
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

                // Initialize timing
                recordStartTime = System.currentTimeMillis()
                pauseStartTime = 0L
                accumulatedPauseDuration = 0L
            } catch (e: IOException) {
                Log.e("MediaRecorder", "startRecording: ", e)
            }
        }
    }

    override fun pauseRecording() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (currentState == State.RECORDING) {
                mediaRecorder?.pause()
                currentState = State.PAUSED
                // Mark when we started pausing
                pauseStartTime = System.currentTimeMillis()
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
                // Increase the accumulated pause time
                val pausedMillis = System.currentTimeMillis() - pauseStartTime
                accumulatedPauseDuration += pausedMillis
                pauseStartTime = 0L
            }
        } else {
            // Similarly, for older devices, you'd need a custom approach.
        }
    }

    override fun stopRecording() {
        mediaRecorder?.stop()
        mediaRecorder?.reset()
        mediaRecorder = null
        currentState = State.IDLE
        // Reset timing
        recordStartTime = 0L
        pauseStartTime = 0L
        accumulatedPauseDuration = 0L
    }

    /**
     * Returns elapsed time in milliseconds since recording started,
     * excluding paused durations.
     */
    override fun getElapsedTimeMs(): Long {
        if (recordStartTime == 0L) return 0L
        return when (currentState) {
            State.PAUSED -> {
                // up to pauseStartTime minus any previously accumulated pause
                (pauseStartTime - recordStartTime) - accumulatedPauseDuration
            }
            State.RECORDING -> {
                // current time - start time - total paused
                (System.currentTimeMillis() - recordStartTime) - accumulatedPauseDuration
            }
            else -> {
                // IDLE
                0L
            }
        }
    }

    override fun isRecording(): Boolean = currentState == State.RECORDING
    override fun isPaused(): Boolean = currentState == State.PAUSED

    private enum class State {
        IDLE, RECORDING, PAUSED
    }
}