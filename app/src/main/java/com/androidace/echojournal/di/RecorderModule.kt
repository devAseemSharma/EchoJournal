package com.androidace.echojournal.di

import com.androidace.echojournal.audio.recorder.MediaRecorderVoiceRecorder
import com.androidace.echojournal.audio.recorder.VoiceRecorder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RecorderModule {

    @Singleton
    @Provides
    fun provideVoiceRecorder(): VoiceRecorder {
        return MediaRecorderVoiceRecorder()
    }
}