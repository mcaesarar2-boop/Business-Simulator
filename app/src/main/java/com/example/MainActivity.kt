package com.example

import com.example.data.*

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainApp()
            }
        }
    }
}

// ==========================================
// 1. DATA MODELS & VIEWMODEL
// ==========================================
enum class BusinessCategory { CULINARY, RETAIL, PROPERTY, CREATIVE, LOGISTICS, ENTERTAINMENT, FINANCE }

data class BusinessUpgrade(
    val id: String,
    val name: String,
    val description: String,
    val baseCost: Long,
    val costMultiplier: Float = 1.0f,
    val maxLevel: Int = 1,
    val revenueFlatBoost: Long = 0,
    val revenueMultiplier: Float = 1.0f,
    val maintenanceFlatReduction: Long = 0,
    val maintenanceMultiplier: Float = 1.0f
)

data class BusinessCatalogItem(
    val id: String,
    val name: String,
    val category: BusinessCategory,
    val costToBuy: Long,
    val monthlyRevenue: Long,
    val monthlyMaintenanceCost: Long,
    val isFluctuating: Boolean = false,
    val upgrades: List<BusinessUpgrade> = emptyList()
)

data class OwnedBusiness(
    val catalogId: String,
    val level: Int,
    val purchasedUpgrades: Set<String> = emptySet(),
    val upgradeLevels: Map<String, Int> = emptyMap()
)

data class OwnedStock(val ticker: String, val averagePrice: Double, val shares: Int)

data class PlayerState(
    val cash: Long = 5000,
    val netWorth: Long = 5000,
    val inGameMonth: Int = 1,
    val inGameYear: Int = 1,
    val lastMonthIncome: Long = 0,
    val lastMonthExpenses: Long = 0,
    val lastMonthNetProfit: Long = 0,
    val ownedBusinesses: List<OwnedBusiness> = emptyList(),
    val ownedStocks: List<OwnedStock> = emptyList(),
    val ownedProperties: List<com.example.data.OwnedProperty> = emptyList(),
    val ownedCrypto: List<com.example.data.OwnedCrypto> = emptyList()
)

fun getBusinessStats(owned: OwnedBusiness, catalog: BusinessCatalogItem): Pair<Long, Long> {
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

    val totalRev = ((catalog.monthlyRevenue + flatRev) * multRev).toLong()
    val totalMaint = ((catalog.monthlyMaintenanceCost - flatMaint) * multMaint).toLong().coerceAtLeast(0)
    
    return Pair(totalRev, totalMaint)
}

fun getUpgradeCost(upgrade: BusinessUpgrade, currentLevel: Int): Long {
    var costMultiplierTotal = 1.0f
    repeat(currentLevel) { costMultiplierTotal *= upgrade.costMultiplier }
    return (upgrade.baseCost * costMultiplierTotal).toLong()
}

