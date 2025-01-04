package com.androidace.echojournal.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recorded_audio")
data class RecordedAudio(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fileUri: String,
    val timestamp: Long
)