package com.example.collecthealthdata.data.remote.wearos

import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.example.collecthealthdata.presentation.activity.MainActicity.MainActivity
import com.example.collecthealthdata.data.local.TrackedDataDao
import com.example.collecthealthdata.data.local.TrackedDataEntity
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

private const val TAG = "DataListenerService"
private const val MESSAGE_PATH = "/msg"

@AndroidEntryPoint
class DataListenerService : WearableListenerService() {

    @Inject
    lateinit var dao: TrackedDataDao

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")

    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        if (messageEvent.path == MESSAGE_PATH) {
            val json = messageEvent.data.decodeToString()
            Log.i(TAG, "Service: message (/msg) received: $json")

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val dataList = parseJsonToEntityList(json)
                    dataList.forEach { dao.insert(it) }
                    Log.i(TAG, "Inserted ${dataList.size} entries to Room.")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse or insert data: ${e.message}")
                }
            }
        }
    }

    private fun parseJsonToEntityList(json: String): List<TrackedDataEntity> {
        val result = mutableListOf<TrackedDataEntity>()
        val jsonArray = JSONArray(json)

        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)

            val entity = TrackedDataEntity(
                cravingLevel = obj.getInt("cravingLevel"),
                vaping = obj.getBoolean("vaping"),
                hrDataString = obj.optString("hrDataString", ""),
                ibiDataString = obj.optString("ibiDataString", ""),
                accelDataString = obj.optString("accelDataString", ""),
                spo2Value = if (obj.has("spo2Value") && !obj.isNull("spo2Value")) obj.getInt("spo2Value") else null,
                spo2MeasuredAt = if (obj.has("spo2MeasuredAt") && !obj.isNull("spo2MeasuredAt")) obj.getLong("spo2MeasuredAt") else null,
                recentActivityLevel = if (obj.has("recentActivityLevel") && !obj.isNull("recentActivityLevel"))
                    obj.getDouble("recentActivityLevel").toFloat() else null,
                timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                startTime = obj.optString("startTime", ""),
                endTime = obj.optString("endTime", "")
            )

            result.add(entity)
        }

        return result
    }
}