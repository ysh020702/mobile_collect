package com.example.collecthealthdata.domain.usecase

import com.example.collecthealthdata.domain.repository.TrackingRepository
import javax.inject.Inject

class StopTrackingUseCase @Inject constructor(
    private val trackingRepository: TrackingRepository
) {
    operator fun invoke() {
        trackingRepository.stopTracking()
    }
}