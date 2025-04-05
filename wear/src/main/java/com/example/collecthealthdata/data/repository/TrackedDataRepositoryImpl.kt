package com.example.collecthealthdata.data.repository

import com.example.collecthealthdata.data.local.TrackedDataDao
import com.example.collecthealthdata.data.local.TrackedDataEntity

import com.example.collecthealthdata.domain.repository.TrackedDataRepository
import kotlinx.coroutines.flow.Flow

class TrackedDataRepositoryImpl (
    private val dao: TrackedDataDao
): TrackedDataRepository{
    override suspend fun insert(entity: TrackedDataEntity){
        dao.insert(entity)
    }
    override fun getAll(): Flow<List<TrackedDataEntity>> {
        return dao.getAll()
    }
    override suspend fun deleteAll(){
        dao.deleteAll()
    }
}