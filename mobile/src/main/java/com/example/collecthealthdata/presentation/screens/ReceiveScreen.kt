package com.example.collecthealthdata.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.collecthealthdata.domain.TrackedDataEntity

@Composable
fun ReceiveScreen(
    results: List<TrackedDataEntity>
) {
    var combinedText = results.joinToString(separator = "\n") {it.hrDataString}
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(
            Modifier
                .height(70.dp)
                .fillMaxWidth()
                .background(Color.Black)
        )
       Text(
           textAlign = TextAlign.Start,
           fontSize = 20.sp,
           text = "DataReceivedScreen",
           color = Color.Gray,
       )
        Spacer(
            Modifier
                .height(70.dp)
                .fillMaxWidth()
                .background(Color.Black)
        )
        Text(
            textAlign = TextAlign.Start,
            fontSize = 20.sp,
            text = combinedText,
            color = Color.Gray,
        )
    }
}
