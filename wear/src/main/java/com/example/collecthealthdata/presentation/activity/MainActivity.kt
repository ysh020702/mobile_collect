package com.example.collecthealthdata.presentation.activity

import android.Manifest
import android.app.Activity
import androidx.compose.runtime.getValue
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.collecthealthdata.R
import com.example.collecthealthdata.presentation.MainViewModel
import com.example.collecthealthdata.presentation.SpO2ViewModel
import com.example.collecthealthdata.presentation.screen.MainScreen
import com.example.collecthealthdata.presentation.screen.Permission
import com.example.collecthealthdata.presentation.screen.SpO2Screen
import dagger.hilt.android.AndroidEntryPoint

private const val TAG = "MainActivity"
private const val SENSOR_PERMISSION_REQUEST_CODE = 100

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
                viewModel.messageSentToast.collect { message ->
                    Toast.makeText(
                        applicationContext,
                        if (message) R.string.sending_success else R.string.sending_failed,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            Log.i(TAG, "connected: ${connectionState.connected}, message: ${connectionState.message}")
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
                            onStart = { vaping, cravingLevel ->
                                viewModel.startTracking(vaping, cravingLevel)
                                Log.i(TAG, "startTracking($vaping, $cravingLevel)")
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

    override fun onStart() {
        super.onStart()

        if (!hasSensorPermissions()) {
            requestSensorPermissions()
        } else {
            viewModel.setUpTracking()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == SENSOR_PERMISSION_REQUEST_CODE) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                viewModel.setUpTracking()
            } else {
                Toast.makeText(this, "센서 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 권한 확인
    private fun hasSensorPermissions(): Boolean {
        val activityGranted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACTIVITY_RECOGNITION
        ) == PackageManager.PERMISSION_GRANTED

        val bodyGranted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.BODY_SENSORS
        ) == PackageManager.PERMISSION_GRANTED

        return activityGranted && bodyGranted
    }

    // 권한 요청
    private fun requestSensorPermissions() {
        val permissions = arrayOf(
            Manifest.permission.ACTIVITY_RECOGNITION,
            Manifest.permission.BODY_SENSORS
        )

        ActivityCompat.requestPermissions(this, permissions, SENSOR_PERMISSION_REQUEST_CODE)
    }
}
