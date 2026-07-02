package com.example.data

enum class BusinessCategory { CULINARY, RETAIL, PROPERTY, CREATIVE, LOGISTICS, ENTERTAINMENT, FINANCE, CORPORATION, TECHNOLOGY, AVIATION, THEME_PARK_HOLDING, HOSPITALITY, CRUISE_LINE }

data class DummyAircraft(val id: String, val model: String, val type: String, val price: Long, val maxPax: Int, val range: String, val deliveryTime: Int, val leasePrice: Long = 0L)

data class DummyHubUpgrade(val id: String, val name: String, val cost: Long, val buildTime: Int, val level: Int, val unlocks: List<String>)

val DUMMY_AIRCRAFTS = listOf(
    DummyAircraft("atr72", "ATR 72-600", "PROPELLER", 20000000L, 72, "SHORT", 1),
    DummyAircraft("a320", "Airbus A320neo", "NARROW_BODY", 55000000L, 180, "MEDIUM", 2),
    DummyAircraft("b777", "Boeing 777-300ER", "WIDE_BODY", 150000000L, 350, "LONG", 4)
)

val AVIATION_AIRCRAFT_CATALOG = listOf(
    // --- TURBOPROP (Baling-baling) - Rute Pendek / Perintis ---
    DummyAircraft("dh8_100", "De Havilland Dash 8-100", "PROPELLER", 20000000L, 37, "SHORT", 3, 80000L),
    DummyAircraft("atr42_600", "ATR 42-600", "PROPELLER", 21000000L, 50, "SHORT", 3, 120000L),
    DummyAircraft("atr72_500", "ATR 72-500", "PROPELLER", 22000000L, 74, "SHORT", 4, 130000L),
    DummyAircraft("dh8_300", "De Havilland Dash 8-300", "PROPELLER", 25000000L, 50, "SHORT", 4, 100000L),
    DummyAircraft("atr72_600", "ATR 72-600", "PROPELLER", 26000000L, 78, "SHORT", 5, 150000L),
    DummyAircraft("q400", "Dash 8 Q400", "PROPELLER", 32000000L, 90, "SHORT", 6, 180000L),

    // --- REGIONAL JET - Rute Menengah/Singkat ---
    DummyAircraft("e170", "Embraer E170", "REGIONAL_JET", 25000000L, 78, "SHORT_MEDIUM", 6, 150000L),
    DummyAircraft("crj700", "Bombardier CRJ700", "REGIONAL_JET", 27000000L, 78, "SHORT_MEDIUM", 6, 180000L),
    DummyAircraft("e175", "Embraer E175", "REGIONAL_JET", 28000000L, 88, "SHORT_MEDIUM", 7, 200000L),
    DummyAircraft("e190", "Embraer E190", "REGIONAL_JET", 32000000L, 114, "SHORT_MEDIUM", 8, 250000L),
    DummyAircraft("crj900", "Bombardier CRJ900", "REGIONAL_JET", 35000000L, 90, "SHORT_MEDIUM", 7, 200000L),
    DummyAircraft("e195", "Embraer E195", "REGIONAL_JET", 38000000L, 124, "SHORT_MEDIUM", 9, 300000L),
    DummyAircraft("crj1000", "Bombardier CRJ1000", "REGIONAL_JET", 45000000L, 104, "SHORT_MEDIUM", 9, 250000L),
    DummyAircraft("m90", "Mitsubishi SpaceJet M90", "REGIONAL_JET", 47000000L, 88, "SHORT_MEDIUM", 10, 250000L),

    // --- NARROW BODY - Tulang Punggung Maskapai ---
    DummyAircraft("e190_e2", "Embraer E190-E2", "NARROW_BODY", 60000000L, 114, "MEDIUM", 12, 180000L),
    DummyAircraft("e195_e2", "Embraer E195-E2", "NARROW_BODY", 70000000L, 146, "MEDIUM", 12, 250000L),
    DummyAircraft("a220_100", "Airbus A220-100", "NARROW_BODY", 81000000L, 135, "MEDIUM", 13, 250000L),
    DummyAircraft("a220_300", "Airbus A220-300", "NARROW_BODY", 91000000L, 160, "MEDIUM", 14, 280000L),
    DummyAircraft("b737m7", "Boeing 737 MAX 7", "NARROW_BODY", 100000000L, 172, "MEDIUM", 15, 380000L),
    DummyAircraft("a319neo", "Airbus A319neo", "NARROW_BODY", 100000000L, 160, "MEDIUM", 15, 350000L),
    DummyAircraft("a320neo", "Airbus A320neo", "NARROW_BODY", 110000000L, 195, "MEDIUM", 16, 400000L),
    DummyAircraft("b737m8", "Boeing 737 MAX 8", "NARROW_BODY", 120000000L, 210, "MEDIUM", 16, 400000L),
    DummyAircraft("b737m9", "Boeing 737 MAX 9", "NARROW_BODY", 125000000L, 220, "MEDIUM", 17, 430000L),
    DummyAircraft("a321neo", "Airbus A321neo", "NARROW_BODY", 130000000L, 244, "MEDIUM", 18, 460000L),
    DummyAircraft("b737m10", "Boeing 737 MAX 10", "NARROW_BODY", 135000000L, 230, "MEDIUM", 18, 450000L),

    // --- WIDE BODY & JUMBO JET - Rute Internasional Jarak Jauh ---
    DummyAircraft("b747_200", "Boeing 747-200 (Classic)", "WIDE_BODY", 39000000L, 480, "LONG", 12, 200000L),
    DummyAircraft("b747_300", "Boeing 747-300", "WIDE_BODY", 82000000L, 496, "LONG", 15, 300000L),
    DummyAircraft("b747_400", "Boeing 747-400", "WIDE_BODY", 230000000L, 660, "LONG", 24, 550000L),
    DummyAircraft("b787_8", "Boeing 787-8 Dreamliner", "WIDE_BODY", 250000000L, 242, "LONG", 26, 950000L),
    DummyAircraft("a330_800neo", "Airbus A330-800neo", "WIDE_BODY", 260000000L, 406, "LONG", 27, 800000L),
    DummyAircraft("a340_600", "Airbus A340-600", "WIDE_BODY", 280000000L, 440, "LONG", 24, 700000L),
    DummyAircraft("a330_900neo", "Airbus A330-900neo", "WIDE_BODY", 290000000L, 440, "LONG", 28, 900000L),
    DummyAircraft("b787_9", "Boeing 787-9 Dreamliner", "WIDE_BODY", 300000000L, 290, "LONG", 29, 1050000L),
    DummyAircraft("a350_900", "Airbus A350-900", "WIDE_BODY", 308000000L, 440, "LONG", 30, 110000L),
    DummyAircraft("b787_10", "Boeing 787-10 Dreamliner", "WIDE_BODY", 340000000L, 330, "LONG", 32, 1150000L),
    DummyAircraft("b777_300er", "Boeing 777-300ER", "WIDE_BODY", 340000000L, 396, "LONG", 31, 120000L),
    DummyAircraft("a350_1000", "Airbus A350-1000", "WIDE_BODY", 356000000L, 480, "LONG", 34, 130000L),
    DummyAircraft("b777_8", "Boeing 777-8", "WIDE_BODY", 410000000L, 384, "LONG", 35, 1400000L),
    DummyAircraft("b747_8", "Boeing 747-8 Intercontinental", "WIDE_BODY", 418000000L, 467, "LONG", 36, 1800000L),
    DummyAircraft("b777_9", "Boeing 777-9", "WIDE_BODY", 440000000L, 426, "LONG", 36, 1600000L),
    DummyAircraft("a380", "Airbus A380-800", "WIDE_BODY", 445000000L, 853, "LONG", 48, 2200000L),

    // --- HELICOPTER - Penerbangan VIP / Ultra Short (Helipad) ---
    DummyAircraft("r44", "Robinson R44 Raven II", "HELICOPTER", 650000L, 3, "ULTRA_SHORT", 1, 7500L),
    DummyAircraft("r66", "Robinson R66 Turbine", "HELICOPTER", 1300000L, 4, "ULTRA_SHORT", 1, 12000L),
    DummyAircraft("bell206", "Bell 206 JetRanger", "HELICOPTER", 1500000L, 4, "ULTRA_SHORT", 2, 15000L),
    DummyAircraft("bell505", "Bell 505 Jet Ranger X", "HELICOPTER", 1500000L, 4, "ULTRA_SHORT", 2, 16000L),
    DummyAircraft("bell407", "Bell 407GXi", "HELICOPTER", 3500000L, 6, "ULTRA_SHORT", 3, 22000L),
    DummyAircraft("h125", "Airbus H125", "HELICOPTER", 4000000L, 5, "ULTRA_SHORT", 3, 30000L),
    DummyAircraft("aw119", "Leonardo AW119Kx", "HELICOPTER", 4000000L, 7, "ULTRA_SHORT", 3, 32000L),
    DummyAircraft("h130", "Airbus H130", "HELICOPTER", 4500000L, 6, "ULTRA_SHORT", 3, 35000L),
    DummyAircraft("aw109", "Leonardo AW109 GrandNew", "HELICOPTER", 6500000L, 7, "ULTRA_SHORT", 4, 70000L),
    DummyAircraft("h135", "Airbus H135", "HELICOPTER", 7000000L, 6, "ULTRA_SHORT", 4, 55000L),
    DummyAircraft("bell429", "Bell 429", "HELICOPTER", 8000000L, 7, "ULTRA_SHORT", 4, 70000L),
    DummyAircraft("h145", "Airbus H145", "HELICOPTER", 10000000L, 9, "ULTRA_SHORT", 5, 80000L),
    DummyAircraft("s76d", "Sikorsky S-76D", "HELICOPTER", 13000000L, 12, "ULTRA_SHORT", 5, 150000L),
    DummyAircraft("aw139", "Leonardo AW139", "HELICOPTER", 14000000L, 15, "ULTRA_SHORT", 5, 180000L),
    DummyAircraft("mi8", "Mil Mi-8", "HELICOPTER", 15000000L, 24, "ULTRA_SHORT", 6, 220000L),
    DummyAircraft("h225", "Airbus H225 Super Puma", "HELICOPTER", 25000000L, 19, "ULTRA_SHORT", 6, 350000L),
    DummyAircraft("s92", "Sikorsky S-92", "HELICOPTER", 30000000L, 19, "ULTRA_SHORT", 6, 450000L)
)

