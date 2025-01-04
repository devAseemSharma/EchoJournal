package com.androidace.echojournal.model

import android.net.Uri

data class LocalAudio(
    val id: String,
    val uri: Uri,
    val path: String,
    val name: String,
    val duration: Long,
    val size: Long
) {
    val nameWithoutExtension: String get() = name.substringBeforeLast('.')
}