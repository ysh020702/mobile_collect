package com.example.collecthealthdata.ui.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.collecthealthdata.domain.model.TrackedData
import com.example.collecthealthdata.domain.usecase.DeleteAllTrackedDataUseCase
import com.example.collecthealthdata.domain.usecase.GetTrackedDataUseCase
import com.example.collecthealthdata.domain.usecase.InsertTrackedDataUseCase
import com.samsung.android.service.health.tracking.HealthTrackerException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val insertTrackedDataUseCase: InsertTrackedDataUseCase,
    private val getTrackedDataUseCase: GetTrackedDataUseCase,
    private val deleteAllTrackedDataUseCase: DeleteAllTrackedDataUseCase
): ViewModel() {
    private val _trackedData = MutableStateFlow<List<TrackedData>>(emptyList())
    val trackedData: StateFlow<List<TrackedData>> = _trackedData.asStateFlow()

    init{
        loadTrackedData()
    }

    fun loadTrackedData() {
        viewModelScope.launch {
            getTrackedDataUseCase().collect {
                _trackedData.value = it
            }
        }
    }

    fun insertTrackedData(data: TrackedData) {
        viewModelScope.launch {
            //HR 데이터 String 으로 만들어서 저장하기
            TODO()
        }
    }

    fun deleteAllTrackedData() {
        viewModelScope.launch {
            deleteAllTrackedDataUseCase()
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
