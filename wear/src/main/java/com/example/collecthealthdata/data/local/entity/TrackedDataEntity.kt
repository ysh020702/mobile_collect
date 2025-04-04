package com.example.collecthealthdata.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.collecthealthdata.data.local.converter.IntListConverter

@Entity(tableName = "tracked_data")
@TypeConverters(IntListConverter::class)
data class TrackedDataEntity (
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var hr: Int,
    var ibi: List<Int>
)
