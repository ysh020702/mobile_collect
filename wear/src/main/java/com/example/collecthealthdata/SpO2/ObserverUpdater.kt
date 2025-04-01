package com.example.collecthealthdata.SpO2

object ObserverUpdater {
    private val trackerObservers = mutableListOf<TrackerObserver>()
    private val connectionObservers = mutableListOf<ConnectionObserver>()

    fun addTrackerObserver(observer: TrackerObserver) {
        trackerObservers.add(observer)
    }

    fun removeTrackerObserver(observer: TrackerObserver) {
        trackerObservers.remove(observer)
    }

    fun addConnectionObserver(observer: ConnectionObserver) {
        connectionObservers.add(observer)
    }

    fun removeConnectionObserver(observer: ConnectionObserver) {
        connectionObservers.remove(observer)
    }

    fun notifyTrackerObservers(status: Int, spO2Value: Int) {
        trackerObservers.forEach { it.onTrackerDataChanged(status, spO2Value) }
    }

    fun displayError(errorResourceId: Int) {
        trackerObservers.forEach { it.onError(errorResourceId) }
    }

    fun notifyConnectionObservers(stringResourceId: Int) {
        connectionObservers.forEach { it.onConnectionResult(stringResourceId) }
    }
}
