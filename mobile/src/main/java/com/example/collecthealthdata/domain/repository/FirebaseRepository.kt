package com.example.collecthealthdata.domain.repository

import com.example.collecthealthdata.data.local.TrackedDataEntity

interface FirebaseRepository {
    suspend fun uploadData(data: TrackedDataEntity)
}