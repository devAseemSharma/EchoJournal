package com.androidace.echojournal.audio

import android.content.ComponentName
import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.androidace.echojournal.db.RecordedAudio
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.UUID
import javax.inject.Inject

class AudioPlaybackManager @Inject constructor(
    @ApplicationContext private val context: Context
) : MediaPlayer.OnPreparedListener {
    companion object {
        private const val PLAYER_POSITION_UPDATE_TIME = 500L
    }

    private var lastEmittedPosition: Int = 0
    private var mediaPlayer: MediaPlayer? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    // SharedFlow for position updates
    // replay=1 ensures the latest emitted value is cached for new collectors
    private val _progressFlow = MutableSharedFlow<Event>(replay = 1)
    val progressFlow: SharedFlow<Event> = _progressFlow.asSharedFlow()

    fun clearAudio() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        lastEmittedPosition = 0
    }

    fun initializePlayer(audio: RecordedAudio) {
        MediaPlayer.create(context, Uri.parse(audio.fileUri)).apply {
            mediaPlayer = this
            setOnPreparedListener(this@AudioPlaybackManager)
            setOnCompletionListener {
                sendEvent(Event.OnPlayingComplete)
                lastEmittedPosition = 0
                seekTo(lastEmittedPosition)
            }
        }
    }

    fun play() {
        mediaPlayer?.start()
        if (lastEmittedPosition > 0) {
            mediaPlayer?.seekTo(lastEmittedPosition)
        }
        scope.launch {
            startObservingProgress(200)
        }
    }

    fun getMediaPlayer() = mediaPlayer

    fun pause() {
        mediaPlayer?.pause()
        lastEmittedPosition = mediaPlayer?.currentPosition ?: 0
    }

    fun seekTo(position: Long) {
        mediaPlayer?.seekTo(position.toInt())
    }

    fun releaseController() {
        clearAudio()
    }

    /**
     * Start polling the current position if the controller is active and playing.
     * Emit updates into the SharedFlow. Typically called from a coroutine in your ViewModel.
     */
    private suspend fun startObservingProgress(pollIntervalMs: Long = 200) {
        val controller = mediaPlayer ?: return

        while (controller.isPlaying) {
            val position = controller.currentPosition
            _progressFlow.emit(Event.PositionChanged(position.toLong()))
            delay(pollIntervalMs)
        }
    }

    private fun sendEvent(event: Event) {
        runBlocking {
            _progressFlow.emit(event)
        }
    }

    sealed interface Event {
        data class PositionChanged(val position: Long) : Event
        data class PlayingChanged(val isPlaying: Boolean) : Event
        data object OnPlayingComplete : Event
    }

    override fun onPrepared(mediaPlayer: MediaPlayer?) {

    }
}