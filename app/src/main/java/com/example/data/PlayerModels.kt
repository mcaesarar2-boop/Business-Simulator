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

data class LifestyleItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val tabCategory: String, // "langganan", "gadget", "ekspedisi", "wellness", "filantropi", dll.
    var sectionName: String, // Contoh: "Entertainment", "Productivity", "Vehicles"
    var name: String,
    var price: Long,
    var imgUrl: String, // Menyimpan link URL gambar (PNG/SVG/JPG)
    var desc: String, // Deskripsi item
    var isActive: Boolean = false, // Untuk sistem toggle/langganan
    var isOwned: Boolean = false, // Untuk sistem one-time purchase
    val isCustom: Boolean = false // Penanda jika ini buatan pemain
)

data class TravelDestination(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val region: String,
    val pricePerDay: Long, // Harga dasar per hari
    val imageUrl: String,
    val isCustom: Boolean = false
)

val defaultTravelDestinations = listOf(
    TravelDestination(name = "Disneyland VIP Tour", region = "Orlando, USA", pricePerDay = 2000L, imageUrl = "https://images.unsplash.com/photo-1545231027-63b3f1626a5e?auto=format&fit=crop&w=500&q=80"),
    TravelDestination(name = "Maldives Private Island Retreat", region = "Maldives", pricePerDay = 5000L, imageUrl = "https://images.unsplash.com/photo-1514282401047-d79a71a590e8?auto=format&fit=crop&w=500&q=80"),
    TravelDestination(name = "Alps Luxury Ski Chalet", region = "Zermatt, Switzerland", pricePerDay = 8000L, imageUrl = "https://images.unsplash.com/photo-1502784444187-359ac186c5bb?auto=format&fit=crop&w=500&q=80"),
    TravelDestination(name = "Necker Island Sanctuary", region = "British Virgin Islands", pricePerDay = 15000L, imageUrl = "https://images.unsplash.com/photo-1548574505-5e239809ee19?auto=format&fit=crop&w=500&q=80"),
    TravelDestination(name = "Kyoto Imperial Villa Retreat", region = "Kyoto, Japan", pricePerDay = 4000L, imageUrl = "https://images.unsplash.com/photo-1493976040374-85c8e12f0c0e?auto=format&fit=crop&w=500&q=80")
)