data class GlobalAviationHubDef(
    val id: String,
    val city: String,
    val country: String,
    val baseCost: Long,
    val buildTime: Int,
    val maxSlots: Int
)

val GLOBAL_AVIATION_HUBS = listOf(
    // LOKAL / REGIONAL
    GlobalAviationHubDef("cgk", "Jakarta (CGK)", "Indonesia", 25000000L, 3, 10),
    GlobalAviationHubDef("dps", "Denpasar (DPS)", "Indonesia", 35000000L, 4, 8),
    GlobalAviationHubDef("sin", "Singapore (SIN)", "Singapore", 60000000L, 6, 15),
    GlobalAviationHubDef("bkk", "Bangkok (BKK)", "Thailand", 45000000L, 5, 12),
    
    // ASIA PASIFIK
    GlobalAviationHubDef("hnd", "Tokyo (HND)", "Japan", 80000000L, 8, 20),
    GlobalAviationHubDef("icn", "Seoul (ICN)", "South Korea", 70000000L, 7, 18),
    GlobalAviationHubDef("pek", "Beijing (PEK)", "China", 75000000L, 8, 25),
    GlobalAviationHubDef("syd", "Sydney (SYD)", "Australia", 65000000L, 7, 15),

    // TIMUR TENGAH & EROPA
    GlobalAviationHubDef("dxb", "Dubai (DXB)", "UAE", 150000000L, 12, 30),
    GlobalAviationHubDef("doh", "Doha (DOH)", "Qatar", 120000000L, 10, 25),
    GlobalAviationHubDef("lhr", "London (LHR)", "UK", 180000000L, 14, 20),
    GlobalAviationHubDef("cdg", "Paris (CDG)", "France", 160000000L, 12, 18),
    GlobalAviationHubDef("fra", "Frankfurt (FRA)", "Germany", 150000000L, 12, 20),

    // AMERIKA
    GlobalAviationHubDef("jfk", "New York (JFK)", "USA", 200000000L, 15, 25),
    GlobalAviationHubDef("lax", "Los Angeles (LAX)", "USA", 180000000L, 14, 25),
    GlobalAviationHubDef("atl", "Atlanta (ATL)", "USA", 160000000L, 12, 30)
)

