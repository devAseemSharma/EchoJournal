package com.androidace.echojournal.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "new_entry",
    foreignKeys = [
        ForeignKey(
            entity = RecordedAudio::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("recording_id"),
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["recording_id"])]
)
data class NewEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "recording_id") val recordingId: Int = 0,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "description") val description: String,
)
