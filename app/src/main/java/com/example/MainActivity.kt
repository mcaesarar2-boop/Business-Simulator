package com.example

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

data class PlayerState(
    val cash: Long = 5000,
    val netWorth: Long = 5000,
    val inGameMonth: Int = 1,
    val inGameYear: Int = 1,
    val lastMonthIncome: Long = 0,
    val lastMonthExpenses: Long = 0,
    val lastMonthNetProfit: Long = 0,
    val monthProgress: Float = 0f,
    val ownedBusinesses: List<OwnedBusiness> = emptyList()
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

    private val _billionaires = MutableStateFlow(
        listOf(
            Billionaire(1, "Elon Musk", 250_000_000_000),
            Billionaire(2, "Bernard Arnault", 200_000_000_000),
            Billionaire(3, "Jeff Bezos", 170_000_000_000)
        )
    )
    val billionaires: StateFlow<List<Billionaire>> = _billionaires.asStateFlow()

    init { startGameLoop() }

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
        val step = 100f / 120_000f // 100ms dari 120 detik (1 bulan)
        val newProgress = currentState.monthProgress + step

        if (newProgress >= 1f) {
            processMonthlyTick()
        } else {
            _playerState.value = currentState.copy(monthProgress = newProgress)
        }
    }

    private fun processMonthlyTick() {
        val currentState = _playerState.value
        var monthlyIncome = 0L
        var monthlyExpenses = 0L

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

        val netProfit = monthlyIncome - monthlyExpenses
        val newCash = currentState.cash + netProfit
        
        var newMonth = currentState.inGameMonth + 1
        var newYear = currentState.inGameYear
        if (newMonth > 12) {
            newMonth = 1
            newYear += 1
        }

        _playerState.value = currentState.copy(
            cash = newCash,
            netWorth = newCash,
            inGameMonth = newMonth,
            inGameYear = newYear,
            lastMonthIncome = monthlyIncome,
            lastMonthExpenses = monthlyExpenses,
            lastMonthNetProfit = netProfit,
            monthProgress = 0f
        )
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
        composable(BottomNavItem.Investing.screen_route) { InvestingScreen(navController) }
        composable("global_stock_market") { GlobalStockMarketScreen(navController) }
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
