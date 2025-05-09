package com.example.collecthealthdata.domain.repositoryimpl

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
        Log.i(TAG, "getCapabilities()")

        val allCapabilities =
            capabilityClient.getAllCapabilities(CapabilityClient.FILTER_REACHABLE).await()

        return allCapabilities.flatMap { (capability, capabilityInfo) ->
            capabilityInfo.nodes.map {
                it to capability
            }
        }
            .groupBy(
                keySelector = { it.first },
                valueTransform = { it.second }
            )
            .mapValues { it.value.toSet() }
    }

    override suspend fun getNodesForCapability(
        capability: String,
        allCapabilities: Map<Node, Set<String>>
    ): Set<Node> {
        return allCapabilities.filterValues { capability in it }.keys
    }
}