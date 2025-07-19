package com.example.collecthealthdata.data.remote.firebase

import android.content.SharedPreferences
import com.example.collecthealthdata.data.local.TrackedDataEntity
import com.example.collecthealthdata.domain.repository.FirebaseRepository
import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Named

class FirebaseRepositoryImpl @Inject constructor(
    private val database: DatabaseReference,
    @Named("user_prefs") private val userPrefs: SharedPreferences
): FirebaseRepository{
    override suspend fun uploadData(data: TrackedDataEntity) {
        val userId = userPrefs.getString("user_id", null)
            ?: throw IllegalStateException("User ID not found in user_prefs")

        database.child("data")
            .child(userId)
            .push()
            .setValue(data)
            .await()
    }
}