val DUMMY_HUB_UPGRADES = listOf(
    DummyHubUpgrade("upg_dom", "Terminal Domestik Standard", 10000000L, 2, 1, listOf("SHORT")),
    DummyHubUpgrade("upg_intl_1", "Gerbang Internasional Dasar", 50000000L, 4, 2, listOf("MEDIUM")),
    DummyHubUpgrade("upg_vip", "VIP Executive Lounge", 25000000L, 3, 1, listOf("PREMIUM_TICKET")),
    DummyHubUpgrade("upg_intl_2", "Landasan Pacu Wide-Body", 100000000L, 6, 3, listOf("LONG")),
    DummyHubUpgrade("upg_cargo", "Fasilitas Kargo Udara", 40000000L, 3, 1, listOf("CARGO"))
)

data class Aircraft(
    val id: String = java.util.UUID.randomUUID().toString(),
    val type: String,
    val name: String,
    val capacity: Int,
    val isUsed: Boolean,
    var condition: Int = 100,
    val maintenanceCost: Double,
    val deliveryDelay: Int = 0
)

data class AircraftInstance(
    val id: String = java.util.UUID.randomUUID().toString(),
    val modelId: String, // 'atr72', 'a320', 'b777'
    val condition: Double = 100.0,
    val status: String = "DELIVERING", // DELIVERING, STANDBY, ASSIGNED, MAINTENANCE
    val monthsUntilDelivery: Int = 2,
    val stationedHubId: String? = null,
    val assignedRouteId: String? = null,
    val isLeased: Boolean = false,
    val leasePrice: Long = 0L
)

