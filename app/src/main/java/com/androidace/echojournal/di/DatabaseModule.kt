package com.androidace.echojournal.di

import android.content.Context
import androidx.room.Room
import com.androidace.echojournal.db.AppDatabase
import com.androidace.echojournal.db.dao.RecordedAudioDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideRecordedAudioDao(db: AppDatabase): RecordedAudioDao {
        return db.recordedAudioDao()
    }
}