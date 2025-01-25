package com.androidace.echojournal.repository

import com.androidace.echojournal.db.NewEntryEntity
import com.androidace.echojournal.db.RecordedAudio
import com.androidace.echojournal.db.Topic
import com.androidace.echojournal.db.dao.NewEntryDao
import com.androidace.echojournal.db.dao.TopicDao
import com.androidace.echojournal.ui.mood.model.Mood
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class NewEntryRepository @Inject constructor(
    private val newEntryDao: NewEntryDao,
    private val topicDao: TopicDao,
    private val coroutineContext: CoroutineDispatcher
) {
    suspend fun insertNewEntryWithTopic(
        title: String,
        description: String = "",
        mood: Mood,
        recordedAudio: RecordedAudio,
        selectedTopics: List<Topic>
    ): Long {
        return withContext(coroutineContext) {
            val topics = topicDao.getTopicsByIds(selectedTopics.map { it.topicId })

            newEntryDao.insertNewEntryWithTopics(
                NewEntryEntity(
                    recordingId = recordedAudio.id,
                    title = title,
                    description = description,
                    mood = mood
                ),
                topics = topics
            )
        }
    }
}