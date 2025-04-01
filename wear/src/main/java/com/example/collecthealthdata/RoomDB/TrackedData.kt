package com.example.collecthealthdata.RoomDB

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trackedData")
data class TrackedData(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    @ColumnInfo(name = "userId") var userId: Int = -1, //TODO: 외래키 설정할 것
    @ColumnInfo(name = "TimeStamp")var TimeStamp: Int = 0,
    @ColumnInfo(name = "HeartRate")var HeartRate: Int = 0,
    @ColumnInfo(name = "SpO2")var SpO2: Int = 0,
    @ColumnInfo(name = "SkinTemperature")var SkinTemperature: Int = 0
    //var ECG
    //var PPG
)