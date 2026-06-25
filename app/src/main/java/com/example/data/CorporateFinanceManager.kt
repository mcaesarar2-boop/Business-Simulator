package com.example.data

object CorporateFinanceManager {
    
    // Synergy Multiplier (+15% bonus)
    const val SYNERGY_MULTIPLIER = 1.15f

    fun calculateHoldingValuation(
        holding: HoldingCompany,
        playerState: PlayerState
    ): Long {
        var totalValuation = 0L
        holding.subsidiaries.forEach { sub ->
            val catalogItem = getCatalogItem(sub.catalogId, playerState)
            if (catalogItem != null) {
                totalValuation += getBusinessValuation(sub, catalogItem)
            }
        }
        return (totalValuation * SYNERGY_MULTIPLIER).toLong()
    }

    fun calculateHoldingMonthlyRevenue(
        holding: HoldingCompany,
        playerState: PlayerState
    ): Long {
        var totalRevenue = 0L
        holding.subsidiaries.forEach { sub ->
            val catalogItem = getCatalogItem(sub.catalogId, playerState)
            if (catalogItem != null) {
                val (baseRev, _) = getBusinessStats(sub, catalogItem, playerState) // We take gross revenue here for calculation, or net profit? Instructions: "Total Pendapatan Semua Divisi"
                val rev = if (catalogItem.isFluctuating) {
                    (baseRev / 1.5).toLong() // Average projection
                } else {
                    baseRev
                }
                totalRevenue += rev
            }
        }
        // Ownership percentage applies to the overall subsidiary revenue
        return (totalRevenue * SYNERGY_MULTIPLIER * (holding.ownershipPercentage / 100.0f)).toLong()
    }

    fun calculateHoldingMonthlyMaintenance(
        holding: HoldingCompany,
        playerState: PlayerState
    ): Long {
        var totalMaint = 0L
        holding.subsidiaries.forEach { sub ->
            val catalogItem = getCatalogItem(sub.catalogId, playerState)
            if (catalogItem != null) {
                val (_, baseMaint) = getBusinessStats(sub, catalogItem, playerState)
                totalMaint += baseMaint
            }
        }
        // Maintenance is also split according to ownership? Usually yes, or holding pays full. Let's apply ownership
        return (totalMaint * SYNERGY_MULTIPLIER * (holding.ownershipPercentage / 100.0f)).toLong()
    }

    fun processIPO(
        holding: HoldingCompany,
        percentToSell: Float,
        playerState: PlayerState
    ): Pair<HoldingCompany, Long> {
        val currentValuation = calculateHoldingValuation(holding, playerState)
        val cashGained = (currentValuation * (percentToSell / 100f)).toLong()
        val updatedHolding = holding.copy(
            isPublic = true,
            ownershipPercentage = holding.ownershipPercentage - percentToSell
        )
        return Pair(updatedHolding, cashGained)
    }

    fun processDivestment(
        holding: HoldingCompany,
        playerState: PlayerState
    ): Long {
        val currentValuation = calculateHoldingValuation(holding, playerState)
        return (currentValuation * (holding.ownershipPercentage / 100f)).toLong()
    }
}
