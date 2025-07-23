package com.example.collecthealthdata.presentation.screen

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.*
import com.example.collecthealthdata.presentation.SpO2ViewModel
import kotlinx.coroutines.delay

@Composable
fun SpO2Screen(
    viewModel: SpO2ViewModel,
    onBack: () -> Unit
) {
    val pastelAmber = Color(0xFFFFE082)
    val gunmetal = Color(0xFF2C3539)
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    val progress = remember { Animatable(0f) }

    // Constants (Java 코드의 Duration, Tick 대응)
    val MEASUREMENT_DURATION = 25000L  // 25초
    val MEASUREMENT_TICK = 100L        // 0.1초

    // 측정 시작 시 타이머 효과 (Progress 증가)
    LaunchedEffect(uiState.measuring) {
        if (uiState.measuring) {
            progress.snapTo(0f)
            val steps = MEASUREMENT_DURATION / MEASUREMENT_TICK
            val increment = 1f / steps

            repeat(steps.toInt()) { step ->
                if (!uiState.measuring) return@LaunchedEffect  // 조기 중지 시 빠져나옴
                progress.snapTo(progress.value + increment)
                delay(MEASUREMENT_TICK)
            }

        } else {
            progress.snapTo(0f)  // 측정 중지 시 초기화
        }
    }

    // 측정 완료 시 토스트 후 이동
    LaunchedEffect(uiState.measurementCompleted) {
        if (uiState.measurementCompleted) {
            Toast.makeText(context, "SpO2 측정 완료: ${uiState.spo2}%", Toast.LENGTH_SHORT).show()
            delay(5000)
            onBack() //popbackStack
        }
    }

    LaunchedEffect(uiState.measurementFailed) {
        if (uiState.measurementFailed) {
            Toast.makeText(context, "SpO2 측정 실패, 다시 시도하세요", Toast.LENGTH_SHORT).show()
        }
    }



    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {


        if (uiState.measuring) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(200.dp)
            ) {
                CircularProgressIndicator(
                    progress = progress.value,
                    modifier = Modifier.fillMaxSize(),
                    indicatorColor = pastelAmber,
                    trackColor = Color.DarkGray,
                    strokeWidth = 6.dp
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ){
                    Text("SpO2 측정 중...",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.stopMeasurement(false) },
                        modifier = Modifier
                            .width(60.dp)
                            .height(44.dp),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = gunmetal
                        )
                    ) {
                        Text("중지", color = Color.White)
                    }
                }
            }
        }
        else if(uiState.measurementCompleted){
            Text("SpO2 측정 결과",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            Text("현재 SpO2 값:")
            Text(
                text = if (uiState.spo2 > 0) "${uiState.spo2}%" else "--",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("잠시 후 메인 화면으로 돌아갑니다...",
                color = Color.White,
                fontSize = 10.sp)
        }
        else {
            Text("SpO2 측정하기", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { viewModel.startMeasurement() },
                modifier = Modifier
                    .width(120.dp)
                    .height(44.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = gunmetal
                )
            ) {
                Text("측정 시작",color = Color.White)
            }
        }
    }
}
