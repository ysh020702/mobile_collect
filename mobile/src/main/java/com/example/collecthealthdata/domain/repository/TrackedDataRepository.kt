package com.example.collecthealthdata.domain.repository

import com.example.collecthealthdata.domain.TrackedDataEntity

interface TrackedDataRepository {
    suspend fun saveData (userId: String, dataList: List<TrackedDataEntity>)
}