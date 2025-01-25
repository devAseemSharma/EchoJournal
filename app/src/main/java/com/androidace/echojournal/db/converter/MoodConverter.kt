package com.androidace.echojournal.db.converter

import androidx.room.TypeConverter
import com.androidace.echojournal.ui.mood.model.Mood

class MoodConverter {

    @TypeConverter
    fun fromMood(mood: Mood): String {
        return mood.name  // e.g. "STRESSED", "SAD", etc.
    }

    @TypeConverter
    fun toMood(moodName: String): Mood {
        return Mood.valueOf(moodName)
    }
}