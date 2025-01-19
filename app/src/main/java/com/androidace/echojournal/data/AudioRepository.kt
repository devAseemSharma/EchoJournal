package com.androidace.echojournal.data

import com.androidace.echojournal.db.RecordedAudio
import com.androidace.echojournal.db.dao.RecordedAudioDao
import com.androidace.echojournal.model.LocalAudio
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AudioRepository @Inject constructor(
    private val localMediaDataSource: RecordedAudioDao,
    private val audioManager: AudioManager,
    private val ioDispatcher: CoroutineDispatcher
) {

    suspend fun loadAudioByContentId(id: Int): RecordedAudio? = withContext(ioDispatcher) {
        return@withContext localMediaDataSource.getRecordedAudioById(id.toLong())
    }

    suspend fun loadAudioAmplitudes(
        localAudio: RecordedAudio
    ): List<Int> = withContext(ioDispatcher) {
        return@withContext audioManager.getAmplitudes(localAudio.fileUri)
    }

}