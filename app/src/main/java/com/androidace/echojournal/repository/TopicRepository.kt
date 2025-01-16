package com.androidace.echojournal.repository

import com.androidace.echojournal.db.Topic
import com.androidace.echojournal.db.dao.TopicDao
import javax.inject.Inject

class TopicRepository @Inject constructor(
    private val topicDao: TopicDao
) {
    suspend fun insertTopic(topic: Topic): Long {
        return topicDao.insertTopic(topic)
    }

    suspend fun deleteTopic(topic: Topic) {
        topicDao.deleteTopic(topic)
    }

    suspend fun getAllTopics(): List<Topic> {
        return topicDao.getAllTopics()
    }

    suspend fun insertTopicAndGet(topic: Topic): Topic {
        return topicDao.insertTopicAndReturn(topic)
    }

}