package com.example.collecthealthdata.data.usecase.roomDB

import com.example.collecthealthdata.domain.repository.TrackedDataRepository
import javax.inject.Inject

class DeleteTrackedDataByIdUseCase @Inject constructor(
    private val repository: TrackedDataRepository
) {
    suspend operator fun invoke(id: Int) {
        repository.deleteById(id)
    }
}