data class AviationHub(
    val id: String = java.util.UUID.randomUUID().toString(),
    val city: String,
    val baseCost: Long = 150000000L,
    val activeUpgrades: List<String> = emptyList(), // ID of upgrades completed
    val constructionQueue: List<HubConstructionItem> = emptyList(),
    val isConstructing: Boolean = false,
    val constructionMonthsLeft: Int = 0
)

data class HubConstructionItem(
    val upgradeId: String,
    val monthsRemaining: Int
)

data class FlightRoute(
    val id: String = java.util.UUID.randomUUID().toString(),
    val originHubId: String,
    val destination: String,
    val distanceCategory: String, // SHORT, MEDIUM, LONG
    val baseDemand: Int,
    val ticketPrice: Int,
    val assignedAircraftIds: List<String> = emptyList()
)

data class BusinessUpgrade(
    val id: String,
    val name: String,
    val description: String,
    val baseCost: Long,
    val costMultiplier: Float = 1.0f,
    val revenueMultiplier: Float = 1.0f,
    val maintenanceMultiplier: Float = 1.0f,
    val revenueFlatBoost: Long = 0,
    val maintenanceFlatReduction: Long = 0,
    val maxLevel: Int = 1
)

data class BusinessCatalogItem(
    val id: String,
    val name: String,
    val category: BusinessCategory,
    val imageUrl: String? = null,
    val costToBuy: Long,
    val monthlyRevenue: Long,
    val monthlyMaintenanceCost: Long,
    val isFluctuating: Boolean = false,
    val upgrades: List<BusinessUpgrade> = emptyList()
)

data class ActiveUpgrade(
    val selectedUpgradeId: String,
    val targetLevel: Int,
    val startTimeMs: Long,
    val finishTimeMs: Long
)

data class MovieProject(
    val title: String,
    val budget: Long,
    val genres: List<String>,
    val distributionScale: String,
    val reviewScore: Int,
    val boxOffice: Long,
    val netProfit: Long,
    val status: String = "FINISHED",
    val remainingMonths: Int = 0,
    val currentRevenue: Long = 0L,
    val targetMaxRevenue: Long = 0L,
    val productionPhase: String = "TAYANG",
    val productionDelayMonths: Int = 0,
    val promoBudget: Long = 0L,
    val scheduledMonth: Int? = null,
    val scheduledYear: Int? = null,
    val filmFormat: String = "Feature Film",
    val isQcPhase: Boolean = false,
    val internalScore: Int? = null,
    val isAwaitingRelease: Boolean = false,
    val scheduledReleaseDate: String? = null,
    var releaseMonth: Int? = null,
    var releaseYear: Int? = null,
    var licenseeName: String? = null,
    var licenseMonthlyFee: Long? = null,
    var licenseRemainingMonths: Int? = null,
    var productionFocus: String? = "REGULER"
)

