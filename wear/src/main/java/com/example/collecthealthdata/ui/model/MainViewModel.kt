package com.example.collecthealthdata.ui.model

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.collecthealthdata.data.repository.ConnectionMessage
import com.example.collecthealthdata.data.repository.TrackerMessage
import com.example.collecthealthdata.domain.model.TrackedData
import com.example.collecthealthdata.domain.usecase.*
import com.samsung.android.service.health.tracking.HealthTrackerException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.compareTo
import kotlin.toString

private const val TAG = "MainViewModel"

@HiltViewModel
class MainViewModel @Inject constructor(
    private val makeConnectionToHealthTrackingServiceUseCase: MakeConnectionToHealthTrackingServiceUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val stopTrackingUseCase: StopTrackingUseCase,
    private val areTrackingCapabilitiesAvailableUseCase: AreTrackingCapabilitiesAvailableUseCase,
    private val insertTrackedDataUseCase: InsertTrackedDataUseCase, //RoomDB
    private val getTrackedDataUseCase: GetTrackedDataUseCase,
    private val deleteAllTrackedDataUseCase: DeleteAllTrackedDataUseCase
): ViewModel() {


    private val _messageSentToast = MutableSharedFlow<Boolean>()
    val messageSentToast = _messageSentToast.asSharedFlow()

    private val _trackingState =
        MutableStateFlow(
            TrackingState(
                trackingRunning = false,
                trackingError = false,
                valueHR = "-",
                valueIBI = arrayListOf(),
                message = ""
            )
        )
    val trackingState: StateFlow<TrackingState> = _trackingState

    private val _connectionState =
        MutableStateFlow(ConnectionState(connected = false, message = "", null))
    val connectionState: StateFlow<ConnectionState> = _connectionState

    @Inject
    lateinit var trackingUseCase: TrackingUseCase

    private var currentHR = "-"
    private var currentIBI = ArrayList<Int>(4)

    fun stopTracking() {
        stopTrackingUseCase()
        trackingJob?.cancel()
        _trackingState.value = TrackingState(
            trackingRunning = false,
            trackingError = false,
            valueHR = "-",
            valueIBI = arrayListOf(),
            message = ""
        )
    }

    fun setUpTracking() {
        Log.i(TAG, "setUpTracking()")
        viewModelScope.launch {
            makeConnectionToHealthTrackingServiceUseCase().collect { connectionMessage ->
                Log.i(TAG, "makeConnectionToHealthTrackingServiceUseCase().collect")
                when (connectionMessage) {
                    is ConnectionMessage.ConnectionSuccessMessage -> {
                        Log.i(TAG, "ConnectionMessage.ConnectionSuccessMessage")
                        _connectionState.value = ConnectionState(
                            connected = true,
                            message = "Connected to Health Tracking Service",
                            connectionException = null
                        )
                    }

                    is ConnectionMessage.ConnectionFailedMessage -> {
                        Log.i(TAG, "Connection: Sth went wrong")
                        _connectionState.value = ConnectionState(
                            connected = false,
                            message = "Connection to Health Tracking Service failed",
                            connectionException = connectionMessage.exception
                        )
                    }

                    is ConnectionMessage.ConnectionEndedMessage -> {
                        Log.i(TAG, "Connection ended")
                        _connectionState.value = ConnectionState(
                            connected = false,
                            message = "Connection ended. Try again later",
                            connectionException = null
                        )
                    }
                }
            }
        }
    }

    //TODO: SendMessage -> 실행 조건 바꾸기!!! SEND 버튼 눌렀을 때로!! ROOM에서 있는 데이터베이스가 맞는지 확인
    fun sendMessage() {
        viewModelScope.launch {
            if (sendMessageUseCase()) {
                _messageSentToast.emit(true)
            } else {
                _messageSentToast.emit(false)
            }
        }
    }

    private fun processExerciseUpdate(trackedData: TrackedData) {

        val hr = trackedData.hr
        val ibi = trackedData.ibi
        Log.i(TAG, "last HeartRate: $hr, last IBI: $ibi")
        currentHR = hr.toString()
        currentIBI = ibi

        _trackingState.value = TrackingState(
            trackingRunning = true,
            trackingError = false,
            valueHR = if (hr > 0) hr.toString() else "-",
            valueIBI = ibi,
            message = ""
        )
    }

    private var trackingJob: Job? = null

    fun startTracking() {
        trackingJob?.cancel()
        Log.i(TAG, "startTracking()")
        if (areTrackingCapabilitiesAvailableUseCase()) {
            trackingJob = viewModelScope.launch {
                trackingUseCase().collect { trackerMessage ->
                    when (trackerMessage) {
                        is TrackerMessage.DataMessage -> {
                            processExerciseUpdate(trackerMessage.trackedData)
                            Log.i(TAG, "TrackerMessage.DataReceivedMessage")
                        }

                        is TrackerMessage.FlushCompletedMessage -> {
                            Log.i(TAG, "TrackerMessage.FlushCompletedMessage")
                            _trackingState.value = TrackingState(
                                trackingRunning = false,
                                trackingError = false,
                                valueHR = "-",
                                valueIBI = arrayListOf(),
                                message = ""
                            )
                        }

                        is TrackerMessage.TrackerErrorMessage -> {
                            Log.i(TAG, "TrackerMessage.TrackerErrorMessage")
                            _trackingState.value = TrackingState(
                                trackingRunning = false,
                                trackingError = true,
                                valueHR = "-",
                                valueIBI = arrayListOf(),
                                message = trackerMessage.trackerError
                            )
                        }

                        is TrackerMessage.TrackerWarningMessage -> {
                            Log.i(TAG, "TrackerMessage.TrackerWarningMessage")
                            _trackingState.value = TrackingState(
                                trackingRunning = true,
                                trackingError = false,
                                valueHR = "-",
                                valueIBI = currentIBI,
                                message = trackerMessage.trackerWarning
                            )
                        }
                    }
                }
            }
        } else {
            _trackingState.value = TrackingState(
                trackingRunning = false,
                trackingError = true,
                valueHR = "-",
                valueIBI = arrayListOf(),
                message = "HR tracking capabilities not available"
            )
        }
    }



}

data class ConnectionState(
    val connected: Boolean,
    val message: String,
    val connectionException: HealthTrackerException?
)

data class TrackingState(
    val trackingRunning: Boolean,
    val trackingError: Boolean,
    val valueHR: String,
    val valueIBI: ArrayList<Int>,
    val message: String
)
