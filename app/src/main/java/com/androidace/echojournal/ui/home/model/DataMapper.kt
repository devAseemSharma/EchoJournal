package com.androidace.echojournal.ui.home.model

import com.androidace.echojournal.db.NewEntryWithAudioAndTopics
import com.androidace.echojournal.util.formatMillis

fun NewEntryWithAudioAndTopics.toTimelineEntry(): TimelineEntry {
    return TimelineEntry(
        id = newEntry.newEntryId,
        mood = newEntry.mood,
        title = newEntry.title,
        description = newEntry.description,
        createdAt = newEntry.createdAt,
        topics = topics, // we already have a list of Topic
        audioPath = recordedAudio?.fileUri,
        audioDuration = formatMillis(recordedAudio?.timestamp ?: 0L)
    )
}