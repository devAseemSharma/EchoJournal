package com.androidace.echojournal.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.androidace.echojournal.db.RecordedAudio
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordedAudioDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recordedAudio: RecordedAudio)

    @Query("SELECT * FROM recorded_audio ORDER BY timestamp DESC")
    fun getAll(): Flow<List<RecordedAudio>>
}