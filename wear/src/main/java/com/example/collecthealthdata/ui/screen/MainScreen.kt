package com.example.collecthealthdata.ui.screen

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
import com.example.collecthealthdata.ui.theme.DataCollectTheme

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
    onStart: () -> Unit,
    onStop: () -> Unit,
    onSend: () -> Unit
) {
    Log.i(TAG, "MainScreen Composable")

    DataCollectTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // HR 텍스트
                Text(
                    text = "HR",
                    color = Color.Gray,
                    fontSize = 14.sp
                )

                // 실제 심박수 값
                Text(
                    text = valueHR,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                // 측정 시작/중지 버튼
                Button(
                    onClick = {
                        if (trackingRunning) onStop() else onStart()
                    },
                    enabled = connected,
                    modifier = Modifier
                        .width(120.dp)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = if (trackingRunning) Color.Red else MaterialTheme.colors.primary
                    )
                ) {
                    Text(
                        text = if (trackingRunning) "측정 중지" else "측정 시작",
                        fontSize = 14.sp,
                        color = MaterialTheme.colors.onPrimary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 전송 버튼
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
