package com.example.collecthealthdata.data.usecase

import android.content.Context
import com.example.collecthealthdata.domain.HelpFunctions.Companion.toSerializable
import android.util.Log
import android.widget.Toast
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
    private val getCapableNodes: GetCapableNodes,
    @Inject private val context: Context
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
            //불러올 데이터가 없을 경우
            Toast.makeText(context, "No data to send", Toast.LENGTH_SHORT).show()
            Log.i(TAG, "No message to send")
            return true
        }
        
        var successCount = 0
        for (entity in entityList){
            val serializable = entityList.map{toSerializable(it)}
            val message = encodeMessage(serializable)//하나만 보냄
            
            val result = messageRepository.sendMessage(message, node, MESSAGE_PATH)
            if (result) {
                trackedDataRepository.deleteById(entity.id)
                Log.i(TAG, "Sent and deleted entity ${entity.id}")
                successCount++
            }else{
                Log.e(TAG, "Failed to send entity: ${entity.id}")
            }
        }

        //얼마나 성공했는지 본다
        Log.i(TAG, "Total sent: $successCount / ${entityList.size}")
        //모두 성공했으면 true
        return successCount == entityList.size
    }

    private fun encodeMessage(entityList: List<TrackedDataSerializable>): String {
        return Json.encodeToString(entityList)
    }
}
