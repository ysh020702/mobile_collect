package com.example.collecthealthdata.data

import com.example.collecthealthdata.data.local.TrackedDataEntity
import com.example.collecthealthdata.data.local.TrackedDataSerializable

class HelpFunctions {

    companion object{
        fun toSerializable(entity : TrackedDataEntity) : TrackedDataSerializable {
            return TrackedDataSerializable(
                id = entity.id,
                craving = entity.craving,
                hrDataString = entity.hrDataString,
                timestamp = entity.timestamp,
                startTime = entity.startTime,
                endTime = entity.endTime
            )
        }
    }
}