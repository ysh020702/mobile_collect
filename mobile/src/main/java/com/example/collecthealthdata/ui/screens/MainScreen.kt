package com.example.collecthealthdata.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.collecthealthdata.ui.UserInputActivity
import com.example.collecthealthdata.user.User
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
                Button(onClick = { /* 로그아웃 로직 */ }) {
                    Text("로그아웃")
                }
                Spacer(modifier = Modifier.width(8.dp)) // 버튼 간격
                Button(onClick = { /* 계정 삭제 로직 */ }) {
                    Text("계정 삭제")
                }
            }

            // 오른쪽: Sync 버튼
            Button(onClick = { /* Sync 클릭 로직 */ }) {
                Text("Sync")
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