val businessCatalog = listOf(
    BusinessCatalogItem(
        id = "culinary", name = "Street Food Cart", category = BusinessCategory.CULINARY,
        costToBuy = 500, monthlyRevenue = 600, monthlyMaintenanceCost = 100,
        upgrades = listOf(
            BusinessUpgrade("culinary_u1", "Menu Baru", "Tambah menu makanan (+300 Rev)", baseCost = 300, revenueFlatBoost = 300),
            BusinessUpgrade("culinary_u2", "Buka Cabang", "Omzet 2x lipat, namun biaya operasional naik", baseCost = 1500, revenueMultiplier = 2.0f, maintenanceMultiplier = 1.5f)
        )
    ),
    BusinessCatalogItem(
        id = "retail", name = "Convenience Store", category = BusinessCategory.RETAIL,
        costToBuy = 1500, monthlyRevenue = 1200, monthlyMaintenanceCost = 300,
        upgrades = listOf(
            BusinessUpgrade("retail_u1", "Perluas Ruangan", "Lebih banyak produk terjual (1.5x Rev)", baseCost = 800, revenueMultiplier = 1.5f),
            BusinessUpgrade("retail_u2", "Optimasi Supplier", "Diskon supplier, biaya perawatan turun", baseCost = 1200, maintenanceMultiplier = 0.5f)
        )
    ),
    BusinessCatalogItem(
        id = "property", name = "Small Apartment", category = BusinessCategory.PROPERTY,
        costToBuy = 5000, monthlyRevenue = 2500, monthlyMaintenanceCost = 200,
        upgrades = listOf(
            BusinessUpgrade("prop_u1", "Renovasi Mewah", "Tingkatkan harga sewa (+1500 Rev)", baseCost = 3000, revenueFlatBoost = 1500),
            BusinessUpgrade("prop_u2", "Manajer Properti", "Lebih hemat urus kerusakan", baseCost = 1000, maintenanceFlatReduction = 100)
        )
    ),
    BusinessCatalogItem(
        id = "creative", name = "Garage Startup", category = BusinessCategory.CREATIVE,
        costToBuy = 2000, monthlyRevenue = 500, monthlyMaintenanceCost = 100, isFluctuating = true,
        upgrades = listOf(
            BusinessUpgrade("creative_u1", "Rekrut Senior Dev", "Aplikasi lebih stabil (+800 Rev)", baseCost = 1500, revenueFlatBoost = 800),
            BusinessUpgrade("creative_u2", "Marketing Viral", "Lonjakan pengguna yang masif (3x Rev)", baseCost = 2500, revenueMultiplier = 3.0f)
        )
    ),
    BusinessCatalogItem(
        id = "logistics", name = "Bicycle Courier", category = BusinessCategory.LOGISTICS,
        costToBuy = 1800, monthlyRevenue = 1800, monthlyMaintenanceCost = 900,
        upgrades = listOf(
            BusinessUpgrade("log_u1", "Kredit Mobil Box", "Kapasitas kirim super besar (+1000 Rev)", baseCost = 2000, revenueFlatBoost = 1000),
            BusinessUpgrade("log_u2", "AI Pembuat Rute", "Hemat bensin luar biasa (-60% Biaya)", baseCost = 1500, maintenanceMultiplier = 0.4f)
        )
    ),
    BusinessCatalogItem(
        id = "entertainment", name = "Dive Bar", category = BusinessCategory.ENTERTAINMENT,
        costToBuy = 3500, monthlyRevenue = 3000, monthlyMaintenanceCost = 1500,
        upgrades = listOf(
            BusinessUpgrade("ent_u1", "Sewa DJ Terkenal", "Keramaian membludak (+2000 Rev)", baseCost = 2000, revenueFlatBoost = 2000),
            BusinessUpgrade("ent_u2", "Program VIP", "Tamu sultan rela bayar mahal (1.5x Rev)", baseCost = 3000, revenueMultiplier = 1.5f)
        )
    ),
    BusinessCatalogItem(
        id = "finance", name = "Local City Bank", category = BusinessCategory.FINANCE,
        costToBuy = 50000, monthlyRevenue = 15000, monthlyMaintenanceCost = 8000,
        upgrades = listOf(
            BusinessUpgrade("fin_u1", "Mobile Banking App", "Pasif income meningkat tiap level", baseCost = 10000, costMultiplier = 1.25f, maxLevel = 50, revenueFlatBoost = 2000),
            BusinessUpgrade("fin_u2", "Aggressive Loan Marketing", "Boost revenue besar, operasional juga naik tajam", baseCost = 25000, costMultiplier = 1.4f, maxLevel = 10, revenueMultiplier = 1.2f, maintenanceMultiplier = 1.15f),
            BusinessUpgrade("fin_u3", "Asset Mgt (AUM)", "Dapatkan keuntungan dari aset", baseCost = 50000, costMultiplier = 1.5f, maxLevel = 20, revenueFlatBoost = 8000),
            BusinessUpgrade("fin_u4", "Merger & Acquisition", "Alokasi dana akuisisi kompetitor (Super mahal)", baseCost = 250000, costMultiplier = 2.0f, maxLevel = 5, revenueMultiplier = 1.5f)
        )
    )
)

