package com.example.collecthealthdata.data.usecase

import com.example.collecthealthdata.domain.HelpFunctions.Companion.toSerializable
import android.util.Log
import com.example.collecthealthdata.domain.local.TrackedDataSerializable
import com.example.collecthealthdata.domain.repository.MessageRepository
import com.example.collecthealthdata.domain.repository.TrackedDataRepository
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
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

        if(nodes.isEmpty()){
            Log.w(TAG, "No capable nodes found")
            return false
        }

        val node = nodes.first()
        val entityList = trackedDataRepository.getAll().first()
        if (entityList.isEmpty()) {
            Log.i(TAG, "No message to send")
        }
        var serializableList = entityList.map{toSerializable(it)}


        val message = encodeMessage(serializableList)
        val result = messageRepository.sendMessage(message, node, MESSAGE_PATH)
        if (result) {
            //TODO: DB삭제 주석 제거하기~~~
            trackedDataRepository.deleteAll()
            Log.i(TAG, "Clean Database")
        }else{
            //TODO: 전송 실패 로직!
            Log.e(TAG, "Failed to send entity")
        }
        Log.i(TAG, "Messages has Been Sent")

        return true// 모두 전송 성공 시
    }

    private fun encodeMessage(entityList: List<TrackedDataSerializable>): String {
        return Json.encodeToString(entityList)
    }


}
