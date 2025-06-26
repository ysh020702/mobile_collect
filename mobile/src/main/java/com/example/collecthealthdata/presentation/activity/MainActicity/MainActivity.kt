package com.example.collecthealthdata.presentation.activity.MainActicity

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.collecthealthdata.domain.User
import com.google.firebase.Firebase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import javax.inject.Named

private const val TAG = "MainActivity"

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject @Named("user_prefs") lateinit var userPrefs : SharedPreferences

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

}

