package com.example.collecthealthdata.data

import kotlinx.serialization.Serializable

@Serializable
data class TrackedDataEntity (
    var id: Int = 0,
    val craving: Boolean, //담배 피고 싶은지?
    var hrDataString: String = "", //hr 값을을 ,로 이어붙인 문자열
    var ibiDataString: String= "",

    var spo2Value: Int?,
    var spo2MeasuredAt: Long?,
    var recentActivityLevel: Float?,

    val timestamp: Long = System.currentTimeMillis(),
    val startTime: String = "",
    val endTime: String=""
)