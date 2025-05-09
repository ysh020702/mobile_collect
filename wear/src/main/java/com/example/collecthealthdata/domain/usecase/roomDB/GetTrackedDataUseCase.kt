package com.example.collecthealthdata.domain.usecase.roomDB

import com.example.collecthealthdata.data.local.TrackedDataEntity
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