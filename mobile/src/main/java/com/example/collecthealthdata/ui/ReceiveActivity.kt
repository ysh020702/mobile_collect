package com.example.collecthealthdata.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import com.example.collecthealthdata.HelpFunctions
import com.example.collecthealthdata.ui.screens.ReceiveScreen

const val TAG = "ReceiveActivity"

class ReceiveActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { TheApp(intent) }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setContent { TheApp(intent) }
    }

    override fun onResume() {
        super.onResume()
        Log.i(TAG, "onResume()")
    }
}

@Composable
fun TheApp(intent: Intent?) {
    if (intent?.getStringExtra("message") != null) {
        val txt = intent.getStringExtra("message").toString()

        //측정 결과 저장!!
        val measurementResults = HelpFunctions.decodeMessage(txt)

        //TODO: 측정 결과를 저장하는 로직을 만들기
        //고려해야할 점:
        //1. 로그인한 후 그 사람의 계정에 있는 파이어베이스에 저장하게 할 것
        //2. 로그인 시 자동 로그인을 고려할 것
        ReceiveScreen(measurementResults)
    }
    else{
        Log.i(TAG, "no message received")
    }
}


