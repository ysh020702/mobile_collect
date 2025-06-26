package com.example.collecthealthdata.presentation.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import android.Manifest
import com.example.collecthealthdata.HelpFunctions.Companion.decodeMessage
import com.example.collecthealthdata.presentation.screens.MainScreen
import com.example.collecthealthdata.domain.User
import com.example.collecthealthdata.domain.repository.TrackedDataRepository
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

    private val RECORD_AUDIO_PERMISSION_CODE = 1001

    private lateinit var context: Context
    var user: User? = null  // 초기값 null 설정
    private var userId = ""
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //init
        initialize()
        checkAndRequestAudioPermission()
        handleIntent(intent) // 👈 추가
        setContent {
            MainScreenWithUser(context, userId, database)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == RECORD_AUDIO_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("Permission", "사용자가 녹음 권한을 허용함")
                Toast.makeText(this, "녹음 권한이 허용되었습니다.", Toast.LENGTH_SHORT).show()
            } else {
                Log.e("Permission", "사용자가 녹음 권한을 거부함")
                Toast.makeText(this, "녹음 권한이 거부되었습니다. 기능이 제한될 수 있습니다.", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        intent?.getStringExtra("message")?.let { message ->
            Log.d("MainActivity", "Received message: $message")

            val trackedData = decodeMessage(message)
            if (userId == "") userId = "anonymous"

            lifecycleScope.launch {
                trackedDataRepository.saveData(userId, trackedData)
            }
        }
    }

    override fun onStart() {
        super.onStart()
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
    private fun checkAndRequestAudioPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                RECORD_AUDIO_PERMISSION_CODE
            )
        } else {
            Log.d("Permission", "RECORD_AUDIO 권한 이미 허용됨")
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
