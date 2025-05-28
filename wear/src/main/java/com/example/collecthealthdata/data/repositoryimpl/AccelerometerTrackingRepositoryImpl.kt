package com.example.collecthealthdata.data.repositoryimpl
import android.content.Context
import android.util.Log
import com.example.collecthealthdata.domain.local.AccelData
import com.example.collecthealthdata.domain.repository.AccelerometerTrackingRepository
import com.samsung.android.service.health.tracking.HealthTracker
import com.samsung.android.service.health.tracking.HealthTrackingService
import com.samsung.android.service.health.tracking.data.DataPoint
import com.samsung.android.service.health.tracking.data.HealthTrackerType
import com.samsung.android.service.health.tracking.data.ValueKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "AccelerometerTrackingRepositoryImpl"

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class AccelerometerTrackingRepositoryImpl  @Inject constructor(
    private val coroutineScope: CoroutineScope,
    private val healthTrackingServiceConnection: HealthTrackingServiceConnection,
    @ApplicationContext private val context: Context
) : AccelerometerTrackingRepository {

    private val trackingType = HealthTrackerType.ACCELEROMETER_CONTINUOUS
    private var listenerSet = false
    private var healthTrackingService: HealthTrackingService? = null


    private var accelTracker: HealthTracker? = null

    override suspend fun track(): Flow<AccelTrackerMessage> = callbackFlow {
        healthTrackingService = healthTrackingServiceConnection.getHealthTrackingService()

        if (!hasCapabilities()) {
            trySend(AccelTrackerMessage.TrackerErrorMessage("ACCELEROMETER_CONTINUOUS not supported"))
            close()
            return@callbackFlow
        }

        val listener = object : HealthTracker.TrackerEventListener {
            override fun onDataReceived(dataPoints: MutableList<DataPoint>) {
                for (point in dataPoints) {
                    val x = point.getValue(ValueKey.AccelerometerSet.ACCELEROMETER_X)
                    val y = point.getValue(ValueKey.AccelerometerSet.ACCELEROMETER_Y)
                    val z = point.getValue(ValueKey.AccelerometerSet.ACCELEROMETER_Z)
                    //Log.i(TAG, "x: $x, y: $y, z: $z")
                    //val timestamp = System.currentTimeMillis()

                    coroutineScope.runCatching {
                        trySendBlocking(
                            AccelTrackerMessage.DataMessage(AccelData(x, y, z))
                        )
                    }
                }
            }

            override fun onFlushCompleted() {
                coroutineScope.runCatching {
                    trySendBlocking(AccelTrackerMessage.FlushCompletedMessage)
                }
            }

            override fun onError(trackerError: HealthTracker.TrackerError?) {
                coroutineScope.runCatching {
                    trySendBlocking(
                        AccelTrackerMessage.TrackerErrorMessage(trackerError.toString())
                    )
                }
            }
        }

        accelTracker = healthTrackingService!!.getHealthTracker(trackingType)
        setListener(listener)


        awaitClose {
            accelTracker?.unsetEventListener()
            listenerSet = false
        }
    }

    private fun setListener(listener: HealthTracker.TrackerEventListener) {
        if (!listenerSet) {
            accelTracker?.setEventListener(listener)
            listenerSet = true
            Log.d(TAG, "Requested trackerType = $trackingType")
        }
    }

    override fun hasCapabilities(): Boolean {
        Log.i(TAG, "hasCapabilities()")
        healthTrackingService = healthTrackingServiceConnection.getHealthTrackingService()
        val trackers = healthTrackingService?.trackingCapability?.supportHealthTrackerTypes
        return trackers?.contains(trackingType) == true
    }
}

sealed class AccelTrackerMessage {
    data class DataMessage(val data: AccelData) : AccelTrackerMessage()
    object FlushCompletedMessage : AccelTrackerMessage()
    data class TrackerErrorMessage(val error: String) : AccelTrackerMessage()
}