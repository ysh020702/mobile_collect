package com.example.collecthealthdata.data.usecase.roomDB

import com.example.collecthealthdata.domain.local.TrackedDataEntity
import com.example.collecthealthdata.domain.repository.TrackedDataRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTrackedDataUseCase @Inject constructor(
    private val repository: TrackedDataRepository
) {
    operator fun invoke(): Flow<List<TrackedDataEntity>> {
        return repository.getAll()
    }
}