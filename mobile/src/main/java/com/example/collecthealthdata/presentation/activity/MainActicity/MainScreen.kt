package com.example.collecthealthdata.presentation.activity.MainActicity

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.collecthealthdata.presentation.activity.UserInputActivity.UserInputActivity
import com.example.collecthealthdata.domain.User
import com.example.collecthealthdata.presentation.viewmodel.MainViewModel
import com.google.firebase.database.DatabaseReference
import kotlin.jvm.java

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

@Composable
fun MainScreen(
    context: Context,
    user: User,
    viewModel: MainViewModel = hiltViewModel()
) {
    val uploadResult by viewModel.uploadResult.collectAsState(initial = null)

    LaunchedEffect(uploadResult) {
        uploadResult?.let { success ->
            val msg = if (success) "✅ 업로드 성공" else "❌ 업로드 실패"
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp) // 전체 패딩 추가
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween // 왼쪽 & 오른쪽 정렬
        ) {
            // 왼쪽: 로그아웃 & 계정 삭제 버튼
            Row {
                Button(onClick = {
                    unDefinedFeature(context, "로그아웃")
                    //FirebaseAuth.getInstance().signOut()
                    //val intent = Intent(context, AuthActivity::class.java)
                    //intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    //context.startActivity(intent)
                }) {
                    Text("로그아웃")
                }
                Spacer(modifier = Modifier.width(8.dp)) // 버튼 간격
                Button(onClick = {
                    unDefinedFeature(context,"계정 삭제")
                    /*val user = FirebaseAuth.getInstance().currentUser

                    user?.delete()
                        ?.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Log.d("AccountDelete", "계정 삭제 성공")

                                // SharedPreferences 초기화 (있다면)
                                val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                                prefs.edit().clear().apply()

                                // 로그인 화면으로 이동
                                val intent = Intent(context, AuthActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                context.startActivity(intent)
                            } else {
                                Log.w("AccountDelete", "계정 삭제 실패", task.exception)
                                Toast.makeText(context, "계정 삭제에 실패했습니다.", Toast.LENGTH_SHORT).show()
                            }
                        }*/
                }) {
                    Text("계정 삭제")
                }
            }

        }

        // 화면 중앙 버튼들
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 50.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = {
                val intent = Intent(context, UserInputActivity::class.java).apply {
                    putExtra("USER",user)
                }
                context.startActivity(intent)
            }) { Text("개인 정보 입력하기") }
            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                viewModel.showLocalDataCount(context)
            }) {
                Text("로컬 데이터 개수 확인")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                Toast.makeText(context, "데이터를 서버에 업로드합니다...", Toast.LENGTH_SHORT).show()
                viewModel.uploadDataToFirebase() // 나중에 ViewModel 연결 예정
            }) {
                Text("데이터 업로드")
            }
        }
    }
}

fun unDefinedFeature(context:Context, text: String){
    Toast.makeText(context, "현재 $text 기능 비활성화중입니다. 개발자에게 문의해주세요",Toast.LENGTH_SHORT).show()
}



