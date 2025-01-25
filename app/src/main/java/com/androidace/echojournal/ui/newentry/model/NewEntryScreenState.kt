package com.androidace.echojournal.ui.newentry.model

import com.androidace.echojournal.db.Topic
import com.androidace.echojournal.ui.mood.model.Mood

data class NewEntryScreenState(
    val newEntryTitle: String = "",
    val selectedMood: Mood? = null,
    val newEntryTitleHint: String = "Add Title...",
    val topicSearchText: String = "",
    val topicSearchTextHint: String = "Topic",
    val listTopics: List<Topic> = emptyList(),
    val descriptionText: String = "",
    val descriptionTextHint: String = "Add Description...",
    val audioWaveFormState: AudioWaveFormState = AudioWaveFormState()
)

data class AudioWaveFormState(
    val amplitudes: List<Int> = emptyList(),
    val isPlaying: Boolean = false,
    val progress: Float = 0F,
    val totalDuration: String = "00:00",
    val seekDuration: String = "00:00"
)
