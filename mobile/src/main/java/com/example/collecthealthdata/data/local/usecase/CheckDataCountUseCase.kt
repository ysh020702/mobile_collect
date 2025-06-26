package com.example.collecthealthdata.data.local.usecase

import com.example.collecthealthdata.data.local.TrackedDataDao
import javax.inject.Inject

class CheckDataCountUseCase @Inject constructor(
    private val dao: TrackedDataDao
) {
    suspend operator fun invoke(): Int {
        return dao.getCount()
    }
}