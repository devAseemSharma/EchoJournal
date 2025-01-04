package com.androidace.echojournal.audio.recorder

interface VoiceRecorder {
    fun startRecording(outputPath: String)
    fun pauseRecording()
    fun resumeRecording()
    fun stopRecording()
    fun isRecording(): Boolean
    fun isPaused(): Boolean
}