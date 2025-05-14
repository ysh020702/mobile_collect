package com.example.collecthealthdata.data

import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.example.collecthealthdata.HelpFunctions.Companion.decodeMessage
import com.example.collecthealthdata.data.repository.TrackedDataRepository
import com.example.collecthealthdata.presentation.activity.MainActivity
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

private const val TAG = "DataListenerService"
private const val MESSAGE_PATH = "/msg"

@AndroidEntryPoint
class DataListenerService : WearableListenerService() {

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")

    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        super.onMessageReceived(messageEvent)
        val value = messageEvent.data.decodeToString()
        Log.i(TAG, "onMessageReceived(): $value")

        when (messageEvent.path) {
            //when 분기에서의 조건
            MESSAGE_PATH -> {
                Log.i(TAG, "Service: message (/msg) received: $value")
                if (value.isNotEmpty()) {
                    Toast.makeText(baseContext, "데이터 수신 완료",Toast.LENGTH_SHORT).show()
                    //MainActivity로 이동해서 데이터를 DB로 전송한다
                    val intent = Intent(this, MainActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        putExtra("message", value)
                    }
                    startActivity(intent)

                } else { Log.i(TAG, "value is an empty string") }
            }

            else -> {
                Log.i(TAG, "message has been Sent to Wrong Path ${messageEvent.path}")
            }
        }
    }



}