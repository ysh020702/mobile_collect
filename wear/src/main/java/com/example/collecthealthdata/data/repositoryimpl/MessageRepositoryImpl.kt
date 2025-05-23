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
        var result = false
        nodeId.also { id ->
            messageClient
                .sendMessage(
                    id,
                    messagePath,
                    message.toByteArray(charset = Charset.defaultCharset())
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
            Log.i(TAG, "result: $result")
            return result
        }
    }
}