data class HealthcareUnit(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val type: String, // "HOSPITAL", "INSURANCE", "CLINIC"
    var level: Int = 1,
    var members: Long = 0,
    var monthlyRevenue: Double = 0.0,
    val isUpgrading: Boolean = false,
    val upgradeDelayMonths: Int = 0,
    val companyCash: Double = 0.0,
    val tierCategory: String = "BASIC",
    val unitCash: Double = 0.0
)

data class ConstructionProject(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val totalContractValue: Double,
    val durationMonths: Int,
    var remainingMonths: Int,
    val isFinished: Boolean = false
)

data class EventProject(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val category: String,
    val pax: Int,
    val totalBudget: Double,
    val eoFee: Double,
    val techFee: Double,
    val useInHouseTech: Boolean,
    val executionEndTime: Long = 0L
)

enum class HotelTier(val title: String, val baseBuildCost: Long, val buildMonths: Int, val maxRooms: Int, val baseRoomRate: Long) {
    CAPSULE_HOTEL("Capsule Hotel", 2_000_000, 2, 100, 20),
    TRANSIT_MOTEL("Transit Motel", 5_000_000, 2, 50, 40),
    BUDGET_INN("Budget Inn", 8_000_000, 3, 80, 50),
    GUESTHOUSE("Boutique Guesthouse", 12_000_000, 3, 30, 80),
    CITY_HOTEL_2STAR("City Hotel 2-Star", 18_000_000, 4, 100, 70),
    CITY_HOTEL_3STAR("City Hotel 3-Star", 30_000_000, 5, 150, 100),
    BUSINESS_HOTEL_3STAR("Business Hotel 3-Star", 45_000_000, 6, 200, 120),
    SUBURBAN_HOTEL_4STAR("Suburban Hotel 4-Star", 60_000_000, 7, 250, 150),
    BUSINESS_HOTEL_4STAR("Business Hotel 4-Star", 85_000_000, 8, 300, 200),
    AIRPORT_HOTEL_4STAR("Premium Airport Hotel", 100_000_000, 9, 350, 220),
    BOUTIQUE_HOTEL_4STAR("Luxury Boutique Hotel", 120_000_000, 10, 80, 350),
    RESORT_4STAR("Beachfront Resort 4-Star", 150_000_000, 12, 400, 280),
    CITY_HOTEL_5STAR("Grand City Hotel 5-Star", 250_000_000, 14, 500, 450),
    LUXURY_RESORT_5STAR("Luxury Resort 5-Star", 350_000_000, 16, 600, 600),
    ECO_LODGE_5STAR("Premium Eco Lodge", 400_000_000, 15, 150, 800),
    HERITAGE_HOTEL_5STAR("Heritage Palace Hotel", 500_000_000, 18, 200, 1000),
    SKYSCRAPER_HOTEL_5STAR("Skyscraper Luxury Hotel", 750_000_000, 20, 800, 750),
    CASINO_RESORT_5STAR("Mega Casino Resort", 1_200_000_000, 24, 1500, 500),
    PRIVATE_VILLA_ESTATE("Exclusive Villa Estate", 1_500_000_000, 20, 50, 2500),
    ULTRA_LUXURY_6STAR("6-Star Ultra Luxury", 2_000_000_000, 26, 300, 3500),
    PRIVATE_ISLAND_7STAR("7-Star Private Island", 3_500_000_000, 36, 100, 10000),
    UNDERWATER_HOTEL("Underwater Luxury Suites", 5_000_000_000, 48, 50, 25000)
}