data class Billionaire(val id: Int, val name: String, val netWorth: Long, val rank: Int = 0)

class GameViewModel : ViewModel() {
    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private val _monthProgress = kotlinx.coroutines.flow.MutableStateFlow(0f)
    val monthProgress: kotlinx.coroutines.flow.StateFlow<Float> = _monthProgress.asStateFlow()

    private val _realEstateMarket = MutableStateFlow(com.example.data.initialRealEstateCatalog)
    val realEstateMarket: StateFlow<List<com.example.data.PropertyItem>> = _realEstateMarket.asStateFlow()

    private val _cryptoList = MutableStateFlow(com.example.data.initialCryptoList)
    val cryptoList: StateFlow<List<com.example.data.CryptoItem>> = _cryptoList.asStateFlow()


    private val _billionaires = MutableStateFlow(
        listOf(
            Billionaire(1, "Elon Musk", 250_000_000_000),
            Billionaire(2, "Bernard Arnault", 200_000_000_000),
            Billionaire(3, "Jeff Bezos", 170_000_000_000)
        )
    )
    val billionaires: StateFlow<List<Billionaire>> = _billionaires.asStateFlow()

    // --- Advanced General & Mini Game Experiment Settings ---
    private val _monthDurationSeconds = MutableStateFlow(120f) // default 120 seconds for game-month
    val monthDurationSeconds: StateFlow<Float> = _monthDurationSeconds.asStateFlow()

    private val _stockIntervalSeconds = MutableStateFlow(30.0f) // default stock fluctuation interval
    val stockIntervalSeconds: StateFlow<Float> = _stockIntervalSeconds.asStateFlow()

    // Default SVG Path (Crown configuration)
    private val _companyLogoSvgPath = MutableStateFlow("M 10 90 L 10 30 L 35 60 L 50 20 L 65 60 L 90 30 L 90 90 Z")
    val companyLogoSvgPath: StateFlow<String> = _companyLogoSvgPath.asStateFlow()

    private val _companyLogoFillColorHex = MutableStateFlow("#FFD700") // gold
    val companyLogoFillColorHex: StateFlow<String> = _companyLogoFillColorHex.asStateFlow()

    private val _isNotificationEnabled = MutableStateFlow(true)
    val isNotificationEnabled: StateFlow<Boolean> = _isNotificationEnabled.asStateFlow()

    private val _isDarkModeSimulated = MutableStateFlow(true)
    val isDarkModeSimulated: StateFlow<Boolean> = _isDarkModeSimulated.asStateFlow()

    private val _soundVolume = MutableStateFlow(0.8f)
    val soundVolume: StateFlow<Float> = _soundVolume.asStateFlow()

    private val _gameDifficulty = MutableStateFlow("Normal") // "Easy", "Normal", "Hard", "Elite Tycoon"
    val gameDifficulty: StateFlow<String> = _gameDifficulty.asStateFlow()

    private val _marketVolatilityFactor = MutableStateFlow(1.0f) // Volatility/Shift factor: 0.1x to 5.0x
    val marketVolatilityFactor: StateFlow<Float> = _marketVolatilityFactor.asStateFlow()

    fun updateMonthDuration(seconds: Float) {
        _monthDurationSeconds.value = seconds.coerceIn(5f, 300f)
    }

    fun updateStockInterval(seconds: Float) {
        _stockIntervalSeconds.value = seconds.coerceIn(1.0f, 60.0f)
    }

    fun updateCompanyLogo(svgPath: String, colorHex: String) {
        _companyLogoSvgPath.value = svgPath
        _companyLogoFillColorHex.value = colorHex
    }

