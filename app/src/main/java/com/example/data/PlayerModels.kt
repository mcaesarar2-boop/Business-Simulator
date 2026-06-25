package com.example.data

data class MonthlyFinancialRecord(
    val monthTick: Int,      // Penanda bulan ke-berapa
    val totalRevenue: Long,  // Pemasukan kotor bulan itu
    val totalExpense: Long,  // Pengeluaran bulan itu
    val netIncome: Long      // Laba Bersih bulan itu
)

data class PrivateLedgerRecord(
    val monthTick: Int,
    val title: String, // Contoh: "Gaji CEO (Mega Holding)", "Pajak Pribadi", "Dividen Saham (AAPL)"
    val amount: Long,
    val isIncome: Boolean // true = Uang Masuk (Hijau), false = Uang Keluar (Merah)
)

data class OwnedStock(val ticker: String, val averagePrice: Double, val shares: Long, val isIntegratedToHolding: Boolean = false)

data class TvProgram(
    val id: String,
    val title: String,
    val type: String,
    val productionCost: Double,
    val monthlyAdRevenue: Double,
    val rating: Double,
    val active: Boolean = true,
    val remainingMonths: Int = -1, // -1 means infinite/manual cancel
    val isOriginalIP: Boolean = true,
    val totalAccumulatedProfit: Double = 0.0,
    val monthsAired: Int = 0,
    val currentOperationalCost: Double = productionCost * 0.15,
    val previousRating: Double = rating,
    val timeSlots: List<String> = emptyList()
)

data class TimeDeposit(
    val id: String,
    val principal: Long,
    val durationMonths: Int,
    val monthsRemaining: Int,
    val interestRate: Double
)

enum class ProjectType {
    CLIENT_B2B, INDEPENDENT_SAAS, ECOSYSTEM_SYNERGY
}

enum class ProjectStatus {
    DEVELOPMENT, MAINTENANCE, COMPLETED
}

data class AppProject(
    val id: String,
    val title: String,
    val type: ProjectType,
    val budgetCost: Double,
    val targetRevenue: Double,
    val devTimeMonths: Int,
    val currentMonth: Int = 0,
    val status: ProjectStatus = ProjectStatus.DEVELOPMENT,
    val targetBusinessId: String? = null
)

data class MegaHoldingState(
    val isActive: Boolean = false,
    val companyName: String = "",
    val includesInvestments: Boolean = false,
    val investmentCompanyName: String = "",
    val ownershipPercentage: Double = 100.0
)

data class PlayerState(
    val lastSavedTimeMs: Long = System.currentTimeMillis(),
    val cash: Long = 5000,
    val netWorth: Long = 5000,
    val inGameMonth: Int = 1,
    val inGameYear: Int = 1,
    val lastMonthIncome: Long = 0,
    val lastMonthExpenses: Long = 0,
    val lastMonthNetProfit: Long = 0,
    val maxBusinessSlots: Int = 11,
    val ownedBusinesses: List<OwnedBusiness> = emptyList(),
    val ownedStocks: List<OwnedStock> = emptyList(),
    val corporateStockPortfolio: List<OwnedStock> = emptyList(),
    val privateStockPortfolio: List<OwnedStock> = emptyList(),
    val ownedProperties: List<com.example.data.OwnedProperty> = emptyList(),
    val ownedCrypto: List<com.example.data.OwnedCrypto> = emptyList(),
    val activeStartupInvestments: List<com.example.data.ActiveStartupInvestment> = emptyList(),
    val ownedCollections: List<com.example.data.OwnedCollection> = emptyList(),
    val ownedMetals: Map<String, Double> = emptyMap(),
    val ownedMetalsAveragePrices: Map<String, Double> = emptyMap(),
    val ownedHouses: List<com.example.data.OwnedHousing> = emptyList(),
    val rentedHouses: List<com.example.data.RentedHousing> = emptyList(),
    val customBusinessCatalog: List<BusinessCatalogItem> = emptyList(),
    val taxLegalReport: com.example.data.TaxLegalReport = com.example.data.TaxLegalReport(),
    val rebrandedCompanies: Map<String, String> = emptyMap(),
    val timeDeposits: List<TimeDeposit> = emptyList(),
    val holdingCompanies: List<HoldingCompany> = emptyList(),
    val activeTvPrograms: List<TvProgram> = emptyList(),
    val ipLibraryHistory: List<TvProgram> = emptyList(),
    val appProjects: List<AppProject> = emptyList(),
    val megaHolding: MegaHoldingState = MegaHoldingState(),
    val customMarketAssets: List<com.example.data.PropertyItem>? = null,
    val customCollectionAssets: List<com.example.data.CollectionItem>? = null,
    val customHousingAssets: List<com.example.data.HousingItem>? = null,
    val personalDebt: Long = 0L,
    val companyOwnershipPercent: Double = 100.0,
    val monthlyCeoSalary: Long = 0L,
    val currentCeoSalaryPercent: Double = 4.0,
    val pendingCeoSalaryPercent: Double? = null,
    val boardApprovalMonthsLeft: Int = 0,
    val lastSalaryRequestMonth: Int = -12,
    val boardReplyMessage: String? = null,
    val privateBalance: Long = 0L,
    val currentDividendPercent: Double = 0.0,
    val pendingDividendPercent: Double? = null,
    val dividendApprovalMonthsLeft: Int = 0,
    val lastDividendRequestMonth: Int = -12,
    val currentTantiemPercent: Double = 0.0,
    val pendingTantiemPercent: Double? = null,
    val tantiemApprovalMonthsLeft: Int = 0,
    val retainedEarnings: Long = 0L,
    val totalTaxPaid: Long = 0L,
    val corporateTaxPaid: Long = 0L,
    val personalTaxPaid: Long = 0L,
    val isSptReportedThisYear: Boolean = true,
    val consecutiveUnreportedSpt: Int = 0,
    val privateTaxServiceLevel: Int = 0,
    val privateLedgerHistory: List<PrivateLedgerRecord> = emptyList(),
    val financialHistory: List<com.example.data.MonthlyFinancialRecord> = emptyList(),
    val activeSubscriptions: List<String> = emptyList(),
    val monthlyLifestyleCost: Long = 0L,
    val ownedGadgets: List<String> = emptyList(),
    val travelHistory: Int = 0,
    val totalCharityDonated: Long = 0L
)

