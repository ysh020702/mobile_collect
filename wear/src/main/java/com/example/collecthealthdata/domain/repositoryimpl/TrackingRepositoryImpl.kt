package com.example.collecthealthdata.domain.repositoryimpl

import android.content.Context
import android.util.Log
import com.example.collecthealthdata.R
import com.example.collecthealthdata.data.IBIDataParsing.Companion.getValidIbiList
import com.example.collecthealthdata.data.TrackedData
import com.example.collecthealthdata.data.repository.TrackingRepository
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

private const val TAG = "TrackingRepositoryImpl"

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class TrackingRepositoryImpl
@Inject constructor(
    private val coroutineScope: CoroutineScope,
    private val healthTrackingServiceConnection: HealthTrackingServiceConnection,
    @ApplicationContext private val context: Context,
) : TrackingRepository {

    private val trackingType = HealthTrackerType.HEART_RATE_CONTINUOUS
    private var listenerSet = false
    private var healthTrackingService: HealthTrackingService? = null

    var errors: HashMap<String, Int> = hashMapOf(
        "0" to R.string.error_initial_state,
        "-2" to R.string.error_wearable_movement_detected,
        "-3" to R.string.error_wearable_detached,
        "-8" to R.string.error_low_ppg_signal,
        "-10" to R.string.error_low_ppg_signal_even_more,
        "-999" to R.string.error_other_sensor_running,
        "SDK_POLICY_ERROR" to R.string.SDK_POLICY_ERROR,
        "PERMISSION_ERROR" to R.string.PERMISSION_ERROR
    )


    private val maxValuesToKeep = 40
    private var heartRateTracker: HealthTracker? = null
    private var validHrData = ArrayList<TrackedData>()

    override fun getValidHrData(): ArrayList<TrackedData> {
        return validHrData
    }

    private fun isHRValid(hrStatus: Int): Boolean {
        return hrStatus == 1
    }

    private fun trimDataList() {
        val howManyElementsToRemove = validHrData.size - maxValuesToKeep
        repeat(howManyElementsToRemove) { validHrData.removeFirstOrNull() }
    }

    /*
    TODO: 추가해야 될 것:
    private val trackingType = HealthTrackerType.ACCELEROMETER_CONTINUOUS
    private val trackingType = HealthTrackerType.PPG_CONTINUOUS -> 심박수와 관계 있긴 함, 일단 Green 데이터만 넣어둘 것
    private val trackingType = HealthTrackerType.SPO2_ON_DEMAND


    혈중산소농도는 측정하기 버튼 눌러서 측정해두고, 앱에 최근 측정 결과를 저장.
    데이터 저장 시에 저장된 값이 없거나, 1일이 지난 데이터이면 0으로 넣기(결손값NaN)
    1일 안에 측정된 데이터이면 그냥 앱에 저장된 데이터로 넣기

    가속도는, 최근 1시간 안에 격한 움직임의 정도를 0~1사이의 숫자로 표현하여 (퍼지 논리 응용)
    앱에 최근 결과를 저장,
    heartRate를 측정하고 DB에 넣을 때, 앱에 저장된 값을 읽어와 반영

     private val trackingType = HealthTrackerType.BIA_ON_DEMAND(측정하기 눌렀을 때 딱 한번만) -> 이거못함 절대못함 ㅅㅂ,,,
     */
    @ExperimentalCoroutinesApi
    override suspend fun track(): Flow<TrackerMessage> = callbackFlow {
        val updateListener = object : HealthTracker.TrackerEventListener {
            override fun onDataReceived(dataPoints: MutableList<DataPoint>) {

                for (dataPoint in dataPoints) {

                    var trackedData: TrackedData? = null

                    //추적한 dataPoint 에서 data parsing
                    val hrValue = dataPoint.getValue(ValueKey.HeartRateSet.HEART_RATE)
                    val hrStatus = dataPoint.getValue(ValueKey.HeartRateSet.HEART_RATE_STATUS)

                    if (isHRValid(hrStatus)) {
                        trackedData = TrackedData()
                        trackedData.hr = hrValue
                        Log.i(TAG, "valid HR: $hrValue")
                    } else {
                        coroutineScope.runCatching {
                            trySendBlocking(TrackerMessage.TrackerWarningMessage(getError(hrStatus.toString())))
                        }
                    }

                    val validIbiList = getValidIbiList(dataPoint)
                    if (validIbiList.isNotEmpty()) {
                        if (trackedData == null) trackedData = TrackedData()
                        trackedData.ibi.addAll(validIbiList)
                    }

                    if ((isHRValid(hrStatus) || validIbiList.isNotEmpty()) && trackedData != null) {
                        coroutineScope.runCatching {
                            trySendBlocking(TrackerMessage.DataMessage(trackedData))
                        }
                    }
                    if (trackedData != null) {
                        validHrData.add(trackedData)
                    }
                }
                trimDataList()
            }

            fun getError(errorKeyFromTracker: String): String {
                val str = errors.getValue(errorKeyFromTracker)
                return context.resources.getString(str)
            }

            override fun onFlushCompleted() {
                Log.i(TAG, "onFlushCompleted()")
                coroutineScope.runCatching {
                    trySendBlocking(TrackerMessage.FlushCompletedMessage)
                }
            }

            override fun onError(trackerError: HealthTracker.TrackerError?) {
                Log.i(TAG, "onError()")
                coroutineScope.runCatching {
                    trySendBlocking(TrackerMessage.TrackerErrorMessage(getError(trackerError.toString())))
                }
            }
        }

        
        //TODO: heartRateTracker 로부터, trackingType에 대한 트래커를 가져옴
        heartRateTracker =
            healthTrackingService!!.getHealthTracker(trackingType)

        //TODO: heartRateTracker에 updateListener를 가져옴 -> 한 번에 trackingType에 대한 데이터만 가져올수 있음
        setListener(updateListener)

        awaitClose {
            Log.i(TAG, "Tracking flow awaitClose()")
            stopTracking()
        }
    }

    override fun stopTracking() {
        unsetListener()
    }

    private fun unsetListener() {
        if (listenerSet) {
            heartRateTracker?.unsetEventListener()
            listenerSet = false
        }
    }

    private fun setListener(listener: HealthTracker.TrackerEventListener) {
        if (!listenerSet) {
            heartRateTracker?.setEventListener(listener)
            listenerSet = true
        }
    }

    override fun hasCapabilities(): Boolean {
        Log.i(TAG, "hasCapabilities()")
        healthTrackingService = healthTrackingServiceConnection.getHealthTrackingService()
        val trackers: List<HealthTrackerType> =
            healthTrackingService!!.trackingCapability.supportHealthTrackerTypes
        return trackers.contains(trackingType)
    }
}

sealed class TrackerMessage {
    class DataMessage(val trackedData: TrackedData) : TrackerMessage()
    object FlushCompletedMessage : TrackerMessage()
    class TrackerErrorMessage(val trackerError: String) : TrackerMessage()
    class TrackerWarningMessage(val trackerWarning: String) : TrackerMessage()
}
