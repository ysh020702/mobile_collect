package com.example.collecthealthdata.domain.usecase

import android.util.Log
import com.example.collecthealthdata.data.local.TrackedDataEntity
import com.example.collecthealthdata.domain.repository.MessageRepository
import com.example.collecthealthdata.domain.repository.TrackedDataRepository
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import kotlin.collections.isNotEmpty

private const val TAG = "SendMessageUseCase"
private const val MESSAGE_PATH = "/msg"

class SendMessageUseCase @Inject constructor(
    private val messageRepository: MessageRepository,
    private val trackedDataRepository: TrackedDataRepository,
    private val getCapableNodes: GetCapableNodes
) {
    suspend operator fun invoke(): Boolean {
        val nodes = getCapableNodes()

        if(nodes.isEmpty()){
            Log.w(TAG, "No capable nodes found")
            return false
        }

        val node = nodes.first()
        val trackedDataList = trackedDataRepository.getAll().first()

        // 요소 하나씩 전송
        var successCount = 0
        for (entity in trackedDataList) {
            val message = encodeMessage(entity)
            val result = messageRepository.sendMessage(message, node, MESSAGE_PATH)
            if (result) {
                successCount++
            } else {
                Log.e(TAG, "Failed to send entity: $entity")
            }
        }
        // 전송된 개수 로깅
        Log.i(TAG, "Sent $successCount/${trackedDataList.size} messages")

        return successCount == trackedDataList.size // 모두 전송 성공 시
    }

    private fun encodeMessage(entity: TrackedDataEntity): String {
        return Json.encodeToString(entity)
    }
}
