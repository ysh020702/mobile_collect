package com.example.collecthealthdata.data.remote.firebase

import com.example.collecthealthdata.data.local.TrackedDataEntity
import com.example.collecthealthdata.domain.repository.FirebaseRepository
import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseRepositoryImpl @Inject constructor(
    private val database: DatabaseReference,
): FirebaseRepository{
    override suspend fun uploadData(data: TrackedDataEntity){
        database.child("data").push().setValue(data).await()
    }
}