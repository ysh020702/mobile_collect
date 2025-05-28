package com.example.collecthealthdata.domain.repository

import com.example.collecthealthdata.domain.local.TrackedDataEntity
import kotlinx.coroutines.flow.Flow

interface TrackedDataRepository {
    suspend fun insert(entity: TrackedDataEntity)
    fun getAll(): Flow<List<TrackedDataEntity>>
    suspend fun deleteAll()
}

//data - repository 에 구현체(TrackedDataRepositoryImpl 있음)