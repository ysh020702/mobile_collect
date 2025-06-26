package com.example.collecthealthdata.data.repositoryimpl

import android.util.Log
import com.example.collecthealthdata.domain.repository.MessageRepository
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Node
import kotlinx.coroutines.tasks.await
import java.nio.charset.Charset
import javax.inject.Inject

private const val TAG = "MessageRepositoryImpl"

class MessageRepositoryImpl @Inject constructor(
    private val messageClient: MessageClient,
) : MessageRepository {

    override suspend fun sendMessage(message: String, node: Node, messagePath: String): Boolean {
        val nodeId = node.id
        val dataBytes = message.toByteArray(charset = Charset.defaultCharset())
        val dataSize = dataBytes.size

        // 로그로 데이터 크기 출력
        Log.i(TAG, "Sending data size: $dataSize bytes")
        var result = false
        nodeId.also { id ->
            messageClient
                .sendMessage(
                    id,
                    messagePath,
                    dataBytes
                ).apply {
                    addOnSuccessListener {
                        Log.i(TAG, "sendMessage OnSuccessListener")
                        result = true
                    }
                    addOnFailureListener {
                        Log.i(TAG, "sendMessage OnFailureListener")
                        result = false
                    }
                }.await()
            Log.i(TAG, "Result: $result")
            return result
        }
    }
}