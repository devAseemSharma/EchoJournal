package com.androidace.echojournal.ui.home.model

import com.androidace.echojournal.db.Topic
import com.androidace.echojournal.ui.mood.model.Mood
import com.androidace.echojournal.ui.newentry.model.AudioWaveFormState

data class TimelineEntry(
    val id: Int,
    val mood: Mood,
    val title: String,
    val description: String?,
    val createdAt: Long, // e.g., System.currentTimeMillis()
    val topics: List<Topic>, // optional: list of associated topics
    val recordingId: Int,
    val audioDuration: String, // e.g., "00:12:30"
    val audioPath: String?,
    val audioWaveFormState: AudioWaveFormState = AudioWaveFormState()
    // any other fields, e.g., audio path
)
