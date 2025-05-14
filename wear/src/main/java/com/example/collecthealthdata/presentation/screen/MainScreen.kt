package com.example.collecthealthdata.presentation.screen

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.*
import com.example.collecthealthdata.presentation.theme.DataCollectTheme

private const val TAG = "MainScreen"

@Composable
fun MainScreen(
    connected: Boolean,
    connectionMessage: String,
    trackingRunning: Boolean,
    trackingError: Boolean,
    trackingMessage: String,
    valueHR: String,
    valueIBI: ArrayList<Int>,
    onStart: (Boolean) -> Unit,
    onStop: () -> Unit,
    onSend: () -> Unit,
    stopSignal: Boolean
) {
    Log.i(TAG, "MainScreen Composable")

    LaunchedEffect(stopSignal) {
        if (stopSignal) {
            onStop()
        }
    }

    DataCollectTheme {
        ScalingLazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "HR",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }

            item {
                Text(
                    text = valueHR,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            if (trackingRunning) {
                item {
                    Button(
                        onClick = onStop,
                        modifier = Modifier
                            .width(120.dp)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red)
                    ) {
                        Text("측정 중지", fontSize = 14.sp, color = Color.White)
                    }
                }
            } else {
                item {
                    Button(
                        onClick = { onStart(true) }, // 피고 싶은 경우
                        modifier = Modifier
                            .width(140.dp)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF4CAF50))
                    ) {
                        Text("피고 싶을 때 시작", fontSize = 14.sp, color = Color.White)
                    }
                }

                item {
                    Button(
                        onClick = { onStart(false) }, // 안 피고 싶은 경우
                        modifier = Modifier
                            .width(140.dp)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF2196F3))
                    ) {
                        Text("안 피고 싶을 때 시작", fontSize = 14.sp, color = Color.White)
                    }
                }
            }

            item {
                Button(
                    onClick = onSend,
                    enabled = connected,
                    modifier = Modifier
                        .width(120.dp)
                        .height(44.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = MaterialTheme.colors.secondary
                    )
                ) {
                    Text(
                        text = "전송",
                        fontSize = 14.sp,
                        color = MaterialTheme.colors.onSecondary
                    )
                }
            }
        }
    }
}