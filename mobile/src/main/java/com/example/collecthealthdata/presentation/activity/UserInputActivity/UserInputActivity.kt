package com.example.collecthealthdata.presentation.activity.UserInputActivity

import android.content.Context
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
import com.example.collecthealthdata.domain.User
import com.google.firebase.Firebase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database

private const val TAG = "UserInputActivity"

class UserInputActivity : ComponentActivity() {
    lateinit var context: Context
    lateinit var user : User
    lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialize()

        setContent {
            var userState by remember { mutableStateOf<User?>(null) }
            LaunchedEffect(Unit) {
                userState = intent.getSerializableExtra("USER") as? User
            }
            UserInputScreenWithLoading(userState, ::saveUserToFirebase)
        }
    }

    fun initialize(){
        database = Firebase.database.reference
        context = this.baseContext
        user = intent.getSerializableExtra("USER") as? User ?: run {
            Log.e(TAG, "User data is missing from Intent!")
            finish() // Activity 종료
            return
        }
    }

    fun saveUserToFirebase(user: User) {
        if (user.id.isBlank()) {
            Log.e(TAG, "User ID is missing, cannot save to Firebase!")
            return
        }
        database.child("users").child(user.id).setValue(user)
            .addOnSuccessListener {
                Log.d(TAG, "User data saved successfully!")
                Toast.makeText(context, "유저 데이터 저장에 성공하였습니다",Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Log.e(TAG, "Failed to save user data: ${it.message}")
                Toast.makeText(context, "유저 데이터 저장에 실패하였습니다. 로그를 분석해주세요",Toast.LENGTH_SHORT).show()
            }

        finish()
    }
}

//setcont에서 유저 정보가 불러와지는대로 UserInputScreen 으로 이동함
@Composable
fun UserInputScreenWithLoading(user: User?, onSave: (User) -> Unit) {
    if (user == null) {
        LoadingScreen() // 유저 정보가 없을 때 로딩 화면 표시
    } else {
        UserInputScreen(user, onSave) // 유저 정보가 있으면 입력 화면 표시
    }
}

@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator() // 로딩 인디케이터
            Spacer(modifier = Modifier.height(16.dp))
            Text("Loading...", fontSize = 18.sp)
        }
    }
}