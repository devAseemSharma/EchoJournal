package com.androidace.echojournal.db

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class NewEntryWithTopics(
    @Embedded val newEntry: NewEntryEntity,
    @Relation(
        parentColumn = "newEntryId",
        entityColumn = "topicId",
        associateBy = Junction(NewEntryTopicCrossRef::class)
    )
    val topics: List<Topic>
)

data class TopicWithNewEntries(
    @Embedded val topic: Topic,
    @Relation(
        parentColumn = "topicId",
        entityColumn = "newEntryId",
        associateBy = Junction(NewEntryTopicCrossRef::class)
    )
    val newEntries: List<NewEntryEntity>
)