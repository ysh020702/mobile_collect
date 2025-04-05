package com.example.collecthealthdata.ui.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.collecthealthdata.ui.model.MainViewModel
import com.example.collecthealthdata.ui.screen.MainScreen
import dagger.hilt.android.AndroidEntryPoint

private const val TAG = "MainActivity"

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel by viewModels<MainViewModel>()

    @SuppressLint("VisibleForTests")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val trackingState by viewModel.trackingState.collectAsStateWithLifecycle()
            val connectionState by viewModel.connectionState.collectAsStateWithLifecycle()
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
                MainScreen(connectionState.connected,
                    connectionState.message,
                    trackingState.trackingRunning,
                    trackingState.trackingError,
                    trackingState.message,
                    trackingState.valueHR,
                    trackingState.valueIBI,
                    { viewModel.startTracking(); Log.i(TAG, "startTracking()") },
                    { viewModel.stopTracking(); Log.i(TAG, "stopTracking()") },
                    { viewModel.sendMessage(); Log.i(TAG, "sendMessage()") })
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