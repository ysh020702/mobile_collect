package com.example.collecthealthdata.SpO2

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.collecthealthdata.R
import com.samsung.android.service.health.tracking.HealthTracker
import com.samsung.android.service.health.tracking.HealthTrackingService
import com.samsung.android.service.health.tracking.data.DataPoint
import com.samsung.android.service.health.tracking.data.HealthTrackerType
import com.samsung.android.service.health.tracking.data.ValueKey

class SpO2Listener {

    private val TAG = "SpO2 Listener"
    private val spo2Handler = Handler(Looper.getMainLooper())
    private var isHandlerRunning = false
    private var spo2Tracker: HealthTracker? = null


    private val spo2Listener = object : HealthTracker.TrackerEventListener {
        override fun onDataReceived(list: List<DataPoint>) {
            for (data in list) {
                updateSpo2(data)
            }
        }

        override fun onFlushCompleted() {
            Log.i(TAG, "Flush completed")
        }

        override fun onError(trackerError: HealthTracker.TrackerError) {
            Log.i(TAG, "SpO2 Tracker error: $trackerError")
            when (trackerError) {
                HealthTracker.TrackerError.PERMISSION_ERROR ->
                    ObserverUpdater.displayError(R.string.NoPermission)
                HealthTracker.TrackerError.SDK_POLICY_ERROR ->
                    ObserverUpdater.displayError(R.string.SDKPolicyError)
                else -> {}
            }
        }
    }

    fun init(healthTrackingService: HealthTrackingService?) {
        spo2Tracker = healthTrackingService?.getHealthTracker(HealthTrackerType.SPO2_ON_DEMAND)
    }

    fun startTracker() {
        if (!isHandlerRunning) {
            spo2Handler.post { spo2Tracker?.setEventListener(spo2Listener) }
            isHandlerRunning = true
        }
    }

    fun stopTracker() {
        spo2Tracker?.unsetEventListener()
        spo2Handler.removeCallbacksAndMessages(null)
        isHandlerRunning = false
    }

    private fun updateSpo2(data: DataPoint) {
        val status = data.getValue(ValueKey.SpO2Set.STATUS)
        val spo2Value = if (status == Status.MEASUREMENT_COMPLETED) {
            data.getValue(ValueKey.SpO2Set.SPO2)
        } else {
            0
        }
        ObserverUpdater.notifyTrackerObservers(status, spo2Value)
    }
}