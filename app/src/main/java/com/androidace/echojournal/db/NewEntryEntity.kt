package com.androidace.echojournal.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.androidace.echojournal.ui.mood.model.Mood

@Entity(
    tableName = "new_entry",
    foreignKeys = [
        ForeignKey(
            entity = RecordedAudio::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("recordingId"),
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["recordingId"])]
)
data class NewEntryEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "newEntryId") val newEntryId: Int = 0,
    @ColumnInfo(name = "recordingId") val recordingId: Int = 0,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "description") val description: String,
    val createdAt: Long, // Store epoch millis
    val mood: Mood
)
