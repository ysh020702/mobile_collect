package com.example.collecthealthdata.ui.screens

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
import com.example.collecthealthdata.data.TrackedDataEntity
import com.example.collecthealthdata.user.User

@Composable
fun ReceiveScreen(
    results: List<TrackedDataEntity>
) {
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
    }
}
