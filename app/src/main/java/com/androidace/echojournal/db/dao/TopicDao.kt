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

    @Transaction
    @Query("SELECT * FROM topics WHERE topicId = :topicId")
    suspend fun getTopicWithEntries(topicId: Int): TopicWithNewEntries?
}