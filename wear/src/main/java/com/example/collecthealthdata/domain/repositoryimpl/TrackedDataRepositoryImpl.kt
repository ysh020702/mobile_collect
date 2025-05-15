package com.example.collecthealthdata.domain.repositoryimpl

import com.example.collecthealthdata.data.local.TrackedDataDao
import com.example.collecthealthdata.data.local.TrackedDataEntity

import com.example.collecthealthdata.data.repository.TrackedDataRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TrackedDataRepositoryImpl @Inject constructor(
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