    fun updateGeneralSettings(isNotification: Boolean, isDarkMode: Boolean, volume: Float, difficulty: String, volatility: Float) {
        _isNotificationEnabled.value = isNotification
        _isDarkModeSimulated.value = isDarkMode
        _soundVolume.value = volume.coerceIn(0f, 1f)
        _gameDifficulty.value = difficulty
        _marketVolatilityFactor.value = volatility.coerceIn(0.1f, 5.0f)
    }

    fun buyCrypto(symbol: String, price: Double, amount: Double) {
        val requiredCashUsd = price * amount
        val currentCash = _playerState.value.cash

        if (currentCash >= requiredCashUsd) {
            val newCash = currentCash - requiredCashUsd.toLong()
            val ownedList = _playerState.value.ownedCrypto.toMutableList()
            
            val existing = ownedList.find { it.symbol == symbol }
            if (existing != null) {
                val totalAmount = existing.amount + amount
                val newAveragePrice = ((existing.amount * existing.averagePrice) + (amount * price)) / totalAmount
                ownedList.remove(existing)
                ownedList.add(com.example.data.OwnedCrypto(symbol, newAveragePrice, totalAmount))
            } else {
                ownedList.add(com.example.data.OwnedCrypto(symbol, price, amount))
            }
            
            _playerState.value = _playerState.value.copy(
                cash = newCash,
                ownedCrypto = ownedList
            )
        }
    }

    fun buyStock(ticker: String, price: Double, quantity: Int) {
        val stockToBuy = _stockList.value.find { it.ticker == ticker } ?: return
        val isIndo = ticker.contains(".JK")
        // USD is default cash. Indo stocks price usually in IDR. Assume 15000 IDR = 1 USD
        val requiredCashUsd = if (isIndo) (price * quantity) / 15000.0 else (price * quantity)
        val currentCash = _playerState.value.cash

        if (currentCash >= requiredCashUsd) {
            val currentState = _playerState.value
            val existingStocks = currentState.ownedStocks.toMutableList()
            val existingIndex = existingStocks.indexOfFirst { it.ticker == ticker }
            
            if (existingIndex != -1) {
                val existing = existingStocks[existingIndex]
                val newShares = existing.shares + quantity
                val newAvgPrice = ((existing.shares * existing.averagePrice) + (quantity * price)) / newShares
                existingStocks[existingIndex] = existing.copy(shares = newShares, averagePrice = newAvgPrice)
            } else {
                existingStocks.add(OwnedStock(ticker, price, quantity))
            }

            _playerState.value = currentState.copy(
                cash = currentCash - requiredCashUsd.toLong(),
                ownedStocks = existingStocks
            )
        }
    }

    fun resetGameProgress() {
        _monthProgress.value = 0f
        _playerState.value = PlayerState(
            cash = 5000,
            netWorth = 5000,
            inGameMonth = 1,
            inGameYear = 1,
            lastMonthIncome = 0,
            lastMonthExpenses = 0,
            lastMonthNetProfit = 0,
            ownedBusinesses = emptyList(),
            ownedStocks = emptyList()
        )
    }

    private val _stockList = MutableStateFlow(generateStockData())
    val stockList: StateFlow<List<StockItem>> = _stockList.asStateFlow()

    private val initialPrices = mutableMapOf<String, Double>()

