package com.example.collecthealthdata.RoomDB

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [TrackedData::class], version = 1, exportSchema = false)
abstract class TrackedDataDB : RoomDatabase() {
    abstract fun TrackedDataDao(): TrackedDataDao

    companion object {
        @Volatile
        private var INSTANCE: TrackedDataDB? = null

        fun getDatabase(context: Context): TrackedDataDB {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TrackedDataDB::class.java,
                    "app_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}