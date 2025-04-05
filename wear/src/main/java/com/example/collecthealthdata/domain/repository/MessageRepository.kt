package com.example.collecthealthdata.domain.repository

import com.google.android.gms.wearable.Node

interface MessageRepository {
    suspend fun sendMessage(message: String, node: Node, messagePath: String): Boolean
}