package com.example.collecthealthdata.SpO2


import android.app.Activity
import android.content.Context
import android.util.Log
import com.example.collecthealthdata.R
import com.samsung.android.service.health.tracking.*
import com.samsung.android.service.health.tracking.data.HealthTrackerType

class ConnectionManager {
    companion object {
        private const val TAG = "Connection Manager"
    }

    private var callingActivity: Activity? = null
    private var healthTrackingService: HealthTrackingService? = null

    private val connectionListener = object : ConnectionListener {
        override fun onConnectionSuccess() {
            Log.i(TAG, "Connected")
            ObserverUpdater.notifyConnectionObservers(R.string.ConnectedToHS)
            if (!isSpO2Available(healthTrackingService)) {
                Log.i(TAG, "Device does not support Blood Oxygen Level tracking")
                ObserverUpdater.notifyConnectionObservers(R.string.NoSPo2Support)
            }
        }

        override fun onConnectionEnded() {
            Log.i(TAG, "Disconnected")
        }

        override fun onConnectionFailed(e: HealthTrackerException) {
            processTrackerException(e)
        }
    }

    fun processTrackerException(e: HealthTrackerException) {
        val hasResolution = e.hasResolution()
        if (hasResolution) {
            callingActivity?.let { e.resolve(it) }
        }
        when (e.errorCode) {
            HealthTrackerException.OLD_PLATFORM_VERSION, HealthTrackerException.PACKAGE_NOT_INSTALLED ->
                ObserverUpdater.notifyConnectionObservers(R.string.NoValidHealthPlatform)
            else ->
                ObserverUpdater.notifyConnectionObservers(R.string.ConnectionError)
        }
        Log.e(TAG, "Could not connect to Health Tracking Service: ${e.message}")
    }

    fun connect(activity: Activity, context: Context) {
        callingActivity = activity
        healthTrackingService = HealthTrackingService(connectionListener, context)
        healthTrackingService?.connectService()
    }

    fun disconnect() {
        healthTrackingService?.disconnectService()
    }

    fun initSpO2(spO2Listener: SpO2Listener) {
        spO2Listener.init(healthTrackingService)
    }

    fun isSpO2Available(healthTrackingService: HealthTrackingService?): Boolean {
        val availableTrackers = healthTrackingService?.trackingCapability?.supportHealthTrackerTypes
        return availableTrackers?.contains(HealthTrackerType.SPO2_ON_DEMAND) ?: false
    }
}