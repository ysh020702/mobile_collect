package com.example.collecthealthdata.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.collecthealthdata.domain.model.TrackedData

@Entity(tableName = "tracked_data")
data class TrackedDataEntity (
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var hrDataString: String = "", //hr 값을을 ,로 이어붙인 문자열
    //TODO: 다른 데이터를 추가하기!
    val timestamp: Long = System.currentTimeMillis()
)