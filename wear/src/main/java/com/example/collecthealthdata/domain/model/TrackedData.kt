package com.example.collecthealthdata.domain.model

import com.example.collecthealthdata.data.local.TrackedDataEntity
import kotlinx.serialization.Serializable

@Serializable
data class TrackedData (
    var hr: Int = 0,
    var ibi: List<Int> = emptyList()
)

//확장 함수 개념, Data 가 Domain에 의존성을 일단 가지는 상태
//근데 dataClass의 멤버 함수로 toEntity를 넣으면, domain에 data에 의존성을 가지게 됨
//이거 안 된다!!! 그래서 아래처럼 확장 함수로 표현함
fun TrackedData.toEntity(): TrackedDataEntity {
    return TrackedDataEntity(hr = hr, ibi = ibi)
}