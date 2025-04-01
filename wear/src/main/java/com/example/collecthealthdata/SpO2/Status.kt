package com.example.collecthealthdata.SpO2

class Status {
    companion object {
        const val LOW_SIGNAL = -5
        const val DEVICE_MOVING = -4
        const val INITIAL_STATUS = -1
        const val CALCULATING = 0
        const val MEASUREMENT_COMPLETED = 2
    }
}