package com.example.collecthealthdata.ui.screen.tracked.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Text
import com.example.collecthealthdata.domain.model.TrackedData

@Composable
fun TrackedDataItem(data: TrackedData) {
    Column(modifier = Modifier.padding(8.dp)) {
        Text("Heart Rate: ${data.hr}")
        Text("IBI: ${data.ibi.joinToString(", ")}")
    }
}