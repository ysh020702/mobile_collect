package com.example.collecthealthdata

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.room.Room
import com.example.collecthealthdata.ui.theme.CollectHealthDataTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class AuthActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private var context = this.baseContext

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        context = this.baseContext

        setContent {
            CollectHealthDataTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LoginScreen(
                        onLoginClick = { email, password -> signIn(email, password) },
                        onSignupClick = { email, password -> signUp(email, password) }
                    )

                }
            }
        }
    }

    private fun signUp(email: String, password: String) {
        // 회원가입 로직 작성
        Log.d(TAG, "회원가입 시도: 이메일=$email, 비밀번호=$password")
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext,
                        "Authentication failed.",
                        Toast.LENGTH_SHORT,
                    ).show()
                    //updateUI(null)
                }
            }
    }
    private fun signIn(email: String, password: String){
        //로그인 로직 작성
        Log.d(TAG, "로그인 시도: 이메일=$email, 비밀번호=$password")
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithEmail:success")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext,
                        "Authentication failed.",
                        Toast.LENGTH_SHORT,
                    ).show()
                    //updateUI(null)
                }
            }
    }

    private fun updateUI(user: FirebaseUser?){
        if(user != null){
            //MainActivity로 넘어가는 로직, FirebaseUser를 인텐트로 받아서
            val intent = Intent(context, MainActivity::class.java).apply {
                putExtra("USER_EMAIL", user.email)
                putExtra("USER_UID", user.uid)
            }
            startActivity(intent)
            finish() // 현재 액티비티 종료
            return
        }
        Log.d(TAG, "유저 정보 받아오기 실패")
        Toast.makeText(context, "유저 정보 받아오기 실패", Toast.LENGTH_SHORT).show()
    }

}

@Composable
fun LoginScreen(onLoginClick: (String, String) -> Unit, onSignupClick: (String, String) -> Unit) {
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



// 이메일 검증 함수
fun isValidEmail(email: String): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}
fun isValidPw(pw:String):Boolean {
    return pw.isNotEmpty()
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CollectHealthDataTheme {

    }
}