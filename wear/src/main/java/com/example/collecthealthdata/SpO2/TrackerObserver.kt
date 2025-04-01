package com.example.collecthealthdata.SpO2

interface TrackerObserver {
    fun onTrackerDataChanged(status: Int, spO2Value: Int)

    fun onError(errorResourceId: Int)
}