val defaultLifestyleItems = listOf(
    // LANGGANAN
    LifestyleItem(tabCategory = "langganan", sectionName = "Entertainment", name = "Spotify Premium", price = 10L, imgUrl = "", desc = "Dengarkan musik resolusi tinggi tanpa gangguan."),
    LifestyleItem(tabCategory = "langganan", sectionName = "Entertainment", name = "YouTube Premium", price = 15L, imgUrl = "", desc = "Nonton offline & bebas iklan untuk video tech startup."),
    LifestyleItem(tabCategory = "langganan", sectionName = "Entertainment", name = "Disney+ & Netflix Bundle", price = 30L, imgUrl = "", desc = "Paket hiburan film akhir pekan 4K HDR."),
    LifestyleItem(tabCategory = "langganan", sectionName = "Entertainment", name = "Apple TV+", price = 15L, imgUrl = "", desc = "Katalog serial orisinal kualitas sinematik terbaik."),
    LifestyleItem(tabCategory = "langganan", sectionName = "Productivity", name = "Adobe Creative Cloud & CapCut Pro", price = 60L, imgUrl = "", desc = "Aset pengeditan video startup marketing."),
    LifestyleItem(tabCategory = "langganan", sectionName = "Productivity", name = "Google One 30TB", price = 150L, imgUrl = "", desc = "Penyimpanan cloud raksasa untuk data & sasis kecerdasan buatan."),

    // GADGET
    LifestyleItem(tabCategory = "gadget", sectionName = "Mobile", name = "Smartphone Lipat", price = 2000L, imgUrl = "", desc = "Layar ganda fleksibel terkini untuk mobilitas level eksekutif."),
    LifestyleItem(tabCategory = "gadget", sectionName = "Mobile", name = "Smartwatch Titanium", price = 1000L, imgUrl = "", desc = "Pelacak kebugaran berlapis titanium dengan sinkronisasi satelit."),
    LifestyleItem(tabCategory = "gadget", sectionName = "Work", name = "Laptop Pribadi", price = 5000L, imgUrl = "", desc = "Grafis termutakhir dengan prosesor kustom ultra hemat daya."),
    LifestyleItem(tabCategory = "gadget", sectionName = "Entertainment", name = "Computer Gaming Super", price = 15000L, imgUrl = "", desc = "Pendingin cairan dual-loop dengan sasis pencahayaan RGB kustom."),
    LifestyleItem(tabCategory = "gadget", sectionName = "Infrastructure", name = "Computer AI & Server", price = 45000L, imgUrl = "", desc = "Server cluster modular mandiri berisi 4 kartu akselerator AI."),

    // EKSPEDISI
    LifestyleItem(tabCategory = "ekspedisi", sectionName = "Leisure", name = "Couples Private Getaway", price = 50000L, imgUrl = "", desc = "Resor pulau tropis terpencil ultra mewah dengan pelayan pribadi 24 jam."),
    LifestyleItem(tabCategory = "ekspedisi", sectionName = "Leisure", name = "First-Class Europe Trip", price = 120000L, imgUrl = "", desc = "Terbang first-class ke 5 ibu kota monarki Eropa & menginap di istana kastel orisinal."),
    LifestyleItem(tabCategory = "ekspedisi", sectionName = "High-End Adventure", name = "Multi-Country Overland Expedition", price = 300000L, imgUrl = "", desc = "Perjalanan konvoi helikopter kustom menyusuri dataran tinggi bersalju & gurun murni."),

    // WELLNESS
    LifestyleItem(tabCategory = "wellness", sectionName = "Health", name = "Personal Trainer & Chef", price = 15000L, imgUrl = "", desc = "Kombinasi nutrisi kustom organik bernutrisi tinggi & latihan kardio personal harian."),
    LifestyleItem(tabCategory = "wellness", sectionName = "Health", name = "Private Doctor On-Call", price = 20000L, imgUrl = "", desc = "Tim medis klinis elit pribadi yang siaga 24 jam dengan peralatan diagnostik portabel canggih."),
    LifestyleItem(tabCategory = "wellness", sectionName = "Security", name = "Tim Bodyguard Elite", price = 50000L, imgUrl = "", desc = "Rejimen penjaga bersenjata bersertifikasi militer yang mengamankan rute perjalanan & kediaman holding."),

    // FILANTROPI
    LifestyleItem(tabCategory = "filantropi", sectionName = "Social Impact", name = "Yayasan Sosial CEO", price = 100000L, imgUrl = "", desc = "Mendirikan yayasan kesejahteraan masyarakat untuk mengurangi kemiskinan perkotaan."),
    LifestyleItem(tabCategory = "filantropi", sectionName = "Education", name = "Beasiswa Global Muda", price = 250000L, imgUrl = "", desc = "Program beasiswa penuh universitas top dunia bagi talenta lokal berprestasi."),
    LifestyleItem(tabCategory = "filantropi", sectionName = "Healthcare", name = "Pusat Riset Medis", price = 1000000L, imgUrl = "", desc = "Mendanai laboratorium penelitian obat langka dan terapi mutakhir.")
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
    val allSubscriptions: List<LifestyleItem> = defaultLifestyleItems,
    val monthlyLifestyleCost: Long = 0L,
    val ownedGadgets: List<String> = emptyList(),
    val travelHistory: Int = 0,
    val totalCharityDonated: Long = 0L,
    val travelDestinations: List<TravelDestination> = defaultTravelDestinations,
    val totalTripsTaken: Int = 0,
    val foundationLegacyPoints: Long = 0L,
    val foundations: List<com.example.data.FoundationEntity> = emptyList()
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