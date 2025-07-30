package com.example.collecthealthdata.data.usecase

import com.example.collecthealthdata.data.repositoryimpl.AccelerometerTrackingRepositoryImpl
import com.example.collecthealthdata.domain.repository.AccelerometerTrackingRepository
import com.example.collecthealthdata.domain.repository.TrackingRepository
import javax.inject.Inject

class StopTrackingUseCase @Inject constructor(
    private val trackingRepository: TrackingRepository,
    private val accelerometerTrackingRepository: AccelerometerTrackingRepository
) {
    operator fun invoke() {
        trackingRepository.stopTracking()
        accelerometerTrackingRepository.stopTracking()
    }
}