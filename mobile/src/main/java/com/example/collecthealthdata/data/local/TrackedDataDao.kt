package com.example.collecthealthdata.data.local

import com.example.collecthealthdata.data.entity.TrackedDataEntity

@Dao
interface TrackedDataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: TrackedDataEntity)

    @Query("SELECT * FROM tracked_data")
    fun getAll(): Flow<List<TrackedDataEntity>>

    @Query("DELETE FROM tracked_data WHERE id = :id")
    suspend fun deleteById(id: Int)
}