enum class HotelFacility(val title: String, val buildCost: Long, val maintenanceCost: Long, val bonusOccupancy: Double, val bonusRevenue: Long) {
    BASIC_POOL("Standard Swimming Pool", 1_000_000, 10_000, 0.02, 0),
    FITNESS_CENTER("24/7 Fitness Center", 2_000_000, 20_000, 0.03, 50_000),
    CAFE_BAKERY("Artisan Cafe & Bakery", 3_000_000, 30_000, 0.02, 100_000),
    KIDS_CLUB("Premium Kids Club", 5_000_000, 50_000, 0.04, 100_000),
    ROOFTOP_BAR("Rooftop Sky Bar", 8_000_000, 80_000, 0.05, 250_000),
    COWORKING_SPACE("Executive Co-working", 8_000_000, 60_000, 0.04, 150_000),
    INFINITY_POOL("Infinity Pool", 10_000_000, 100_000, 0.05, 50_000),
    HELIPAD("VIP Helipad", 10_000_000, 100_000, 0.02, 200_000),
    SPA_WELLNESS("Luxury Spa & Wellness", 15_000_000, 150_000, 0.06, 400_000),
    FINE_DINING("Michelin Star Resto", 25_000_000, 250_000, 0.08, 800_000),
    BALLROOM("Grand Ballroom", 30_000_000, 300_000, 0.08, 1_000_000),
    NIGHTCLUB("Underground VIP Nightclub", 40_000_000, 400_000, 0.06, 1_500_000),
    PRIVATE_BEACH("Exclusive Private Beach", 50_000_000, 400_000, 0.10, 500_000),
    MEDICAL_CLINIC("Premium Aesthetic Clinic", 60_000_000, 500_000, 0.05, 2_000_000),
    CONVENTION_CENTER("Mega Convention Hall", 80_000_000, 800_000, 0.12, 3_000_000),
    AQUARIUM("Giant Indoor Aquarium", 100_000_000, 1_200_000, 0.08, 2_500_000),
    YACHT_MARINA("Private Yacht Marina", 120_000_000, 1_000_000, 0.10, 3_000_000),
    GOLF_COURSE("18-Hole Golf Course", 150_000_000, 1_500_000, 0.15, 4_000_000),
    SHOPPING_ARCADE("Luxury Shopping Arcade", 200_000_000, 1_500_000, 0.12, 6_000_000),
    SKI_SLOPE("Indoor Ski Slope", 250_000_000, 2_500_000, 0.15, 8_000_000),
    CASINO("Luxury Casino", 300_000_000, 3_000_000, 0.20, 15_000_000),
    THEME_PARK_ACCESS("Integrated Theme Park", 400_000_000, 4_000_000, 0.18, 12_000_000)
}

data class HotelProperty(
    val id: String = java.util.UUID.randomUUID().toString(),
    var name: String,
    val location: String, // ex: "Bali", "Las Vegas", "Maldives"
    val tier: HotelTier,
    var isConstructing: Boolean = true,
    var remainingBuildMonths: Int,
    var customRoomRate: Long, // MEKANIK GILA: Pemain menentukan harga kamar sendiri
    var builtFacilities: MutableList<HotelFacility> = mutableListOf(),
    
    // Tracking performa bulanan
    var lastMonthOccupancyRate: Double = 0.0, // 0.0 to 1.0
    var lastMonthRevenue: Long = 0L,
    var lastMonthExpense: Long = 0L,
    var activeMegaEvent: String? = null, // Event raksasa acak (G20, Formula 1)
    
    var imageUrl: String? = null,
    var targetRoomStrategy: String? = "STANDARD", // Canceled/Deprecated, replace with roomConfigs but kept for backwards compatibility
    var roomConfigs: MutableMap<String, RoomClassConfig> = mutableMapOf()
)

data class RoomClassConfig(
    var isEnabled: Boolean = false,
    var allocationPercent: Double = 0.0,
    var customPrice: Long = 0L,
    var lastMonthOccupancy: Double = 0.0,
    var lastMonthRevenue: Long = 0L
)

enum class RoomClassStrategy(val title: String, val priceMultiplier: Double, val requiredPrestige: Int) {
    STANDARD("Standard Room", 1.0, 0),
    SUPERIOR("Superior Room", 1.5, 10),
    DELUXE("Deluxe Room", 2.0, 30),
    JUNIOR_SUITE("Junior Suite", 3.5, 50),
    SUITE("Suite Room", 6.0, 80),
    PRESIDENTIAL("Presidential / Penthouse", 12.0, 150)
}

