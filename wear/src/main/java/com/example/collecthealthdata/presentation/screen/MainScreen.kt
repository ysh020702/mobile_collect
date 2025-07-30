package com.example.collecthealthdata.presentation.screen

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
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
    onStart: (Boolean, Int) -> Unit,
    onStop: () -> Unit,
    onSignal: () -> Unit,
    onSend: () -> Unit,
    onSpO2Click: () -> Unit,
    stopSignal: Boolean
) {
    Log.i("MainScreen", "Composable")

    val dividerColor = Color.White.copy(alpha = 0.3f)

    LaunchedEffect(stopSignal) {
        if (stopSignal) {
            Log.d("ScreenMain", "onStop called $stopSignal")
            onStop()
        }
    }

    DataCollectTheme {
        ScalingLazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Text(
                    text = "HR",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            item {
                Text(
                    text = valueHR,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
            item { Divider(color = dividerColor, thickness = 1.dp) }

            if (trackingRunning) {
                item {
                    MenuItem(text = "측정 중지", onClick = onSignal)
                }
                item { Divider(color = dividerColor, thickness = 1.dp) }
            } else {
                /*
                주제: 현재 담배를 피고 있는지에 따른 생체 데이터를 측정하고,
                어떤 생체 데이터가 담배 욕구와 밀접한 관련이 있는지
                그리고 사용자의 행동과 생체 신호로 담배를 피고 싶은 욕구가 있을지, 아니면 현재 담배를 피는지
                판별하는 딥러닝 모델을 구축*/
                item {
                    MenuItem(text = "담배 피기 시작할 때\n(담배 피기 전 측정 시작)", onClick = { onStart(true, -1) })
                }
                item { Divider(color = dividerColor, thickness = 1.dp) }

                item {
                    MenuItem(text = "담배 피지 않을 때", onClick = { onStart(false,-1) })
                }
                item { Divider(color = dividerColor, thickness = 1.dp) }

                item {
                    MenuItem(text = "SpO2 측정", onClick = onSpO2Click)
                }
                item { Divider(color = dividerColor, thickness = 1.dp) }

                item {
                    MenuItem(
                        text = "데이터 전송",
                        onClick = onSend,
                        enabled = connected
                    )
                }
                item { Divider(color = dividerColor, thickness = 1.dp) }
            }
        }
    }
}


@Composable
fun MenuItem(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            color = if (enabled) Color.White else Color.Gray
        )
    }
}

@Composable
fun Divider(
    color: Color,
    thickness: Dp = 1.dp
) {
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .height(thickness)
            .background(color)
    )
}
