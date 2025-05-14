package com.example.collecthealthdata.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Colors
import androidx.wear.compose.material.Typography

// Wear 전용 컬러 팔레트
private val wearColorPalette = Colors(
    primary = Color(0xFF00BCD4),
    primaryVariant = Color(0xFF008BA3),
    secondary = Color(0xFF4CAF50),
    secondaryVariant = Color(0xFF388E3C),
    background = Color.Black,
    surface = Color.DarkGray,
    error = Color.Red,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
    onError = Color.White
)

// 기본 타이포그래피
private val typography = Typography()

@Composable
fun DataCollectTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = wearColorPalette,
        typography = typography,
        content = content
    )
}

