package com.androidace.echojournal.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "new_entry_topic_cross_ref",
    primaryKeys = ["newEntryId", "topicId"],
    foreignKeys = [
        ForeignKey(
            entity = NewEntryEntity::class,
            parentColumns = ["newEntryId"],
            childColumns = ["newEntryId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Topic::class,
            parentColumns = ["topicId"],
            childColumns = ["topicId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["newEntryId"]),
        Index(value = ["topicId"])
    ]
)
data class NewEntryTopicCrossRef(
    val newEntryId: Int,
    val topicId: Int
)
