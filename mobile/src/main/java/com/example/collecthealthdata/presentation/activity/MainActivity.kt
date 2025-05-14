package com.example.collecthealthdata.presentation.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.*
import androidx.lifecycle.lifecycleScope
import com.example.collecthealthdata.HelpFunctions.Companion.decodeMessage
import com.example.collecthealthdata.presentation.screens.MainScreen
import com.example.collecthealthdata.data.User
import com.example.collecthealthdata.data.repository.TrackedDataRepository
import com.google.firebase.Firebase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

private const val TAG = "MainActivity"

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject @Named("user_prefs") lateinit var userPrefs : SharedPreferences
    @Inject lateinit var trackedDataRepository: TrackedDataRepository

    private lateinit var context: Context
    var user: User? = null  // 초기값 null 설정
    private var userId = ""
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //init
        initialize()
        setContent {
            MainScreenWithUser(context, userId, database)
        }
    }

    fun initialize(){
        context = this
        database = Firebase.database.reference
        userId = userPrefs.getString("user_id", "") ?: ""
        if (userId == "") {
            Log.i(TAG, "user_id not found!")
            return
        }
    }

    override fun onNewIntent(intent : Intent?){
        super.onNewIntent(intent)
        intent?.getStringExtra("message")?.let { message ->
            Log.d("MainActivity", "Received message via onNewIntent: $message")

            //string -> List<TrackedDataEntity> 로 decode
            var trackedData = decodeMessage(message)

            //userId 없는 경우 anonymous
            if(userId=="") userId="anonymous"

            //firebase와 동기화
            lifecycleScope.launch {
                trackedDataRepository.saveData(userId, trackedData)
            }
        }
    }
}

@Composable
fun MainScreenWithUser(context: Context, userId: String, database: DatabaseReference) {
    var user by remember { mutableStateOf<User?>(null) }

    //TODO: user 의 체성분 데이터가 없으면 입력받도록 하기!!

    //유저 정보 받아오기(런처이펙트)
    LaunchedEffect(userId) {
        database.child("users").child(userId).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    user = snapshot.getValue(User::class.java)!!
                } else {
                    Log.d("Firebase", "User not found")
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Failed to fetch user", e)
            }
    }

    //유저 정보가 null인 상태(덜 받아왔으면) 로딩 화면 표시하기
    user?.let {
        MainScreen(context, it)
    } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Loading user data...", fontSize = 18.sp)
        }
    }
}
