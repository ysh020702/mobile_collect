package com.example.collecthealthdata.data.usecase

import com.example.collecthealthdata.data.repositoryimpl.TrackerMessage
import com.example.collecthealthdata.domain.repository.TrackingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TrackingUseCase @Inject constructor(
    private val trackingRepository: TrackingRepository
) {
    suspend operator fun invoke(): Flow<TrackerMessage> = trackingRepository.track()
}