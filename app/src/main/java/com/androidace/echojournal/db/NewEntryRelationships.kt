package com.androidace.echojournal.db

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.androidace.echojournal.ui.newentry.NewEntry

data class NewEntryWithTopics(
    @Embedded val newEntry: NewEntry,
    @Relation(
        parentColumn = "id",
        entityColumn = "topicId",
        associateBy = Junction(NewEntryTopicCrossRef::class)
    )
    val topics: List<Topic>
)

data class TopicWithNewEntries(
    @Embedded val topic: Topic,
    @Relation(
        parentColumn = "topicId",
        entityColumn = "id",
        associateBy = Junction(NewEntryTopicCrossRef::class)
    )
    val newEntries: List<NewEntryEntity>
)