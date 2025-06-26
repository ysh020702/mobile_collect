package com.example.collecthealthdata.presentation

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.collecthealthdata.domain.local.TrackedDataEntity
import com.example.collecthealthdata.data.repositoryimpl.ConnectionMessage
import com.example.collecthealthdata.data.repositoryimpl.TrackerMessage
import com.example.collecthealthdata.domain.TrackedData
import com.example.collecthealthdata.domain.local.AccelData
import com.example.collecthealthdata.data.SpO2ResultStore
import com.example.collecthealthdata.data.repositoryimpl.AccelTrackerMessage
import com.example.collecthealthdata.data.repositoryimpl.HealthTrackingServiceConnection
import com.example.collecthealthdata.data.usecase.*
import com.example.collecthealthdata.data.usecase.roomDB.DeleteAllTrackedDataUseCase
import com.example.collecthealthdata.data.usecase.roomDB.GetTrackedDataUseCase
import com.example.collecthealthdata.data.usecase.roomDB.InsertTrackedDataUseCase
import com.samsung.android.service.health.tracking.HealthTrackerException
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime
import javax.inject.Inject
import kotlin.toString

private const val TAG = "MainViewModel"
private const val TRACKING_DURATION_LIMIT = 30

@HiltViewModel
class MainViewModel @OptIn(ExperimentalCoroutinesApi::class)
@Inject constructor(
    private val makeConnectionToHealthTrackingServiceUseCase: MakeConnectionToHealthTrackingServiceUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val stopTrackingUseCase: StopTrackingUseCase,
    private val areTrackingCapabilitiesAvailableUseCase: AreTrackingCapabilitiesAvailableUseCase,
    private val insertTrackedDataUseCase: InsertTrackedDataUseCase, //RoomDB
    private val getTrackedDataUseCase: GetTrackedDataUseCase,
    private val deleteAllTrackedDataUseCase: DeleteAllTrackedDataUseCase,
    private val resultStore: SpO2ResultStore,
    private val trackingUseCase: TrackingUseCase,
    private val accelerometerUseCase: AccelerometerUseCase,
    private val healthTrackingServiceConnection: HealthTrackingServiceConnection,
    @ApplicationContext context: Context
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


    private var cravingLevel = -1               //0~10사이의 값인데, -1이면 결측치
    private var vaping = false
    private var hrList = mutableListOf<Int>()
    private var ibiList = mutableListOf<Int>()
    private var accelList = mutableListOf<AccelData>()
    private var spo2Value = 0
    private var spo2MeasuredAt = 0L
    private var recentActivityLevel = 0f
    private var startTime: LocalDateTime? = null
    private var endTime: LocalDateTime? = null
    private val _stopSignal = MutableStateFlow(false)

    val stopSignal: StateFlow<Boolean> = _stopSignal



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
            makeConnectionToHealthTrackingServiceUseCase()
                .collect { connectionMessage ->
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

    private var trackingJob: Job? = null
    @OptIn(ExperimentalCoroutinesApi::class)
    fun startTracking(vapingState: Boolean, cravingLevelState: Int) {
        trackingJob?.cancel()
        Log.i(TAG, "startTracking()")

        trackingJob = viewModelScope.launch {
            healthTrackingServiceConnection.connectionFlow.collect { message ->
                when (message) {
                    is ConnectionMessage.ConnectionSuccessMessage -> {
                        Log.i(TAG, "HealthTrackingService connected. Starting sensor tracking...")

                        if (!areTrackingCapabilitiesAvailableUseCase()) {
                            _trackingState.value = TrackingState(
                                trackingRunning = false,
                                trackingError = true,
                                valueHR = "-",
                                valueIBI = arrayListOf(),
                                message = "Tracking capabilities not available"
                            )
                            return@collect
                        }

                        // 별도 스코프로 가속도 센서 시작
                        launch(SupervisorJob() + Dispatchers.Default) {
                            accelerometerUseCase().collect { trackerMessage ->
                                when (trackerMessage) {
                                    is AccelTrackerMessage.DataMessage -> {
                                        val acc = trackerMessage.data
                                        accelList.add(acc)
                                        //Log.d(TAG, "Acc: x=${acc.x}, y=${acc.y}, z=${acc.z}")
                                    }

                                    is AccelTrackerMessage.FlushCompletedMessage -> {
                                        Log.i(TAG, "ACC Tracker FlushCompleted")
                                    }

                                    is AccelTrackerMessage.TrackerErrorMessage -> {
                                        Log.e(TAG, "ACC Tracker Error: ${trackerMessage.error}")
                                    }
                                }
                            }
                        }

                        //  별도 스코프로 심박수 센서 시작
                        launch(SupervisorJob() + Dispatchers.Default) {
                            cravingLevel = cravingLevelState
                            vaping = vapingState
                            startTime = LocalDateTime.now()
                            hrList = arrayListOf()
                            ibiList = arrayListOf()

                            Log.i(TAG, "startTime updated $startTime")

                            trackingUseCase().collect { trackerMessage ->
                                when (trackerMessage) {
                                    is TrackerMessage.DataMessage -> {
                                        processExerciseUpdate(trackerMessage.trackedData)
                                        Log.i(TAG, "TrackerMessage.DataReceivedMessage")
                                    }

                                    is TrackerMessage.FlushCompletedMessage -> {
                                        _trackingState.value = TrackingState(
                                            trackingRunning = false,
                                            trackingError = false,
                                            valueHR = "-",
                                            valueIBI = arrayListOf(),
                                            message = ""
                                        )
                                    }

                                    is TrackerMessage.TrackerErrorMessage -> {
                                        _trackingState.value = TrackingState(
                                            trackingRunning = false,
                                            trackingError = true,
                                            valueHR = "-",
                                            valueIBI = arrayListOf(),
                                            message = trackerMessage.trackerError
                                        )
                                    }

                                    is TrackerMessage.TrackerWarningMessage -> {
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
                    }

                    is ConnectionMessage.ConnectionFailedMessage -> {
                        Log.e(TAG, "Health service connection failed: ${message.exception}")
                        _trackingState.value = TrackingState(
                            trackingRunning = false,
                            trackingError = true,
                            valueHR = "-",
                            valueIBI = arrayListOf(),
                            message = "Health Tracking Service connection failed"
                        )
                    }

                    is ConnectionMessage.ConnectionEndedMessage -> {
                        Log.w(TAG, "Health service connection ended.")
                    }
                }
            }
        }
    }




    private fun processExerciseUpdate(trackedData: TrackedData) {
        //TODO: 여기가 실제 TrackedData처리되는 구간!! 여기서 데이터베이스 넣는 로직
        //TrackedData- Domain.Model.TrackedData
        val hr = trackedData.hr
        val ibi = trackedData.ibi
        Log.i(TAG, "last HeartRate: $hr, last IBI: $ibi")
        currentHR = hr.toString()
        currentIBI = ibi

        // HR 값 누적 저장 (정상값만)
        if (hr > 0) {
            hrList.add(hr)
        }
        //ibi 값 누적 저장
        ibiList.addAll(currentIBI)

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
            //TODO: 위 3개의 값을 앱에 저장된 대로 받아올 것!!!
            spo2Value = 0
            spo2MeasuredAt = 0L
            recentActivityLevel = 0f

            val(spo2MeasuredAt, spo2Value) = resultStore.loadSpO2()
            val accelDataString = accelList.joinToString(";") {
                "${it.x},${it.y},${it.z}"
            }

            //데이터를 저장
            endTime = LocalDateTime.now()
            val trackedEntity = TrackedDataEntity(
                vaping = vaping,
                cravingLevel = cravingLevel,
                hrDataString = hrList.joinToString(","), // e.g. "75,77,80,..."
                ibiDataString = ibiList.joinToString(","),
                accelDataString = accelDataString,
                spo2Value = spo2Value,
                spo2MeasuredAt = spo2MeasuredAt,
                recentActivityLevel = recentActivityLevel,
                startTime = startTime.toString(),
                endTime = endTime.toString()
            )

            saveDataAndStop(trackedEntity)
        }
    }

    private fun saveDataAndStop(entity: TrackedDataEntity) {
        viewModelScope.launch {
            insertTrackedDataUseCase(entity)
            cravingLevel = -1
            vaping = false
            hrList.clear()
            ibiList.clear()
            accelList.clear()
            startTime = LocalDateTime.now() // 새 추적 세션 시작 시간 초기화
            _stopSignal.value = true

            delay(100) // 살짝 delay 주고
            _stopSignal.value = false // 다시 false로 리셋        }
        }

        viewModelScope.launch{
            sendMessage()
        }
    }

    fun sendMessage() {
        viewModelScope.launch {
            if (sendMessageUseCase()) {
                _messageSentToast.emit(true)
            } else {
                _messageSentToast.emit(false)
            }
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
