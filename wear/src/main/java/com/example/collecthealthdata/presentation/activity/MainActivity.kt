package com.example.collecthealthdata.presentation.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.example.collecthealthdata.R
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.collecthealthdata.presentation.MainViewModel
import com.example.collecthealthdata.presentation.SpO2ViewModel
import com.example.collecthealthdata.presentation.screen.MainScreen
import com.example.collecthealthdata.presentation.screen.Permission
import com.example.collecthealthdata.presentation.screen.SpO2Screen
import dagger.hilt.android.AndroidEntryPoint

private const val TAG = "MainActivity"
/*
wear os 사용하면서 느낀 점..
안드로이드 워치 앱 개발은 많은 걸 하면 안 됨..
성능 개 느리고
부팅해서 와이파이 연결하는 데만 배터리 2프로 잡아먹음

그냥 OS자체가 이 워치의 낮은 성능을 받쳐주지 못하는 느낌
앱에서 뭘 하는 순간 배터리 타임이 확 짧아짐
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val trackingState by viewModel.trackingState.collectAsStateWithLifecycle()
            val connectionState by viewModel.connectionState.collectAsStateWithLifecycle()
            val stopSignal by viewModel.stopSignal.collectAsStateWithLifecycle()
            val navController = rememberNavController()

            if (trackingState.trackingRunning) {
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
            LaunchedEffect(Unit) {
                viewModel
                    .messageSentToast
                    .collect { message ->
                        Toast.makeText(
                            applicationContext,
                            if (message) R.string.sending_success else R.string.sending_failed,
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
            }
            Log.i(
                TAG, "connected: ${connectionState.connected}, " +
                        "message: ${connectionState.message}, " +
                        "connectionException: ${connectionState.connectionException}"
            )
            connectionState.connectionException?.resolve(this)
            Permission {
                NavHost(navController = navController, startDestination = "main") {
                    composable("main") {
                        MainScreen(
                            connected = connectionState.connected,
                            connectionMessage = connectionState.message,
                            trackingRunning = trackingState.trackingRunning,
                            trackingError = trackingState.trackingError,
                            trackingMessage = trackingState.message,
                            valueHR = trackingState.valueHR,
                            valueIBI = trackingState.valueIBI,
                            onStart = { craving ->
                                viewModel.startTracking(craving)
                                Log.i(TAG, "startTracking($craving)")
                            },
                            onStop = {
                                viewModel.stopTracking()
                                Log.i(TAG, "stopTracking()")
                            },
                            onSend = {
                                viewModel.sendMessage()
                                Log.i(TAG, "sendMessage()")
                            },
                            onSpO2Click = {
                                navController.navigate("spo2")
                            },
                            stopSignal = stopSignal
                        )
                    }

                    composable("spo2") {
                        val spo2ViewModel: SpO2ViewModel = hiltViewModel()
                        SpO2Screen(viewModel = spo2ViewModel) {
                            navController.popBackStack()
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!viewModel.connectionState.value.connected) {
            viewModel.setUpTracking()
        }
    }
}