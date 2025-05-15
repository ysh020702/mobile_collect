package com.example.collecthealthdata.domain.usecase.roomDB

import com.example.collecthealthdata.data.local.TrackedDataEntity
import com.example.collecthealthdata.data.repository.TrackedDataRepository
import javax.inject.Inject

class InsertTrackedDataUseCase @Inject constructor(
    private val repository: TrackedDataRepository
) {
    suspend operator fun invoke(data: TrackedDataEntity) {
        repository.insert(data)
    }
}