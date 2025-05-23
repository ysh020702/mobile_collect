package com.example.collecthealthdata.domain.repository

import com.example.collecthealthdata.data.repositoryimpl.TrackerMessage
import com.example.collecthealthdata.domain.TrackedData
import kotlinx.coroutines.flow.Flow

interface TrackingRepository {
    fun hasCapabilities(): Boolean
    suspend fun track(): Flow<TrackerMessage>
    fun stopTracking()
    fun getValidHrData(): ArrayList<TrackedData>
}