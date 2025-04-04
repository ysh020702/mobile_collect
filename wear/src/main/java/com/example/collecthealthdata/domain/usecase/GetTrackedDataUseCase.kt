package com.example.collecthealthdata.domain.usecase

import com.example.collecthealthdata.domain.model.TrackedData
import com.example.collecthealthdata.domain.repository.TrackedDataRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTrackedDataUseCase @Inject constructor(
    private val repository: TrackedDataRepository
) {
    operator fun invoke(): Flow<List<TrackedData>>{
        return repository.getAll()
    }
}