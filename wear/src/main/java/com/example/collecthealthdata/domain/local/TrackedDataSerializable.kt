package com.example.collecthealthdata.domain.local

import kotlinx.serialization.Serializable

// 직렬화용 데이터 클래스
@Serializable
data class TrackedDataSerializable(
    val id: Int = 0,
    val craving: Boolean,
    val hrDataString: String = "",
    var ibiDataString: String= "",
    var spo2Value: Int?,
    var spo2MeasuredAt: Long?,
    var recentActivityLevel: Float?,

    val timestamp: Long = System.currentTimeMillis(),
    val startTime: String = "",
    val endTime: String = ""
)