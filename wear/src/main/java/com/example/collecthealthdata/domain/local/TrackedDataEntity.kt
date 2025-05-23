package com.example.collecthealthdata.domain.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tracked_data")
data class TrackedDataEntity (
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    val craving: Boolean, //담배 피고 싶은지?
    var hrDataString: String = "", //hr 값을을 ,로 이어붙인 문자열

    //추가된 부분
    var ibiDataString: String= "",
    val accelDataString: String = "",
    var spo2Value: Int?,
    var spo2MeasuredAt: Long?,
    var recentActivityLevel: Float?,


    val timestamp: Long = System.currentTimeMillis(),
    val startTime: String = "",
    val endTime: String=""
)

