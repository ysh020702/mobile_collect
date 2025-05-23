package com.example.collecthealthdata.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.collecthealthdata.domain.SpO2Status
import com.example.collecthealthdata.data.SpO2ResultStore
import com.example.collecthealthdata.data.repositoryimpl.HealthTrackingServiceConnection
import com.samsung.android.service.health.tracking.HealthTracker
import com.samsung.android.service.health.tracking.HealthTrackingService
import com.samsung.android.service.health.tracking.data.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "SpO2ViewModel"

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SpO2ViewModel @Inject constructor(
    private val healthTrackingServiceConnection: HealthTrackingServiceConnection,
    private val resultStore: SpO2ResultStore
) : ViewModel() {

    private var spo2Tracker: HealthTracker? = null
    private var healthTrackingService: HealthTrackingService? = null
    private val listener = object : HealthTracker.TrackerEventListener {
        override fun onDataReceived(list: List<DataPoint>) {
            list.forEach { updateSpo2(it) }
        }

        override fun onFlushCompleted() {}
        override fun onError(trackerError: HealthTracker.TrackerError) {
            // handle errors (optional)
        }
    }

    private val _uiState = MutableStateFlow(SpO2UiState())
    val uiState: StateFlow<SpO2UiState> = _uiState.asStateFlow()

    //MainActivity 의 onResume 에서 setupTracking 이 실행됨 ->
    //healthTrackingService는 항상 실행되는 상태
    private var measurementTimeoutJob: Job? = null

    fun startMeasurement() {
        healthTrackingService = healthTrackingServiceConnection.getHealthTrackingService()
        val service = healthTrackingService
        if (service == null) {
            Log.e(TAG, "HealthTrackingService is not connected")
            return
        }
        spo2Tracker = service.getHealthTracker(HealthTrackerType.SPO2_ON_DEMAND)
        spo2Tracker?.setEventListener(listener)
        _uiState.update { it.copy(measuring = true, measurementCompleted = false) }

        // 14초 타임아웃 타이머 시작
        measurementTimeoutJob?.cancel()  // 이전 타이머 취소
        measurementTimeoutJob = viewModelScope.launch {
            delay(35_000L)  // 35초
            if (_uiState.value.measuring) {
                Log.d(TAG, "측정 타임아웃 발생")
                _uiState.update { it.copy(measuring = false) }
                // 저장 없이 측정 종료만
                stopMeasurement(false)
            }
        }
    }

    fun stopMeasurement(status : Boolean) {
        spo2Tracker?.unsetEventListener()
        measurementTimeoutJob?.cancel()
        measurementTimeoutJob = null

        if (status){
            _uiState.update { it.copy(measuring = false) }
        }
        else{
            _uiState.update { it.copy(measuring = false, measurementFailed = true) }
        }

    }

    private fun updateSpo2(data: DataPoint) {
        val status = data.getValue(ValueKey.SpO2Set.STATUS)

        when(status) {
            SpO2Status.LOW_SIGNAL -> Log.d(TAG, "Status: LOW_SIGNAL")
            SpO2Status.DEVICE_MOVING -> Log.d(TAG, "Status: DEVICE_MOVING")
            SpO2Status.INITIAL_STATUS -> Log.d(TAG, "Status: INITIAL_STATUS")
            SpO2Status.CALCULATING -> Log.d(TAG, "Status: CALCULATING")
            SpO2Status.MEASUREMENT_COMPLETED -> {
                Log.d(TAG, "Status: MEASUREMENT_COMPLETED")
                val spo2Value = data.getValue(ValueKey.SpO2Set.SPO2)
                val timestamp = System.currentTimeMillis()

                //저장!!!
                resultStore.saveSpO2(timestamp, spo2Value)
                // 측정 완료 시 타이머 취소
                measurementTimeoutJob?.cancel()

                _uiState.update {
                    it.copy(spo2 = spo2Value, measurementCompleted = true, measuring = false)
                }

                stopMeasurement(true)
            }
            else -> stopMeasurement(false)

        }
    }
}

data class SpO2UiState(
    val measuring: Boolean = false,
    val spo2: Int = 0,
    val measurementCompleted: Boolean = false,
    val measurementFailed: Boolean = false
)
