package com.example.collecthealthdata.domain.model

import com.example.collecthealthdata.data.local.TrackedDataEntity
import kotlinx.serialization.Serializable

@Serializable
data class TrackedData (
    var hr: Int = 0,
    var ibi: ArrayList<Int> = arrayListOf()
)