    private fun startCryptoMarketLoop() {
        viewModelScope.launch {
            val initialCryptoPrices = com.example.data.initialCryptoList.associate { it.symbol to it.currentPrice }
            while (true) {
                delay((_stockIntervalSeconds.value * 1000f).toLong().coerceAtLeast(100L))
                val volatility = _marketVolatilityFactor.value * 2.5f // Crypto is more volatile
                val triggerNews = Math.random() < 0.10
                
                val updatedCrypto = _cryptoList.value.map { crypto ->
                    val baseline = initialCryptoPrices[crypto.symbol] ?: crypto.currentPrice
                    val changePct = (Math.random() - 0.5) * 0.015 * volatility
                    val newPrice = Math.max(0.000001, crypto.currentPrice * (1 + changePct))
                    val newChangeAbs = newPrice - baseline
                    val newChangePct = (newChangeAbs / baseline) * 100
                    
                    crypto.copy(
                        currentPrice = newPrice,
                        changePercentage = newChangePct
                    )
                }.toMutableList()

                if (triggerNews) {
                    val rand = Math.random()
                    val newsItem = when {
                        rand < 0.5 -> {
                            val boost = 0.03 + (Math.random() * 0.05)
                            updatedCrypto.replaceAll { c -> 
                                val baseline = initialCryptoPrices[c.symbol] ?: c.currentPrice
                                val newP = c.currentPrice * (1 + boost)
                                c.copy(currentPrice = newP, changePercentage = ((newP - baseline) / baseline) * 100)
                            }
                            MarketNews(id = "crypto_b_${System.currentTimeMillis()}", text = "CRYPTO PUMP: Institusi besar mulai adopsi masal blockchain!", type = "BULL")
                        }
                        else -> {
                            val drop = -0.03 - (Math.random() * 0.05)
                            updatedCrypto.replaceAll { c -> 
                                val baseline = initialCryptoPrices[c.symbol] ?: c.currentPrice
                                val newP = c.currentPrice * (1 + drop)
                                c.copy(currentPrice = newP, changePercentage = ((newP - baseline) / baseline) * 100)
                            }
                            MarketNews(id = "crypto_b_${System.currentTimeMillis()}", text = "CRYPTO CRASH: Regulasi ketat memukul pasar kripto!", type = "BEAR")
                        }
                    }
                    val newFeeds = listOf(newsItem) + _newsFeed.value
                    _newsFeed.value = newFeeds.take(20)
                }
                
                _cryptoList.value = updatedCrypto
            }
        }
    }
    
    init { 
        _stockList.value.forEach { initialPrices[it.ticker] = it.currentPrice }
        startGameLoop() 
        startStockMarketLoop()
        startCryptoMarketLoop()
    }

    private val _newsFeed = MutableStateFlow(listOf(
        MarketNews("0", "Sesi Pasar dibuka. Seluruh pasar global dan domestik beroperasi normal.", "NEUTRAL")
    ))
    val newsFeed: StateFlow<List<MarketNews>> = _newsFeed.asStateFlow()

    private var newsCounter = 1

