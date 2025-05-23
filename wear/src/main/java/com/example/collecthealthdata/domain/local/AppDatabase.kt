package com.example.collecthealthdata.domain.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [TrackedDataEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun trackedDataDao(): TrackedDataDao
}