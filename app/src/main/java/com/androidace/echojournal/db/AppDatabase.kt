package com.androidace.echojournal.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [RecordedAudio::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recordedAudioDao(): RecordedAudioDao
}