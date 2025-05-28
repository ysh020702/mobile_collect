package com.example.collecthealthdata.data.usecase

import com.example.collecthealthdata.data.repositoryimpl.AccelTrackerMessage
import com.example.collecthealthdata.domain.repository.AccelerometerTrackingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AccelerometerUseCase @Inject constructor(
    private val accelerometerTrackingRepository: AccelerometerTrackingRepository
){
    suspend operator fun invoke(): Flow<AccelTrackerMessage> = accelerometerTrackingRepository.track()
}