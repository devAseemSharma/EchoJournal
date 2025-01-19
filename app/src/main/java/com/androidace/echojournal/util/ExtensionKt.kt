package com.androidace.echojournal.util

import kotlin.time.DurationUnit
import kotlin.time.toDuration

// Helper to format milliseconds to HH:MM:SS
fun formatMillis(millis: Long): String {
    val totalSeconds = millis / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        // If you prefer mm:ss when hours == 0
        String.format("%02d:%02d", minutes, seconds)
    }
}