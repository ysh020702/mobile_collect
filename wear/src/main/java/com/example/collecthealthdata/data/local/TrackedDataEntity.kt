package com.example.collecthealthdata.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.collecthealthdata.data.local.Converters
import com.example.collecthealthdata.domain.model.TrackedData

@Entity(tableName = "tracked_data")
@TypeConverters(Converters::class)
data class TrackedDataEntity (
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var hr: Int,
    var ibi: List<Int>,
    val timestamp: Long = System.currentTimeMillis()
)
//확장 함수 형태, 자세한 건 domain.TrackedData참고
fun TrackedDataEntity.toDomain(): TrackedData {
    return TrackedData(hr = hr, ibi = ibi)
}