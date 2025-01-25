package com.androidace.echojournal.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.androidace.echojournal.db.NewEntryEntity
import com.androidace.echojournal.db.NewEntryTopicCrossRef
import com.androidace.echojournal.db.NewEntryWithTopics
import com.androidace.echojournal.db.Topic

@Dao
interface NewEntryDao {
    @Insert
    suspend fun insertNewEntry(newEntry: NewEntryEntity): Long

    @Insert
    suspend fun insertTopics(vararg topics: Topic): List<Long>

    @Insert
    suspend fun insertCrossRef(crossRef: NewEntryTopicCrossRef)

    @Transaction
    @Query("SELECT * FROM new_entry WHERE newEntryId = :entryId")
    suspend fun getNewEntryWithTopics(entryId: Int): NewEntryWithTopics?

    @Transaction
    suspend fun insertNewEntryWithTopics(
        newEntry: NewEntryEntity,
        topics: List<Topic>
    ): Long {
        // 1. Insert the NewEntry
        val newEntryId = insertNewEntry(newEntry).toInt()

        // 3. For each topic, create cross-ref
        topics.forEach { topicId ->
            insertCrossRef(NewEntryTopicCrossRef(newEntryId, topicId.topicId))
        }

        return newEntryId.toLong()
    }
}