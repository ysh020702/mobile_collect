package com.example.collecthealthdata.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "tracked_data")
data class TrackedDataEntity (
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    val craving: Boolean, //담배 피고 싶은지?
    var hrDataString: String = "", //hr 값을을 ,로 이어붙인 문자열
    //TODO: 다른 데이터를 추가하기!

    val timestamp: Long = System.currentTimeMillis(),
    val startTime: String = "",
    val endTime: String=""
)