    private fun startStockMarketLoop() {
        viewModelScope.launch {
            while (true) {
                delay((_stockIntervalSeconds.value * 1000f).toLong().coerceAtLeast(100L))
                val volatility = _marketVolatilityFactor.value
                val triggerNews = Math.random() < 0.12 // 12% change for a major market story
                val updatedList = _stockList.value.map { stock ->
                    val baseline = initialPrices[stock.ticker] ?: stock.currentPrice
                    // Default slight random walk fluctuation (-0.35% to +0.35%) * dynamic volatility multiplier
                    val changePct = (Math.random() - 0.5) * 0.007 * volatility
                    val newPrice = Math.max(0.01, stock.currentPrice * (1 + changePct))
                    val newChangeAbs = newPrice - baseline
                    val newChangePct = (newChangeAbs / baseline) * 100
                    
                    stock.copy(
                        currentPrice = newPrice,
                        changeAbsolute = newChangeAbs,
                        changePercentage = newChangePct
                    )
                }.toMutableList()

                if (triggerNews) {
                    val rand = Math.random()
                    val newsItem = when {
                        rand < 0.25 -> {
                            // Tech rally event
                            val boost = 0.015 + (Math.random() * 0.02) // +1.5% to +3.5%
                            updatedList.replaceAll { stock ->
                                if (stock.sector == "Technology") {
                                    val baseline = initialPrices[stock.ticker] ?: stock.currentPrice
                                    val newPrice = stock.currentPrice * (1 + boost)
                                    val newChangeAbs = newPrice - baseline
                                    val newChangePct = (newChangeAbs / baseline) * 100
                                    stock.copy(currentPrice = newPrice, changeAbsolute = newChangeAbs, changePercentage = newChangePct)
                                } else stock
                            }
                            MarketNews(
                                id = newsCounter.toString(),
                                text = "RALLY TEKNOLOGI: Kenaikan konsensus chip AI memicu gelembung bullish di seluruh emiten teknologi!",
                                type = "BULL"
                            )
                        }
                        rand < 0.50 -> {
                            // Energy commodity shock
                            val boost = -0.015 - (Math.random() * 0.02)
                            updatedList.replaceAll { stock ->
                                if (stock.sector == "Energy") {
                                    val baseline = initialPrices[stock.ticker] ?: stock.currentPrice
                                    val newPrice = stock.currentPrice * (1 + boost)
                                    val newChangeAbs = newPrice - baseline
                                    val newChangePct = (newChangeAbs / baseline) * 100
                                    stock.copy(currentPrice = newPrice, changeAbsolute = newChangeAbs, changePercentage = newChangePct)
                                } else stock
                            }
                            MarketNews(
                                id = newsCounter.toString(),
                                text = "KOREKSI ENERGI: Harga minyak mentah dunia anjlok, menyeret turun sektor energi secara global.",
                                type = "BEAR"
                            )
                        }
                        rand < 0.75 -> {
                            // Indo bluechips crash
                            val boost = -0.02 - (Math.random() * 0.01)
                            updatedList.replaceAll { stock ->
                                if (stock.ticker.contains(".JK")) {
                                    val baseline = initialPrices[stock.ticker] ?: stock.currentPrice
                                    val newPrice = stock.currentPrice * (1 + boost)
                                    val newChangeAbs = newPrice - baseline
                                    val newChangePct = (newChangeAbs / baseline) * 100
                                    stock.copy(currentPrice = newPrice, changeAbsolute = newChangeAbs, changePercentage = newChangePct)
                                } else stock
                            }
                            MarketNews(
                                id = newsCounter.toString(),
                                text = "ASING NET-SELL: Dana asing keluar masif dari IHSG, bursa Indonesia memerah tajam.",
                                type = "BEAR"
                            )
                        }
                        else -> {
                            // Crypto regulation or random
                            MarketNews(
                                id = newsCounter.toString(),
                                text = "BERITA MAKRO: Bank Sentral mengumumkan kebijakan netral. Pasar merespon sideways.",
                                type = "NEUTRAL"
                            )
                        }
                    }
                    newsCounter++
                    _newsFeed.value = (listOf(newsItem) + _newsFeed.value).take(15)
                }

                _stockList.value = updatedList
            }
        }
    }

    private fun startGameLoop() {
        viewModelScope.launch {
            while(true) {
                delay(100) // update setiap 100ms
                updateProgress()
            }
        }
    }

    private fun updateProgress() {
        val currentState = _playerState.value
        val durationMs = _monthDurationSeconds.value * 1000f
        val step = 100f / durationMs // 100ms from game loop rate
        val newProgress = _monthProgress.value + step

        if (newProgress >= 1f) {
            processMonthlyTick()
        } else {
            _monthProgress.value = newProgress
        }
    }

