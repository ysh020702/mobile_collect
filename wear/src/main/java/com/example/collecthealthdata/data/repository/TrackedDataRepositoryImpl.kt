package com.example.collecthealthdata.data.repository

import com.example.collecthealthdata.data.local.TrackedDataDao
import com.example.collecthealthdata.data.local.toDomain
import com.example.collecthealthdata.domain.model.TrackedData
import com.example.collecthealthdata.domain.model.toEntity
import com.example.collecthealthdata.domain.repository.TrackedDataRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TrackedDataRepositoryImpl (
    private val dao: TrackedDataDao
): TrackedDataRepository{
    override suspend fun insert(trackedData: TrackedData){
        dao.insert(trackedData.toEntity())
    }
    override fun getAll(): Flow<List<TrackedData>>{
        return dao.getAll().map{ list -> list.map{ it.toDomain()}}
    }
    override suspend fun deleteAll(){
        dao.deleteAll()
    }
}