package com.androidace.echojournal.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.androidace.echojournal.db.RecordedAudio
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordedAudioDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recordedAudio: RecordedAudio): Long

    @Query("SELECT * FROM recorded_audio ORDER BY timestamp DESC")
    fun getAll(): Flow<List<RecordedAudio>>

    @Transaction
    suspend fun insertRecordingAndReturnRecording(recordedAudio: RecordedAudio): RecordedAudio? {
        val rowId = insert(recordedAudio)

        return getRecordedAudioById(rowId)
            ?: throw IllegalStateException("Unable to find the record with id: $rowId")
    }

    @Query("SELECT * FROM recorded_audio WHERE id = :id")
    fun getRecordedAudioById(id: Long): RecordedAudio?
}