fun getBusinessStats(owned: OwnedBusiness, catalog: BusinessCatalogItem, playerState: PlayerState? = null): Pair<Long, Long> {
    var flatRev = 0L
    var multRev = 1.0f
    var flatMaint = 0L
    var multMaint = 1.0f

    catalog.upgrades.forEach { upgrade ->
        val level = owned.upgradeLevels[upgrade.id] ?: if (owned.purchasedUpgrades.contains(upgrade.id)) 1 else 0
        if (level > 0) {
            flatRev += upgrade.revenueFlatBoost * level
            repeat(level) { multRev *= upgrade.revenueMultiplier }
            flatMaint += upgrade.maintenanceFlatReduction * level
            repeat(level) { multMaint *= upgrade.maintenanceMultiplier }
        }
    }

    var totalRev = (owned.customRevenue ?: ((catalog.monthlyRevenue + flatRev) * multRev).toLong())
    totalRev = (totalRev * owned.synergyMultiplier).toLong()
    var totalMaint = ((catalog.monthlyMaintenanceCost - flatMaint) * multMaint).toLong().coerceAtLeast(0)

    if (playerState != null && catalog.id == "media_tv") {
        var tvRev = 0.0
        var tvMaint = 0.0
        playerState.activeTvPrograms.forEach { prog ->
            if (prog.active) {
                tvRev += prog.monthlyAdRevenue
                tvMaint += prog.currentOperationalCost
            }
        }
        totalRev += tvRev.toLong()
        totalMaint += tvMaint.toLong()
    }

    if (catalog.id == "fine_dining") {
        val lvl = owned.level
        totalRev = when {
            lvl in 1..10 -> 5000L + (lvl * 2000L)
            lvl in 11..30 -> 25000L + ((lvl - 10) * 8000L)
            lvl in 31..40 -> 185000L + ((lvl - 30) * 50000L)
            lvl in 41..50 -> 685000L + ((lvl - 40) * 200000L)
            else -> 685000L + ((lvl - 40) * 200000L)
        }
    }

    if (catalog.category == com.example.data.BusinessCategory.HOSPITALITY) {
        totalRev = owned.hospitalityProperties.sumOf { it.lastMonthRevenue }
        totalMaint = owned.hospitalityProperties.sumOf { it.lastMonthExpense }
    }

    if (owned.isUpgradingRealTime) {
        totalRev = 0L
    }

    return Pair(totalRev, totalMaint)
}

fun getBusinessValuation(owned: OwnedBusiness, catalog: BusinessCatalogItem): Long {
    var totalUpgradeCost = 0L
    
    // Calculate regular catalog upgrades
    for ((upgradeId, level) in owned.upgradeLevels) {
        val upgradeDef = catalog.upgrades.find { it.id == upgradeId }
        if (upgradeDef != null) {
            var currentCost = upgradeDef.baseCost.toDouble()
            for (i in 0 until level) {
                totalUpgradeCost += currentCost.toLong()
                currentCost *= upgradeDef.costMultiplier
            }
        }
    }
    
    // Calculate Content Creator upgrades based on its exponential curve
    if (owned.catalogId == "content_creator") {
        totalUpgradeCost = 0L
        var ccCost = 500.0
        for (i in 1 until owned.level) {
            totalUpgradeCost += ccCost.toLong()
            ccCost *= 1.18
        }
    }

    val (revenue, maintenance) = getBusinessStats(owned, catalog)
    val netProfit = revenue - maintenance
    val annualProfit = if (netProfit > 0) netProfit * 12 else 0

    return catalog.costToBuy + totalUpgradeCost + annualProfit + owned.extraValuation
}

fun getUpgradeCost(upgrade: BusinessUpgrade, currentLevel: Int): Long {
    var costMultiplierTotal = 1.0f
    repeat(currentLevel) { costMultiplierTotal *= upgrade.costMultiplier }
    return (upgrade.baseCost * costMultiplierTotal).toLong()
}

fun getCatalogItem(catalogId: String, playerState: PlayerState): BusinessCatalogItem? {
    val found = businessCatalog.find { it.id == catalogId } ?: playerState.customBusinessCatalog.find { it.id == catalogId }
    if (found != null) return found

    if (catalogId == "umkm_foodcart") return BusinessCatalogItem("umkm_foodcart", "Gerobak Gorengan (Legacy)", BusinessCategory.CULINARY, costToBuy = 500, monthlyRevenue = 600, monthlyMaintenanceCost = 150)
    if (catalogId == "umkm_laundry") return BusinessCatalogItem("umkm_laundry", "Laundry Kiloan (Legacy)", BusinessCategory.RETAIL, costToBuy = 1500, monthlyRevenue = 1200, monthlyMaintenanceCost = 400)
    
    return null
}

data class Billionaire(val id: Int, val name: String, val netWorth: Long, val rank: Int = 0)