    private fun processMonthlyTick() {
        _monthProgress.value = 0f
        val currentState = _playerState.value
        var monthlyIncome = 0L
        var monthlyExpenses = 0L

        // Business Income
        currentState.ownedBusinesses.forEach { owned ->
            val catalogItem = businessCatalog.find { it.id == owned.catalogId }
            if (catalogItem != null) {
                val (baseRev, baseMaint) = getBusinessStats(owned, catalogItem)
                val revenue = if (catalogItem.isFluctuating) {
                    (baseRev / 2 .. baseRev * 2).random()
                } else {
                    baseRev
                }
                
                monthlyIncome += revenue
                monthlyExpenses += baseMaint
            }
        }

        // Real Estate Rental Income
        currentState.ownedProperties.forEach { owned ->
            val propItem = _realEstateMarket.value.find { it.id == owned.propertyId }
            if (propItem != null) {
                monthlyIncome += propItem.baseRentalIncome
            }
        }

        // Stock Portfolio Dividend Yield (Estimation)
        currentState.ownedStocks.forEach { owned ->
            val liveStock = _stockList.value.find { it.ticker == owned.ticker }
            if (liveStock != null) {
                val stats = com.example.data.getMarketStats(liveStock.ticker, liveStock.currentPrice)
                val monthlyYieldPercent = stats.dividendYield / 12.0 / 100.0 // Annual yield / 12 / 100
                val estMonthlyDividend = (owned.shares * liveStock.currentPrice * monthlyYieldPercent).toLong()
                monthlyIncome += estMonthlyDividend
            }
        }

        val netProfit = monthlyIncome - monthlyExpenses
        val newCash = currentState.cash + netProfit
        
        var newMonth = currentState.inGameMonth + 1
        var newYear = currentState.inGameYear
        if (newMonth > 12) {
            newMonth = 1
            newYear += 1
        }
        
        val stocksValue = currentState.ownedStocks.sumOf { owned ->
            val livePrice = _stockList.value.find { it.ticker == owned.ticker }?.currentPrice ?: owned.averagePrice
            (owned.shares * livePrice).toLong()
        }
        val cryptoValue = currentState.ownedCrypto.sumOf { owned ->
            val livePrice = _cryptoList.value.find { it.symbol == owned.symbol }?.currentPrice ?: owned.averagePrice
            (owned.amount * livePrice).toLong()
        }
        val realEstateValue = currentState.ownedProperties.sumOf { owned ->
            val prop = _realEstateMarket.value.find { it.id == owned.propertyId }
            prop?.basePrice ?: owned.purchasedPrice
        }
        val businessValue = currentState.ownedBusinesses.sumOf { it.level * 5000L }
        
        val newNetWorth = newCash + stocksValue + cryptoValue + realEstateValue + businessValue

        _playerState.value = currentState.copy(
            cash = newCash,
            netWorth = newNetWorth,
            inGameMonth = newMonth,
            inGameYear = newYear,
            lastMonthIncome = monthlyIncome,
            lastMonthExpenses = monthlyExpenses,
            lastMonthNetProfit = netProfit,
        )
    }

    
    fun addProperty(name: String, location: String, price: Long, rental: Long) {
        val newId = "prop_custom_${System.currentTimeMillis()}"
        val newProp = com.example.data.PropertyItem(newId, name, location, "Custom", price, rental)
        val currentMarket = _realEstateMarket.value.toMutableList()
        currentMarket.add(newProp)
        _realEstateMarket.value = currentMarket
    }

    fun buyProperty(propertyId: String) {
        val currentState = _playerState.value
        val property = _realEstateMarket.value.find { it.id == propertyId }
        if (property != null && currentState.cash >= property.basePrice) {
            val newCash = currentState.cash - property.basePrice
            val ownedList = currentState.ownedProperties.toMutableList()
            ownedList.add(com.example.data.OwnedProperty(propertyId, property.basePrice, property.basePrice))
            _playerState.value = currentState.copy(cash = newCash, ownedProperties = ownedList)
        }
    }

fun buyBusiness(businessId: String) {
        val currentState = _playerState.value
        val catalogItem = businessCatalog.find { it.id == businessId } ?: return

        if (currentState.ownedBusinesses.any { it.catalogId == businessId }) return

        if (currentState.cash >= catalogItem.costToBuy) {
            val newOwned = OwnedBusiness(
                catalogId = businessId,
                level = 1,
                purchasedUpgrades = emptySet()
            )
            _playerState.value = currentState.copy(
                cash = currentState.cash - catalogItem.costToBuy,
                ownedBusinesses = currentState.ownedBusinesses + newOwned
            )
        }
    }

