package com.androidace.echojournal.di

import android.content.Context
import com.androidace.echojournal.audio.recorder.MediaRecorderVoiceRecorder
import com.androidace.echojournal.audio.recorder.VoiceRecorder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RecorderModule {

    @Singleton
    @Provides
    fun provideVoiceRecorder(@ApplicationContext context: Context): VoiceRecorder {
        return MediaRecorderVoiceRecorder(context)
    }
}