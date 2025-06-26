package com.example.collecthealthdata.presentation.activity.AuthActivity

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun AuthScreen(onLoginClick: (String, String) -> Unit, onSignupClick: (String, String) -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf(false) }
    var pwError by remember{ mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        TextField(
            value = email,
            onValueChange = {
                email = it
                emailError = !isValidEmail(email)            },
            label = { Text("Email") },
            isError = emailError,
            modifier = Modifier.fillMaxWidth()
        )
        if(emailError){
            Text(
                text = "유효한 이메일 주소를 입력하세요.",
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.Start)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = password,
            onValueChange = {
                password = it
                pwError = !isValidPw(password)
            },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {onLoginClick(email, password)},
            modifier = Modifier.fillMaxWidth(),
            enabled = !emailError && !pwError
        ) {
            Text("로그인")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {onSignupClick(email, password)},
            modifier = Modifier.fillMaxWidth(),
            enabled = !emailError && !pwError) {
            Text("회원가입")
        }
    }
}

