package com.example.collecthealthdata.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackedDataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: TrackedDataEntity)

    @Query("SELECT * FROM tracked_data")
    fun getAll(): Flow<List<TrackedDataEntity>>

    @Query("SELECT * FROM tracked_data")
    suspend fun getAllDataOnce(): List<TrackedDataEntity>

    @Query("DELETE FROM tracked_data WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("SELECT COUNT(*) FROM tracked_data")
    suspend fun getCount(): Int
}
