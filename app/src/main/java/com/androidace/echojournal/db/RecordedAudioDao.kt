package com.androidace.echojournal.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordedAudioDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recordedAudio: RecordedAudio)

    @Query("SELECT * FROM recorded_audio ORDER BY timestamp DESC")
    fun getAll(): Flow<List<RecordedAudio>>
}