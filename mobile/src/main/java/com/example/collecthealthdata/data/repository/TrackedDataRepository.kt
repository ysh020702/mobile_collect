package com.example.collecthealthdata.data.repository

import com.example.collecthealthdata.data.TrackedDataEntity

interface TrackedDataRepository {
    suspend fun saveData (userId: String, dataList: List<TrackedDataEntity>)
}