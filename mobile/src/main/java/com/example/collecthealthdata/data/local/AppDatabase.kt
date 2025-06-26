package com.example.collecthealthdata.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.collecthealthdata.data.local.TrackedDataEntity

@Database(
    entities = [TrackedDataEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase(){
    abstract fun trackedDataDao(): TrackedDataDao
}