package com.example.collecthealthdata.data.local

@Database(
    entities = [TrackedDataEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase {
    abstract fun trackedDataDao(): TrackedDataDao
}