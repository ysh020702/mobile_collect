package com.example.collecthealthdata.presentation.activity.AuthActivity

import android.content.ContentValues.TAG
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.collecthealthdata.presentation.theme.CollectHealthDataTheme
import com.example.collecthealthdata.domain.User
import com.example.collecthealthdata.presentation.activity.MainActicity.MainActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import javax.inject.Named

private const val TAG = "AuthActivity"

@AndroidEntryPoint
class AuthActivity : ComponentActivity() {

    @Inject @Named("login_prefs") lateinit var loginPrefs : SharedPreferences
    @Inject @Named("user_prefs") lateinit var userPrefs : SharedPreferences
    private lateinit var auth: FirebaseAuth
    private var context = this.baseContext
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        context = this.baseContext


        val savedEmail = loginPrefs.getString("email", null)
        val savedPassword = loginPrefs.getString("password", null)

        if (savedEmail != null && savedPassword != null) {
            signIn(savedEmail, savedPassword)
        } else {
            // 로그인 UI 표시
            setLoginContent()
        }
    }
    private fun setLoginContent(){
        setContent {
            CollectHealthDataTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AuthScreen(
                        onLoginClick = { email, password -> signIn(email, password) },
                        onSignupClick = { email, password -> signUp(email, password) }
                    )

                }
            }
        }
    }

    private fun signIn(email: String, password: String){
        //로그인 로직 작성
        Log.d(TAG, "로그인 시도: 이메일=$email, 비밀번호=$password")
        if (email.isBlank() || password.isBlank()) {
            Toast.makeText(context, "이메일 또는 비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show()
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithEmail:success")
                    val user = auth.currentUser
                    saveLoginInfo(email, password)
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext,
                        "Authentication failed.",
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
    }

    private fun signUp(email: String, password: String) {
        // 회원가입 로직 작성
        Log.d(TAG, "회원가입 시도: 이메일=$email, 비밀번호=$password")
        if (email.isBlank() || password.isBlank()) {
            Toast.makeText(context, "이메일 또는 비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show()
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success")
                    val user = auth.currentUser
                    saveLoginInfo(email, password)
                    registerUser(user)
                    updateUI(user)

                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext,
                        "Authentication failed.",
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
    }

    private fun saveLoginInfo(email: String, password: String) {
        loginPrefs.edit().apply(){
            putString("email", email).putString("password", password)
            apply()
        }
    }
    private fun saveUserInfo(userId: String){
        userPrefs.edit().apply {
            putString("user_id", userId)
            apply() // apply()를 사용하여 비동기적으로 저장
        }
    }

    private fun registerUser(user: FirebaseUser?){
        //register A User Into Firebase Database
        if (user == null) return
        //프로젝트에 있는 유저 인스턴스
        val userInstance = User(id = user.uid.toString(), email = user.email.toString())

        //firebase_database_reference
        database = Firebase.database.reference

        //user -> userID -> user Data 저장
        database.child("users").child(user.uid.toString()).setValue(userInstance)
        Log.d(TAG, "userInformation Stored")
    }


    private fun updateUI(user: FirebaseUser?){
        //going To mainActivity
        if(user == null){
            Log.d(TAG, "Failed To Get User Information")
            Toast.makeText(context, "유저 정보 받아오기 실패", Toast.LENGTH_SHORT).show()
            return
        }

        saveUserInfo(user.uid.toString())

        val intent = Intent(context, MainActivity::class.java)
        startActivity(intent)
        finish() // 현재 액티비티 종료
        return

    }

}

// 이메일 검증 함수
fun isValidEmail(email: String): Boolean {
    return Patterns.EMAIL_ADDRESS.matcher(email).matches()
}
fun isValidPw(pw:String):Boolean {
    return pw.isNotEmpty() && (pw.length >= 6)
}
