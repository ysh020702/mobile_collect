package com.example.collecthealthdata.user

import java.io.Serializable

data class User(
    var id: String = "",
    var email: String= "",
    var name: String = "",
    var age: Int = -1,
    var Weight: Float = -1.0f,      //체중
    var SMM: Float= -1.0f,         //골격근량 (Skeletal Muscle Mass)
    var BFM: Float= -1.0f,         //체지방량 (Body Fat Mass)
    var PBF: Float= -1.0f          //체지방률 (Percentage Body Fat), 중복되지만, 일단 합니다
) : Serializable