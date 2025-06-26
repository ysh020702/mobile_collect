package com.example.collecthealthdata.data.repositoryimpl

import com.example.collecthealthdata.domain.local.TrackedDataDao
import com.example.collecthealthdata.domain.local.TrackedDataEntity

import com.example.collecthealthdata.domain.repository.TrackedDataRepository
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
    override suspend fun deleteById(id: Int){
        dao.deleteById(id)
    }
}