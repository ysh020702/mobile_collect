package com.example.collecthealthdata.domain.usecase

import com.example.collecthealthdata.data.repository.CapabilityRepository
import com.google.android.gms.wearable.Node
import javax.inject.Inject

private const val CAPABILITY = "wear"

class GetCapableNodes @Inject constructor(
    private val capabilityRepository: CapabilityRepository
) {
    suspend operator fun invoke(): Set<Node> {
        return capabilityRepository.getNodesForCapability(
            CAPABILITY,
            capabilityRepository.getCapabilitiesForReachableNodes()
        )
    }
}