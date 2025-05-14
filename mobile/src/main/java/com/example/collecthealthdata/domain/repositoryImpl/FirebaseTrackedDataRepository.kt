package com.example.collecthealthdata.domain.repositoryImpl

import android.util.Log
import com.example.collecthealthdata.data.TrackedDataEntity
import com.example.collecthealthdata.data.repository.TrackedDataRepository
import com.google.firebase.Firebase
import com.google.firebase.database.database
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseTrackedDataRepository @Inject constructor() : TrackedDataRepository {

    override suspend fun saveData(userId : String, dataList: List<TrackedDataEntity>){
        var userRef = Firebase.database.reference
            .child("users").child(userId).child("data")

        dataList.forEach { trackedData ->
            try {
                userRef.child(trackedData.id.toString())
                    .setValue(trackedData)
                    .await() // 저장될 때까지 suspend
                Log.d("FirebaseRepo", "Saved data for ID: ${trackedData.id}")
            } catch (e: Exception) {
                Log.e("FirebaseRepo", "Failed to save ID: ${trackedData.id}", e)
            }
        }
    }
}