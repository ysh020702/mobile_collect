package com.example.collecthealthdata.domain

import com.example.collecthealthdata.domain.local.TrackedDataEntity
import com.example.collecthealthdata.domain.local.TrackedDataSerializable

class HelpFunctions {

    companion object{
        fun toSerializable(entity : TrackedDataEntity) : TrackedDataSerializable {
            return TrackedDataSerializable(
                id = entity.id,
                craving = entity.craving,
                hrDataString = entity.hrDataString,
                ibiDataString = entity.ibiDataString,
                accelDataString = entity.accelDataString,
                spo2Value = entity.spo2Value,
                spo2MeasuredAt = entity.spo2MeasuredAt,
                recentActivityLevel = entity.recentActivityLevel,
                timestamp = entity.timestamp,
                startTime = entity.startTime,
                endTime = entity.endTime
            )
        }
    }
}