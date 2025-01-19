package com.androidace.echojournal.ui.newentry

import kotlinx.serialization.Serializable

@Serializable
data class NewEntry(
    val recordingPath: String?,
    val id: Int,
    val timeStamp: Long
)