package com.example.collecthealthdata.RoomDB

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface TrackedDataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrackedData(trackedData: TrackedData)

    @Query("SELECT * FROM TrackedData")
    suspend fun getAllUsers(): List<User>

    @Delete
    suspend fun deleteUser(user: User)
}