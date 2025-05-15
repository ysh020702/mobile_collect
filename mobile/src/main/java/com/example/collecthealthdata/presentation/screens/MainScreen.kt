package com.example.collecthealthdata.presentation.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.collecthealthdata.presentation.activity.AuthActivity
import com.example.collecthealthdata.presentation.activity.UserInputActivity
import com.example.collecthealthdata.data.User
import com.google.firebase.auth.FirebaseAuth
import kotlin.jvm.java

@Composable
fun MainScreen(context: Context, user: User) {
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

            Button(onClick = { /* 버튼 2 클릭 */ }) { Text("Button 2") }
            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = { /* 버튼 3 클릭 */ }) { Text("Button 3") }
        }
    }
}

fun unDefinedFeature(context:Context, text: String){
    Toast.makeText(context, "현재 $text 기능 비활성화중입니다. 개발자에게 문의해주세요",Toast.LENGTH_SHORT).show()
}
