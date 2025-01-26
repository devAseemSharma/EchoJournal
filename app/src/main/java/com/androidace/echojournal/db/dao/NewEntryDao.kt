package com.androidace.echojournal.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.androidace.echojournal.db.NewEntryEntity
import com.androidace.echojournal.db.NewEntryTopicCrossRef
import com.androidace.echojournal.db.NewEntryWithAudioAndTopics
import com.androidace.echojournal.db.NewEntryWithTopics
import com.androidace.echojournal.db.Topic
import com.androidace.echojournal.ui.newentry.NewEntry

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
    @Query("SELECT * FROM new_entry ORDER BY createdAt DESC")
    suspend fun getAllEntriesByNewestFirst(): List<NewEntryWithAudioAndTopics>

    @Transaction
    @Query("""
    SELECT ne.*
    FROM new_entry AS ne
    INNER JOIN new_entry_topic_cross_ref AS cr
      ON ne.newEntryId = cr.newEntryId
    WHERE cr.topicId IN (:topicIds)
    GROUP BY ne.newEntryId
    HAVING COUNT(DISTINCT cr.topicId) = :topicCount
    ORDER BY ne.createdAt DESC
""")
    suspend fun getNewEntriesByAllTopicsSorted(topicIds: List<Int>, topicCount: Int): List<NewEntryWithAudioAndTopics>

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