package com.example.collecthealthdata.data

import kotlinx.serialization.Serializable

@Serializable
data class TrackedData (
    var hr: Int = 0,
    var ibi: ArrayList<Int> = arrayListOf()
)