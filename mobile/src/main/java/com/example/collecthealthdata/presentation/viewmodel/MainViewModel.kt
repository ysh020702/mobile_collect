package com.example.collecthealthdata.presentation.viewmodel

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.collecthealthdata.data.local.usecase.CheckDataCountUseCase
import com.example.collecthealthdata.data.remote.firebase.usecase.UploadDataToFirebaseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "MainViewModel"

@HiltViewModel
class MainViewModel @Inject constructor(
    private val uploadDataToFirebaseUseCase: UploadDataToFirebaseUseCase,
    private val checkDataCountUseCase: CheckDataCountUseCase
) : ViewModel(){

    private val _uploadResult = MutableSharedFlow<Boolean>() // true: 성공, false: 실패
    val uploadResult = _uploadResult.asSharedFlow()

    fun uploadDataToFirebase() {
        viewModelScope.launch {
            try {
                uploadDataToFirebaseUseCase()
                Log.d(TAG, "Data uploaded to Firebase successfully")
                _uploadResult.emit(true)
            } catch (e: Exception) {
                Log.e(TAG, "upload failed: ${e.message}")
                _uploadResult.emit(false)
            }
        }
    }

    fun showLocalDataCount(context: Context) {
        viewModelScope.launch {
            try {
                val count = checkDataCountUseCase()
                Toast.makeText(context, "로컬에 저장된 데이터 수: {$count} 개", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "데이터 수 조회 실패", Toast.LENGTH_SHORT).show()
                Log.e("MainViewModel", "Count fetch failed: ${e.message}")
            }
        }
    }

}