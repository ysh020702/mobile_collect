package com.example.collecthealthdata.data.local

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {
    @TypeConverter
    fun fromIbiList(list: List<Int>): String = Json.Default.encodeToString(list)

    @TypeConverter
    fun toIbiList(data: String): List<Int> = Json.Default.decodeFromString(data)
}