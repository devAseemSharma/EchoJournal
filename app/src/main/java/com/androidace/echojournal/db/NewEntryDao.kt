package com.androidace.echojournal.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface NewEntryDao {
    @Insert
    suspend fun insertNewEntry(newEntry: NewEntryEntity): Long

    @Insert
    suspend fun insertTopics(vararg topics: Topic): List<Long>

    @Insert
    suspend fun insertCrossRef(crossRef: NewEntryTopicCrossRef)

    @Transaction
    @Query("SELECT * FROM new_entry WHERE id = :entryId")
    suspend fun getNewEntryWithTopics(entryId: Int): NewEntryWithTopics?

    @Transaction
    suspend fun insertNewEntryWithTopics(
        newEntry: NewEntryEntity,
        topics: List<Topic>
    ): Long {
        // 1. Insert the NewEntry
        val newEntryId = insertNewEntry(newEntry).toInt()

        // 2. Insert Topics (or fetch existing if you want to reuse topics)
        val topicIds = insertTopics(*topics.toTypedArray())

        // 3. For each topic, create cross-ref
        topicIds.forEach { topicId ->
            insertCrossRef(NewEntryTopicCrossRef(newEntryId, topicId.toInt()))
        }

        return newEntryId.toLong()
    }
}