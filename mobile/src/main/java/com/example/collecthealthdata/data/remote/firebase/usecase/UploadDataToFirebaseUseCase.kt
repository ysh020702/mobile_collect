package com.example.collecthealthdata.data.remote.firebase.usecase

import com.example.collecthealthdata.data.local.TrackedDataDao
import com.example.collecthealthdata.domain.repository.FirebaseRepository
import javax.inject.Inject

class UploadDataToFirebaseUseCase @Inject constructor(
    private val dao: TrackedDataDao,
    private val firebaseRepository: FirebaseRepository
) {
    suspend operator fun invoke() {
        val dataList = dao.getAllDataOnce() // suspend fun: 전체 데이터 가져오기

        for (data in dataList) {
            firebaseRepository.uploadData(data) // 업로드
            dao.deleteById(data.id)              // 성공 시 삭제
        }
    }
}