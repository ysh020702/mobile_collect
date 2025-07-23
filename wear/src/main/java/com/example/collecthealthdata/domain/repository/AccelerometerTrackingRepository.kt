package com.example.collecthealthdata.domain.repository

import com.example.collecthealthdata.data.repositoryimpl.AccelTrackerMessage
import kotlinx.coroutines.flow.Flow

interface AccelerometerTrackingRepository {
    suspend fun track(): Flow<AccelTrackerMessage>
    fun hasCapabilities(): Boolean
    fun stopTracking()
}