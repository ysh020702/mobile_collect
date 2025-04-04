package com.example.collecthealthdata.ui.screen.tracked

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.collecthealthdata.domain.model.TrackedData
import com.example.collecthealthdata.domain.usecase.DeleteAllTrackedDataUseCase
import com.example.collecthealthdata.domain.usecase.GetTrackedDataUseCase
import com.example.collecthealthdata.domain.usecase.SaveTrackedDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrackedDataViewModel @Inject constructor(
    private val saveTrackedDataUseCase: SaveTrackedDataUseCase,
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
            saveTrackedDataUseCase(data)
        }
    }

    fun deleteAllTrackedData() {
        viewModelScope.launch {
            deleteAllTrackedDataUseCase()
        }
    }

}