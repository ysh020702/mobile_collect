package com.example.collecthealthdata.data.repositoryImpl

import android.util.Log
import com.example.collecthealthdata.domain.TrackedDataEntity
import com.example.collecthealthdata.domain.repository.TrackedDataRepository
import com.google.firebase.Firebase
import com.google.firebase.database.database
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseTrackedDataRepository @Inject constructor() : TrackedDataRepository {

    override suspend fun saveData(userId: String, dataList: List<TrackedDataEntity>) {
        val userRef = Firebase.database.reference
            .child("users").child(userId).child("data")

        dataList.forEach { trackedData ->
            try {
                // 1. 중복 확인
                val snapshot = userRef.orderByChild("timestamp")
                    .equalTo(trackedData.timestamp.toDouble()) // timestamp가 Long이면 toDouble() 필요
                    .get().await()

                // 2. 이미 존재하면 skip
                if (snapshot.exists()) {
                    Log.d("FirebaseRepo", "Duplicate data exists for timestamp: ${trackedData.timestamp}")
                    return@forEach
                }

                // 3. 새 데이터 저장
                val newRef = userRef.push()
                newRef.setValue(trackedData).await()
                Log.d("FirebaseRepo", "Saved new data with key: ${newRef.key}")
            } catch (e: Exception) {
                Log.e("FirebaseRepo", "Failed to save new data", e)
            }
        }
    }
}