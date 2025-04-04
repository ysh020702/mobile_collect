package com.example.collecthealthdata.domain.usecase

import com.example.collecthealthdata.domain.model.TrackedData
import com.example.collecthealthdata.domain.repository.TrackedDataRepository
import javax.inject.Inject

class SaveTrackedDataUseCase @Inject constructor(
    private val repository: TrackedDataRepository
) {
    suspend operator fun invoke(data: TrackedData) {
        repository.insert(data)
    }
}


//Repository 에 종속되는 클래스임(특히 이건 TrackedDataRepository에 -> 매개변수로 받아서 repository 의 기능을 사용함
// -> 기능 추가가 쉬움!! 이게 DI(Dependency Injection)