data class OwnedBusiness(
    val instanceId: String = java.util.UUID.randomUUID().toString(),
    val catalogId: String = "",
    val customName: String? = null,
    val level: Int = 1,
    val purchasedUpgrades: Set<String> = emptySet(),
    val upgradeLevels: Map<String, Int> = emptyMap(),
    val activeUpgrades: List<ActiveUpgrade> = emptyList(),
    val customRevenue: Long? = null,
    val projectHistory: List<MovieProject> = emptyList(),
    val extraValuation: Long = 0L,
    val synergyMultiplier: Double = 1.0,
    val studioType: String? = "LIVE_ACTION",
    val companyCash: Double = 0.0,
    val acquiredStockTicker: String? = null,
    val parentId: String? = null,
    val subsidiaries: List<OwnedBusiness> = emptyList(),
    val restaurantBranches: Int = 1,
    val michelinStars: Int = 0,
    val activeTenders: List<ConstructionProject> = emptyList(),
    val availableClientProjects: List<ConstructionProject> = emptyList(),
    val healthcareSubsidiaries: List<HealthcareUnit> = emptyList(),
    val isUpgrading: Boolean = false,
    val upgradeDelayMonths: Int = 0,
    val pendingAction: String? = null,
    val airlineFleet: List<Aircraft> = emptyList(),
    val airlineHubs: List<String> = emptyList(),
    val airlineFleetComplex: List<AircraftInstance> = emptyList(),
    val airlineHubsComplex: List<AviationHub> = emptyList(),
    val flightRoutes: List<FlightRoute> = emptyList(),
    val marketDemand: Int = 0,
    val upgradeEndTimeRealTime: Long = 0L,
    val isUpgradingRealTime: Boolean = false,
    val hasRentalDivision: Boolean = false,
    val activeEvents: List<EventProject> = emptyList(),
    val clientEventRequests: List<EventProject> = emptyList(),
    val themeParkBranches: List<ThemeParkBranch> = emptyList(),
    val activeThemeParkBiddings: List<ActiveBidding> = emptyList(),
    val hospitalityProperties: List<HotelProperty> = emptyList(),
    val cruiseShips: List<CruiseShip> = emptyList(),
    val cruisePortsUnlocked: List<String> = emptyList(),
    val cruiseBrandPrestige: Int = 50,
    val contentCreatorProgress: Float = 0f,
    val contentCreatorSubscribers: Long = 100L,
    val contentCreatorEmployees: Int = 0,
    val contentCreatorOfficeUnlocked: Boolean = false,
    val contentCreatorCash: Long = 5000L
)

enum class BiddingPhase {
    WAITING_INITIAL,
    OWNER_COUNTERED,
    WAITING_REPLY,
    DEAL_REACHED,
    REJECTED
}

data class ActiveBidding(
    val id: String = java.util.UUID.randomUUID().toString(),
    val landType: ThemeParkLandType,
    var phase: BiddingPhase = BiddingPhase.WAITING_INITIAL,
    var monthsLeft: Int = landType.biddingMonths,
    var currentAskingPrice: Long = landType.basePrice,
    var playerOffer: Long = landType.basePrice
)

enum class ThemeParkLandType(val locationName: String, val maxSlots: Int, val basePrice: Long, val biddingMonths: Int) {
    // Kelas Lokal/Kecil (20-30 Slot)
    BOGOR("Bogor, ID", 20, 150_000_000, 4),
    BANDUNG("Bandung, ID", 25, 200_000_000, 5),
    BATAM("Batam, ID", 30, 250_000_000, 5),
    PEKANBARU("Pekanbaru, ID", 20, 160_000_000, 4),
    
    // Kelas Menengah (30-50 Slot)
    JAKARTA("Jakarta, ID", 35, 500_000_000, 8),
    BALI("Bali, ID", 40, 750_000_000, 10),
    MANILA("Manila, PH", 35, 450_000_000, 8),
    SEOUL("Seoul, KR", 50, 900_000_000, 10),
    
    // Kelas Berat/Besar (50-70 Slot)
    TEXAS("Texas, USA", 60, 2_000_000_000, 14),
    OSAKA("Osaka, JP", 65, 2_500_000_000, 14),
    SHANGHAI("Shanghai, CN", 70, 3_000_000_000, 16),
    
    // Kelas Raksasa/Sultan (70-100 Slot)
    FLORIDA("Orlando, USA", 80, 5_000_000_000, 18),
    CALIFORNIA("Anaheim, USA", 90, 7_500_000_000, 24),
    PARIS("Paris, FR", 85, 6_000_000_000, 24),
    TOKYO("Tokyo, JP", 100, 10_000_000_000, 30)
}

