package com.androidace.echojournal.db

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.androidace.echojournal.ui.newentry.NewEntry

data class NewEntryWithAudioAndTopics(
    @Embedded val newEntry: NewEntryEntity,

    @Relation(
        parentColumn = "recordingId",
        entityColumn = "id"
    )
    val recordedAudio: RecordedAudio?,

    @Relation(
        parentColumn = "newEntryId",
        entityColumn = "topicId",
        associateBy = Junction(NewEntryTopicCrossRef::class)
    )
    val topics: List<Topic>
)
