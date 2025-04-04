package com.example.collecthealthdata.data.local.converter

import androidx.core.view.WindowInsetsCompat
import androidx.room.TypeConverter

class IntListConverter {
    @TypeConverter
    fun fromList(value: List<Int>): String{
        //,으로 묶어 String으로 반환합니다
        return value.joinToString(",")
    }

    @TypeConverter
    fun toList(value: String): List<Int>{
        return if(value.isEmpty()) emptyList() else value.split(",").map{it.toInt()}
    }
}