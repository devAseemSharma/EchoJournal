package com.androidace.echojournal.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.androidace.echojournal.db.dao.NewEntryDao
import com.androidace.echojournal.db.dao.RecordedAudioDao
import com.androidace.echojournal.db.dao.TopicDao

@Database(
    entities = [RecordedAudio::class,
        NewEntryEntity::class,
        Topic::class,
        NewEntryTopicCrossRef::class], version = 1, exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recordedAudioDao(): RecordedAudioDao
    abstract fun newEntryDao(): NewEntryDao
    abstract fun topicDao(): TopicDao
}