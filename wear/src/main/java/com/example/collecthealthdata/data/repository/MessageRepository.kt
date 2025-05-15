package com.example.collecthealthdata.data.repository

import com.google.android.gms.wearable.Node

interface MessageRepository {
    suspend fun sendMessage(message: String, node: Node, messagePath: String): Boolean
}