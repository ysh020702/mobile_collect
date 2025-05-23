package com.example.collecthealthdata.data.repositoryimpl

import com.example.collecthealthdata.domain.repository.CapabilityRepository
import android.util.Log
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.Node
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "CapabilitiesRepositoryImpl"

@Singleton
class CapabilityRepositoryImpl @Inject constructor(
    private val capabilityClient: CapabilityClient
) : CapabilityRepository {

    override suspend fun getCapabilitiesForReachableNodes(): Map<Node, Set<String>> {
        Log.i(TAG, "🔍 Fetching all reachable capabilities...")
        
        val allCapabilities =
            capabilityClient.getAllCapabilities(CapabilityClient.FILTER_ALL).await()
        Log.i(TAG, "Capabilities fetched: $allCapabilities")

        val allReachableNodes = mutableSetOf<Node>()
        val capabilityMap = mutableMapOf<Node, MutableSet<String>>()

        for ((capabilityName, capabilityInfo) in allCapabilities) {
            for (node in capabilityInfo.nodes) {
                allReachableNodes.add(node)
                val set = capabilityMap.getOrPut(node) { mutableSetOf() }
                set.add(capabilityName)
            }
        }

        // 로그: 모든 연결된 노드 및 그들의 capability 목록 출력
        for (node in allReachableNodes) {
            val caps = capabilityMap[node]?.joinToString(", ") ?: "없음"
            Log.i(TAG, "✅ 연결된 노드: ${node.displayName} (${node.id}) - Capabilities: [$caps]")
        }

        // 연결된 노드가 아예 없는 경우
        if (allReachableNodes.isEmpty()) {
            Log.w(TAG, "⚠️ 연결된 노드가 없습니다. (Reachable 노드 없음)")
        }

        return capabilityMap
    }


    override suspend fun getNodesForCapability(
        capability: String,
        allCapabilities: Map<Node, Set<String>>
    ): Set<Node> {
        return allCapabilities.filterValues { capability in it }.keys
    }
}