    fun purchaseUpgrade(businessId: String, upgradeId: String) {
        val currentState = _playerState.value
        val owned = currentState.ownedBusinesses.find { it.catalogId == businessId } ?: return
        val catalogItem = businessCatalog.find { it.id == businessId } ?: return
        val upgrade = catalogItem.upgrades.find { it.id == upgradeId } ?: return

        val currentLevel = owned.upgradeLevels[upgradeId] ?: if (owned.purchasedUpgrades.contains(upgradeId)) 1 else 0
        if (currentLevel >= upgrade.maxLevel) return

        var costMultiplierTotal = 1.0f
        repeat(currentLevel) { costMultiplierTotal *= upgrade.costMultiplier }
        val cost = (upgrade.baseCost * costMultiplierTotal).toLong()

        if (currentState.cash >= cost) {
            val newUpgradeLevels = owned.upgradeLevels + (upgradeId to currentLevel + 1)
            val newPurchasedUpgrades = owned.purchasedUpgrades + upgradeId

            val newOwned = owned.copy(
                purchasedUpgrades = newPurchasedUpgrades,
                upgradeLevels = newUpgradeLevels,
                level = owned.level + 1
            )
            _playerState.value = currentState.copy(
                cash = currentState.cash - cost,
                ownedBusinesses = currentState.ownedBusinesses.map {
                    if (it.catalogId == businessId) newOwned else it
                }
            )
        }
    }
}

// ==========================================
// 2. NAVIGASI BOTTOM TABS (Gaya "Instagram")
// ==========================================
sealed class BottomNavItem(var title: String, var icon: ImageVector, var screen_route: String) {
    object Investing : BottomNavItem("Investasi", Icons.Filled.TrendingUp, "investing")
    object Business : BottomNavItem("Bisnis", Icons.Filled.BusinessCenter, "business")
    object Earnings : BottomNavItem("Pendapatan", Icons.Filled.AttachMoney, "earnings")
    object Items : BottomNavItem("Aset", Icons.Filled.ShoppingCart, "items")
    object Profile : BottomNavItem("Profil", Icons.Filled.Person, "profile")
}

@Composable
fun MainApp(viewModel: GameViewModel = viewModel()) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavigationApp(navController = navController) }
    ) { innerPadding ->
        NavigationGraph(
            navController = navController, 
            viewModel = viewModel, 
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
fun BottomNavigationApp(navController: NavHostController) {
    val items = listOf(
        BottomNavItem.Investing,
        BottomNavItem.Business,
        BottomNavItem.Earnings,
        BottomNavItem.Items,
        BottomNavItem.Profile
    )
    
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title) },
                selected = currentRoute == item.screen_route,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.background,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                onClick = {
                    navController.navigate(item.screen_route) {
                        navController.graph.startDestinationRoute?.let { screen_route ->
                            popUpTo(screen_route) { saveState = true }
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

@Composable
fun NavigationGraph(navController: NavHostController, viewModel: GameViewModel, modifier: Modifier) {
    NavHost(navController, startDestination = BottomNavItem.Investing.screen_route, modifier = modifier) {
        composable(BottomNavItem.Investing.screen_route) { InvestingScreen(navController, viewModel) }
        composable("global_stock_market") { GlobalStockMarketScreen(navController, viewModel) }
        composable(BottomNavItem.Business.screen_route) { BusinessDashboardScreen(navController, viewModel) }
        composable("business_catalog") { BusinessCatalogScreen(navController, viewModel) }
        composable("business_detail/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            BusinessDetailScreen(navController, viewModel, id)
        }
        composable(BottomNavItem.Earnings.screen_route) { EarningsScreen(viewModel) }
        composable(BottomNavItem.Items.screen_route) { ItemsScreen() }
        composable(BottomNavItem.Profile.screen_route) { ProfileScreen(viewModel) }
    }
}
