package com.example.data

data class HoldingCompany(
    val instanceId: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val subsidiaries: List<OwnedBusiness> = emptyList(),
    val isHolding: Boolean = true,
    val ownershipPercentage: Float = 100.0f,
    val isPublic: Boolean = false,
    val holdingCash: Double = 0.0
)
