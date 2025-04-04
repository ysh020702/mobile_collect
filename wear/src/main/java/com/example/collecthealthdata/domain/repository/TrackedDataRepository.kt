package com.example.collecthealthdata.domain.repository

import com.example.collecthealthdata.domain.model.TrackedData
import kotlinx.coroutines.flow.Flow

interface TrackedDataRepository {
    suspend fun insert(trackedData: TrackedData)
    fun getAll(): Flow<List<TrackedData>>
    suspend fun deleteAll()
}

//data - repository 에 구현체(TrackedDataRepositoryImpl 있음)