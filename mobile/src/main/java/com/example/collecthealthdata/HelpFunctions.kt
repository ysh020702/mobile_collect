package com.example.collecthealthdata

import com.example.collecthealthdata.data.TrackedData
import kotlinx.serialization.json.Json

class HelpFunctions {

    companion object {
        fun decodeMessage(message: String): List<TrackedData> {

            return Json.decodeFromString(message)
        }
    }
}