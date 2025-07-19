package com.example.collecthealthdata.presentation.viewmodel

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.collecthealthdata.data.local.usecase.CheckDataCountUseCase
import com.example.collecthealthdata.data.remote.firebase.usecase.UploadDataToFirebaseUseCase
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named
import androidx.core.content.edit
import com.example.collecthealthdata.presentation.activity.AuthActivity.AuthActivity

private const val TAG = "MainViewModel"

@HiltViewModel
class MainViewModel @Inject constructor(
    private val uploadDataToFirebaseUseCase: UploadDataToFirebaseUseCase,
    private val checkDataCountUseCase: CheckDataCountUseCase
) : ViewModel(){

    private val _uploadResult = MutableSharedFlow<Boolean>() // true: 성공, false: 실패
    val uploadResult = _uploadResult.asSharedFlow()
    @Inject @Named("login_prefs") lateinit var loginPrefs : SharedPreferences
    @Inject @Named("user_prefs") lateinit var userPrefs : SharedPreferences

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

    fun logout(context: Context) {
        FirebaseAuth.getInstance().signOut()

        // 로그인 정보 초기화
        loginPrefs.edit { clear() }
        userPrefs.edit { clear() }

        Log.d(TAG, "로그아웃 완료")

        // AuthActivity로 이동
        val intent = Intent(context, AuthActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        context.startActivity(intent)
    }

}