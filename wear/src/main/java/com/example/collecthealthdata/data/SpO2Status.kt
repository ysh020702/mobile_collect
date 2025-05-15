package com.example.collecthealthdata.data

class SpO2Status {
    companion object{
        val LOW_SIGNAL: Int = -5
        val DEVICE_MOVING: Int = -4
        val INITIAL_STATUS: Int = -1
        val CALCULATING: Int = 0
        val MEASUREMENT_COMPLETED: Int = 2
    }
}