enum class RideTier(val level: Int, val cost: Long, val buildMonths: Int, val description: String) {
    TIER_1(1, 1_500_000, 2, "Kios / Atraksi Minor"),
    TIER_2(2, 5_500_000, 3, "Atraksi Keluarga Dasar"),
    TIER_3(3, 10_000_000, 4, "Wahana Kelas Menengah"),
    TIER_4(4, 25_000_000, 6, "Roller Coaster Mini / Dark Ride"),
    TIER_5(5, 50_000_000, 8, "Atraksi Tema Standar"),
    TIER_6(6, 80_000_000, 10, "Wahana Ekstrem Besar"),
    TIER_7(7, 100_000_000, 12, "Atraksi Ikonik Taman"),
    TIER_8(8, 160_000_000, 15, "Zona Tema Eksklusif"),
    TIER_9(9, 200_000_000, 18, "Hyper Coaster / 4D Ride"),
    TIER_10(10, 300_000_000, 24, "Mega Wahana Revolusioner")
}

data class ThemeParkRide(
    val id: String = java.util.UUID.randomUUID().toString(),
    var name: String,
    val ipFilmId: String? = null,
    var isConstructing: Boolean = true,
    var constructionMonthsLeft: Int,
    val tierDescription: String = "Wahana Kustom",
    var imageUrl: String? = null,
    val cost: Long = 0L,
    var zoneName: String? = null,
    var ipThemeTitle: String? = null,
    var ipThemeScore: Int? = null,
    var isPaused: Boolean = false,
    var isUnderMaintenance: Boolean = false,
    var maintenanceMonthsLeft: Int = 0
) {
    val estimatedIncome: Long get() = (cost * 0.15).toLong()
    val maintenanceCost: Long get() = (cost * 0.05).toLong()
    val baseMonthlyVisitors: Int get() = (cost / 50_000).toInt() + 100
}

data class ThemeParkBranch(
    val id: String = java.util.UUID.randomUUID().toString(),
    val locationName: String,
    var customName: String? = null,
    val landType: ThemeParkLandType,
    var remainingBiddingMonths: Int = 0,
    var rides: MutableList<ThemeParkRide> = mutableListOf(),
    var imageUrl: String? = null,
    var isLaunched: Boolean = false,
    var hasHypeMarketing: Boolean = false,
    var hypeMonthsLeft: Int = 0,
    var lastMonthVisitors: Int = 0,
    var activeDisaster: String? = null,
    var priceRegular: Long = 15L,
    var priceTerusan: Long = 35L,
    var priceVIP: Long = 100L,
    var priceFamily: Long = 80L,
    var lastMonthProfit: Long = 0L,
    var lastMonthRevenue: Long = 0L,
    var lastMonthExpense: Long = 0L,
    var parkZones: MutableList<String> = mutableListOf(),
    var activeAdName: String? = null,
    var adMonthsLeft: Int = 0,
    var adBoostMultiplier: Double = 1.0,
    var facilities: List<ThemeParkFacility> = emptyList()
) {
    val minRidesToLaunch: Int get() = when {
        landType.maxSlots <= 30 -> 5
        landType.maxSlots <= 60 -> 10
        else -> 20
    }

    val currentStatus: String get() {
        if (remainingBiddingMonths > 0) return "Riset & Akuisisi"
        if (!isLaunched) {
            if (rides.isEmpty()) return "Perencanaan Lahan"
            val finishedRidesCount = rides.count { !it.isConstructing }
            if (finishedRidesCount < minRidesToLaunch) return "Pembangunan Infrastruktur"
            return "Siap Launching"
        }
        if (hypeMonthsLeft > 0) return "Hype Season!"
        return "Beroperasi"
    }
}

data class ThemeParkFacility(
    val id: String = java.util.UUID.randomUUID().toString(),
    val catalogId: String,
    val name: String,
    val buildCost: Long,
    val maintenanceCost: Long,
    val fnbBoostPercent: Double, // Persentase kenaikan jajan pengunjung
    val appealBoost: Double,     // Tambahan poin daya tarik taman
    val zoneName: String = "Public Area",
    val imageUrl: String? = null // Link gambar kustom
)
