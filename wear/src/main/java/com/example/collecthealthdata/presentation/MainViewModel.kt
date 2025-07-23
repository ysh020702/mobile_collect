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
import kotlinx.coroutines.sync.withLock
import java.time.Duration
import java.time.LocalDateTime
import java.util.Collections
import javax.inject.Inject
import kotlin.toString

private const val TAG = "MainViewModel"
private const val INTERVAL_SEC = 2_000L
private const val MAX_REPEAT = 450  //15분

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
    private var accelList = Collections.synchronizedList(mutableListOf<AccelData>())
    private var hrList = Collections.synchronizedList(mutableListOf<Int>())
    private var ibiList = Collections.synchronizedList(mutableListOf<Int>())
    private val listMutex: kotlinx.coroutines.sync.Mutex = kotlinx.coroutines.sync.Mutex()
    //private var spo2Value = 0
    //private var spo2MeasuredAt = 0L
    private var recentActivityLevel = 0f
    private var startTime: LocalDateTime? = null
    private var endTime: LocalDateTime? = null
    private val _stopSignal = MutableStateFlow(false)

    val stopSignal: StateFlow<Boolean> = _stopSignal



    private var currentHR = "-"
    private var currentIBI = ArrayList<Int>(4)



    fun setUpTracking() {
        Log.i(TAG, "setUpTracking()")
        viewModelScope.launch(Dispatchers.IO) {
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
    private var isConnecting = false // 중복 클릭 방지 플래그

    @OptIn(ExperimentalCoroutinesApi::class)
    fun startTracking(vapingState: Boolean, cravingLevelState: Int) {

        isConnecting = true
        Log.i(TAG, "startTracking()")

        trackingJob?.cancel()
        trackingJob = viewModelScope.launch(Dispatchers.IO) {
            Log.d("trackingJob", "launched()")
            try {
                //HealthTracking Service 는 setUpTracking 에서 Connected

                Log.i(TAG, "HealthTrackingService connected. Starting sensor tracking...")

                // 2️⃣ Capability 체크 (연결 이후에)
                if (!areTrackingCapabilitiesAvailableUseCase()) {
                    _trackingState.value = TrackingState(
                        trackingRunning = false,
                        trackingError = true,
                        valueHR = "-",
                        valueIBI = arrayListOf(),
                        message = "Tracking capabilities not available"
                    )
                    return@launch
                }

                startSavingLoop()

                // 3️⃣ 가속도 센서 시작
                launch(SupervisorJob() + Dispatchers.Default) {
                    accelList = Collections.synchronizedList(mutableListOf())
                    accelerometerUseCase().collect { trackerMessage ->
                        when (trackerMessage) {
                            is AccelTrackerMessage.DataMessage -> {
                                val acc = trackerMessage.data
                                listMutex.withLock { accelList.add(acc) }
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

                // 4️⃣ 심박수 센서 시작
                launch(SupervisorJob() + Dispatchers.IO) {
                    cravingLevel = cravingLevelState
                    vaping = vapingState
                    startTime = LocalDateTime.now()
                    hrList = Collections.synchronizedList(mutableListOf())
                    ibiList = Collections.synchronizedList(mutableListOf())

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


                _trackingState.value = _trackingState.value.copy(trackingRunning = true)
            } finally {
                isConnecting = false
            }
        }
    }


    private suspend fun processExerciseUpdate(trackedData: TrackedData) {
        //Hr, IBI 처리 로직
        val hr = trackedData.hr
        val ibi = trackedData.ibi
        Log.i(TAG, "last HeartRate: $hr, last IBI: $ibi")
        currentHR = hr.toString()
        currentIBI = ibi

        // HR 값 누적 저장 (정상값만)
        listMutex.withLock {
            if (hr > 0) {
                hrList.add(hr)
            }
            ibiList.addAll(currentIBI)
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
    }

    private var savingJob: Job? = null
    private fun startSavingLoop() {
        
        savingJob?.cancel() // 이미 실행 중인 루프가 있으면 취소
        savingJob = viewModelScope.launch(Dispatchers.IO) {
            var skipped = 0
            repeat(MAX_REPEAT) { count ->
                delay(INTERVAL_SEC) // 측정 대기

                if (accelList.isEmpty() || hrList.isEmpty() ) {
                    
                    skipped += 1
                    if(skipped > 50) {
                        //한 데이터가 계속 측정되지 않고 있는 경우, 초기화
                        listMutex.withLock {
                            hrList.clear()
                            ibiList.clear()
                            accelList.clear()
                        }
                        startTime = LocalDateTime.now() // 시작시간 초기화
                        Log.w(TAG, "${count + 1}번째 저장 스킵 및 데이터 초기화. 데이터 누락 지속 발생")
                    }else{
                        Log.w(TAG, "${count + 1}번째 저장 스킵: 데이터 없음")
                    }
                    
                    
                    return@repeat
                }
                skipped = 0
                // 저장할 데이터가 있으므로 시간 갱신
                if (startTime == null) {
                    startTime = LocalDateTime.now() // 첫 저장 시작시간
                }
                endTime = LocalDateTime.now() // 마지막 저장 끝시간

                val (spo2MeasuredAt, spo2Value) = resultStore.loadSpO2()

                val accelDataString: String
                val hrDataString: String
                val ibiDataString: String

                listMutex.withLock {
                    accelDataString = accelList.joinToString(";") { "${it.x},${it.y},${it.z}" }
                    hrDataString = hrList.joinToString(",")
                    ibiDataString = ibiList.joinToString(",")
                }

                val trackedEntity = TrackedDataEntity(
                    vaping = vaping,
                    cravingLevel = cravingLevel,
                    hrDataString = hrDataString,
                    ibiDataString = ibiDataString,
                    accelDataString = accelDataString,
                    spo2Value = spo2Value,
                    spo2MeasuredAt = spo2MeasuredAt,
                    recentActivityLevel = recentActivityLevel,
                    startTime = startTime.toString(),
                    endTime = endTime.toString()
                )

                saveData(trackedEntity)
                Log.d(TAG, "${count + 1}번째 저장 완료")

                // 누적 리스트 초기화
                listMutex.withLock {
                    hrList.clear()
                    ibiList.clear()
                    accelList.clear()
                }
            }


            Log.d(TAG, "자동 업로드 트리거 완료")
            stopTracking()
        }
    }


    fun stopTracking() {
        stopTrackingUseCase() //이건 listener unset 밖에 없다 -> awaitClose로 이미 unsetted 되어야 되는데 안 되고 있어서 명시적으로 unset
        trackingJob?.cancel() // 센서 수집 Job 취소
        savingJob?.cancel()   // 저장 루프 Job도 취소
        _trackingState.value = TrackingState(
            trackingRunning = false,
            trackingError = false,
            valueHR = "-",
            valueIBI = arrayListOf(),
            message = ""
        )
        uploadAfterAllSaved()
    }




    private fun saveData(entity: TrackedDataEntity) {
        viewModelScope.launch {
            insertTrackedDataUseCase(entity)
            Log.d(TAG, "Saved measurement.")
        }
    }

    private fun uploadAfterAllSaved() {
        viewModelScope.launch {
            startTime = LocalDateTime.now() // 새 추적 세션 시작 시간 초기화
            _stopSignal.value = true

            delay(100) // 살짝 delay 주고
            _stopSignal.value = false // 다시 false로 리셋
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
