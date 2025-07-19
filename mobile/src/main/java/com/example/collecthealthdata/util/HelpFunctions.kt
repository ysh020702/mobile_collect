package com.example.collecthealthdata.util

import com.example.collecthealthdata.data.local.TrackedDataEntity
import kotlinx.serialization.json.Json

class HelpFunctions {

    companion object {
        //singleton, helpFunctions 클래스 선언부에 존재, 인스턴스화 되지 않음
        fun decodeMessage(message: String): List<TrackedDataEntity> {
            return Json.decodeFromString(message)
        }
    }
}