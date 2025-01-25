package com.androidace.echojournal.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.androidace.echojournal.db.Topic
import com.androidace.echojournal.db.TopicWithNewEntries

@Dao
interface TopicDao {
    // Insert a single Topic
    @Insert
    suspend fun insertTopic(topic: Topic): Long

    // Insert multiple Topics
    @Insert
    suspend fun insertTopics(vararg topics: Topic): List<Long>

    // Delete a single Topic
    @Delete
    suspend fun deleteTopic(topic: Topic)

    // Fetch all Topics
    @Query("SELECT * FROM topics")
    suspend fun getAllTopics(): List<Topic>

    @Query("SELECT * FROM topics WHERE topicId = :id LIMIT 1")
    suspend fun getTopicById(id: Long): Topic?

    @Transaction
    suspend fun insertTopicAndReturn(topic: Topic): Topic {
        // 1) Insert the topic
        val rowId = insertTopic(topic)

        // 2) Fetch the newly inserted topic by ID
        return getTopicById(rowId)
            ?: throw IllegalStateException("Unable to find the inserted topic with ID $rowId")
    }

    @Transaction
    @Query("SELECT * FROM topics WHERE topicId = :topicId")
    suspend fun getTopicWithEntries(topicId: Int): TopicWithNewEntries?

    @Query("SELECT * FROM topics WHERE topicId IN (:topicIds)")
    suspend fun getTopicsByIds(topicIds: List<Int>): List<Topic>
}