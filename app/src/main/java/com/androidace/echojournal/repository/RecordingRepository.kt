package com.androidace.echojournal.repository

import com.androidace.echojournal.db.RecordedAudio
import com.androidace.echojournal.db.dao.RecordedAudioDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecordingRepository @Inject constructor(
    private val dao: RecordedAudioDao
) {
    fun getAllRecordings(): Flow<List<RecordedAudio>> = dao.getAll()

    suspend fun insertRecording(recordedAudio: RecordedAudio): RecordedAudio? {
        return dao.insertRecordingAndReturnRecording(recordedAudio)
    }

}