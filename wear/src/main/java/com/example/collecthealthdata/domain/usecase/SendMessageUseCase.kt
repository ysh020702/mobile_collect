package com.example.collecthealthdata.domain.usecase

import android.util.Log
import com.example.collecthealthdata.data.local.TrackedDataEntity
import com.example.collecthealthdata.domain.repository.MessageRepository
import com.example.collecthealthdata.domain.repository.TrackedDataRepository
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.serializer
import javax.inject.Inject

private const val TAG = "SendMessageUseCase"
private const val MESSAGE_PATH = "/msg"

class SendMessageUseCase @Inject constructor(
    private val messageRepository: MessageRepository,
    private val trackedDataRepository: TrackedDataRepository,
    private val getCapableNodes: GetCapableNodes
) {
    suspend operator fun invoke(): Boolean {

        val nodes = getCapableNodes()

        return if (nodes.isNotEmpty()) {

            val node = nodes.first()
            // Flow<List<TrackedDataEntity>> → List<TrackedDataEntity>
            val trackedDataList = trackedDataRepository.getAll().first()

            val message = encodeMessage(trackedDataList)
            messageRepository.sendMessage(message, node, MESSAGE_PATH)

            true

        } else {
            Log.i(TAG, "Ain't no nodes around")
            false
        }
    }

    private fun encodeMessage(entityList: List<TrackedDataEntity>): String {
        TODO()
        return Json.encodeToString(ListSerializer(TrackedDataEntity.serializer()), entityList)
    }
}