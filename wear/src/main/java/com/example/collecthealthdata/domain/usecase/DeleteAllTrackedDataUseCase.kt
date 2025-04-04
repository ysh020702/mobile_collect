package com.example.collecthealthdata.domain.usecase

import com.example.collecthealthdata.domain.repository.TrackedDataRepository
import javax.inject.Inject

class DeleteAllTrackedDataUseCase @Inject constructor(
    private val repository: TrackedDataRepository
) {
    suspend operator fun invoke() {
        repository.deleteAll()
    }
}