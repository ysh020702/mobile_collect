package com.example.collecthealthdata.data

import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.example.collecthealthdata.ui.ReceiveActivity
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService


private const val TAG = "DataListenerService"
private const val MESSAGE_PATH = "/msg"

class DataListenerService : WearableListenerService() {


    override fun onMessageReceived(messageEvent: MessageEvent) {
        super.onMessageReceived(messageEvent)

        val value = messageEvent.data.decodeToString()
        Log.i(TAG, "onMessageReceived(): $value")
        when (messageEvent.path) {
            MESSAGE_PATH -> {
                Log.i(TAG, "Service: message (/msg) received: $value")

                if (value != "") {
                    //TODO : 데이터를 받아서 DB에 저장하기
                    Toast.makeText(baseContext, "데이터 수신 완료",Toast.LENGTH_SHORT).show()
                    startActivity(
                        Intent(this, ReceiveActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).putExtra("message", value)
                    )

                } else {
                    Log.i(TAG, "value is an empty string")
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")
    }

}