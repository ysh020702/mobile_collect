package com.example.collecthealthdata.domain

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import androidx.core.content.edit

class SpO2ResultStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("spo2_result", Context.MODE_PRIVATE)

    fun saveSpO2(timestamp: Long, value: Int) {
        prefs.edit() {
            putLong("timestamp", timestamp)
                .putInt("spo2_value", value)
        }
    }

    fun loadSpO2(): Pair<Long, Int> {
        val time = prefs.getLong("timestamp", 0L)
        val value = prefs.getInt("spo2_value", 0)
        return time to value
    }
}
