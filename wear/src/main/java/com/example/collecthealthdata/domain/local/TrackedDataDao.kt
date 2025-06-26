package com.example.collecthealthdata.domain.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackedDataDao {
    @Insert
    suspend fun insert(data: TrackedDataEntity)

    @Query("SELECT * FROM tracked_data ORDER BY timestamp DESC")
    fun getAll(): Flow<List<TrackedDataEntity>>

    @Query("DELETE FROM tracked_data")
    suspend fun deleteAll()

    @Query("DELETE FROM tracked_data WHERE id = :id")
    suspend fun deleteById(id: Int)
}