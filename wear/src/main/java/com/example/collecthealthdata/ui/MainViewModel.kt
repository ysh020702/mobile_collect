package com.example.collecthealthdata.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.collecthealthdata.data.local.TrackedDataEntity
import com.example.collecthealthdata.domain.repositoryimpl.ConnectionMessage
import com.example.collecthealthdata.domain.repositoryimpl.TrackerMessage
import com.example.collecthealthdata.data.TrackedData
import com.example.collecthealthdata.domain.usecase.*
import com.example.collecthealthdata.domain.usecase.roomDB.DeleteAllTrackedDataUseCase
import com.example.collecthealthdata.domain.usecase.roomDB.GetTrackedDataUseCase
import com.example.collecthealthdata.domain.usecase.roomDB.InsertTrackedDataUseCase
import com.samsung.android.service.health.tracking.HealthTrackerException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
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
    //Set up Our Invironment
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


    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    private var hrList = ArrayList<Int>()
    private var startTime: LocalDateTime? = null
    private var endTime: LocalDateTime? = null
    private val _stopSignal = MutableStateFlow(false)
    private val TRACKING_DURATION_LIMIT = 30
    val stopSignal: StateFlow<Boolean> = _stopSignal


    @Inject
    lateinit var trackingUseCase: TrackingUseCase

    private var currentHR = "-"
    private var currentIBI = ArrayList<Int>(4)

    fun stopTracking() {
        stopTrackingUseCase() //이건 listener unset 밖에 없다
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

    fun sendMessage() {
        //TODO: SendMessage -> 실행 조건 바꾸기!!! SEND 버튼 눌렀을 때로!! ROOM에서 있는 데이터베이스가 맞는지 확인
        viewModelScope.launch {
            if (sendMessageUseCase()) {
                _messageSentToast.emit(true)
            } else {
                _messageSentToast.emit(false)
            }
        }
    }


    private var trackingJob: Job? = null
    fun startTracking() {
        trackingJob?.cancel()
        Log.i(TAG, "startTracking()")
        if (areTrackingCapabilitiesAvailableUseCase()) {
            trackingJob = viewModelScope.launch {
                //측정 시작 시간 저장 및 HRlist초기화
                startTime = LocalDateTime.now()
                hrList = ArrayList<Int>()

                trackingUseCase().collect { trackerMessage ->
                    when (trackerMessage) {
                        //여기서부터 실제 트래킹 시작, 한 데이터포인트마다 trackerMessage가 들어
                        is TrackerMessage.DataMessage -> {
                            //제대로 들어온 데이터이면
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

    private fun processExerciseUpdate(trackedData: TrackedData) {
        //TODO: 여기가 실제 TrackedData처리되는 구간!! 여기서 데이터베이스 넣는 로직
        //TrackedData- Domain.Model.TrackedData
        //HRString에 쌓인 값이 40개가 되면 특정 값을 리턴시켜서 MainScreen 에서 onStop이 실행되게 헤야 함
        val hr = trackedData.hr
        val ibi = trackedData.ibi
        Log.i(TAG, "last HeartRate: $hr, last IBI: $ibi")
        currentHR = hr.toString()
        currentIBI = ibi

        // HR 값 누적 저장 (정상값만)
        if (hr > 0) {
            hrList.add(hr)
        }

        _trackingState.value = TrackingState(
            trackingRunning = true,
            trackingError = false,
            valueHR = if (hr > 0) hr.toString() else "-",
            valueIBI = ibi,
            message = ""
        )

        val now = LocalDateTime.now()
        val duration = Duration.between(startTime, now)
        if (duration.seconds >= TRACKING_DURATION_LIMIT) {
            //데이터를 저장
            endTime = LocalDateTime.now()
            val trackedEntity = TrackedDataEntity(
                hrDataString = hrList.joinToString(","), // e.g. "75,77,80,..."
                //ibiDataString = currentIBI.joinToString(","), //
                startTime = startTime.toString(),
                endTime = endTime.toString()
            )

            saveDataAndStop(trackedEntity)
        }
    }

    private fun saveDataAndStop(entity: TrackedDataEntity) {
        viewModelScope.launch {
            insertTrackedDataUseCase(entity)
            hrList.clear()
            _stopSignal.value = true
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
