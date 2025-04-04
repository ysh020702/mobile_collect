package com.example.collecthealthdata.ui.screen.tracked

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material.*
import com.example.collecthealthdata.domain.model.TrackedData
import com.example.collecthealthdata.ui.screen.tracked.components.TrackedDataItem

@Composable
fun TrackedDataScreen(
    viewModel: TrackedDataViewModel = viewModel()
) {
    val trackedDataList by viewModel.trackedData.collectAsState()

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        Text(
            text = "Tracked Data",
            style = MaterialTheme.typography.title3
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(trackedDataList) { data ->
                TrackedDataItem(data = data)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color.Gray)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = {
                val dummy = TrackedData(
                    hr = (60..100).random(),
                    ibi = arrayListOf(800, 810, 795)
                )
            }) {
                Text("Insert")
            }

            Button(onClick = {
                viewModel.deleteAllTrackedData()
            }) {
                Text("Delete All")
            }
        }
    }
}