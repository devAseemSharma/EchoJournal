package com.androidace.echojournal.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "topics")
data class Topic(
    @PrimaryKey(autoGenerate = true)
    val topicId: Int = 0,
    val name: String
)