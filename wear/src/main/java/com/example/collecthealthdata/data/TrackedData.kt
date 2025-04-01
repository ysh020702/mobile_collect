package com.example.collecthealthdata.data

data class TrackedData(
    var TimeStamp: Int = 0,
    var HeartRate: Int = 0,
    var SpO2: Int = 0,
    var SkinTemperature: Int = 0
    //var ECG
    //var PPG
)
