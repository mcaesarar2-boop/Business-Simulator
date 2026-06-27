package com.example

import com.example.viewmodel.GameViewModel

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.SplashScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
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
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute != "splash"

    Scaffold(
        bottomBar = { 
            if (showBottomBar) {
                BottomNavigationApp(navController = navController) 
            }
        }
    ) { innerPadding ->
        NavigationGraph(
            navController = navController, 
            viewModel = viewModel, 
            modifier = Modifier.padding(if (showBottomBar) innerPadding else PaddingValues(0.dp))
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
                label = { androidx.compose.material3.Text(text = item.title, maxLines = 1, softWrap = false, fontSize = 11.sp, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) },
                selected = currentRoute == item.screen_route,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFFFFD700),
                    selectedTextColor = Color(0xFFFFD700),
                    indicatorColor = Color.White.copy(alpha = 0.05f),
                    unselectedIconColor = Color.White.copy(alpha = 0.4f),
                    unselectedTextColor = Color.White.copy(alpha = 0.4f)
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
    NavHost(navController, startDestination = "splash", modifier = modifier) {
        composable("splash") { SplashScreen(navController) }
        composable(BottomNavItem.Investing.screen_route) { InvestingScreen(navController, viewModel) }
        composable(
            route = "global_stock_market?ticker={ticker}",
            arguments = listOf(navArgument("ticker") { 
                nullable = true
                defaultValue = null
                type = NavType.StringType 
            })
        ) { backStackEntry ->
            val ticker = backStackEntry.arguments?.getString("ticker")
            GlobalStockMarketScreen(navController, viewModel, initialTicker = ticker)
        }
        composable(
            route = "private_stock_market?ticker={ticker}",
            arguments = listOf(navArgument("ticker") { 
                nullable = true
                defaultValue = null
                type = NavType.StringType 
            })
        ) { backStackEntry ->
            val ticker = backStackEntry.arguments?.getString("ticker")
            com.example.ui.PrivateStockMarketScreen(navController, viewModel, initialTicker = ticker)
        }
        composable("my_portfolio_detail") { com.example.ui.MyPortfolioScreen(navController, viewModel) }
        composable("my_private_portfolio") { com.example.ui.PrivatePortfolioScreen(navController, viewModel) }
        composable("bank_savings") { com.example.ui.BankScreen(navController, viewModel) }
        composable(
            route = "ip_history/{instanceId}",
            arguments = listOf(navArgument("instanceId") { type = NavType.StringType })
        ) { backStackEntry ->
            val instanceId = backStackEntry.arguments?.getString("instanceId") ?: return@composable
            IPLibraryHistoryScreen(navController, viewModel, instanceId)
        }
        composable("tv_ip_library") { TvIpLibraryScreen(navController, viewModel) }
        composable(BottomNavItem.Business.screen_route) { BusinessDashboardScreen(navController, viewModel) }
        composable(
            route = "business_catalog?holdingId={holdingId}&targetParentId={targetParentId}",
            arguments = listOf(
                navArgument("holdingId") {
                    nullable = true
                    defaultValue = null
                    type = NavType.StringType
                },
                navArgument("targetParentId") {
                    nullable = true
                    defaultValue = null
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val holdingId = backStackEntry.arguments?.getString("holdingId")
            val targetParentId = backStackEntry.arguments?.getString("targetParentId")
            BusinessCatalogScreen(navController, viewModel, holdingId, targetParentId)
        }
        composable("business_detail/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            BusinessDetailScreen(navController, viewModel, id)
        }
        composable("theme_park_dashboard/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            com.example.ui.ThemeParkDashboardScreen(navController, viewModel, id)
        }
        composable("theme_park_branch_detail/{instanceId}/{branchId}") { backStackEntry ->
            val instanceId = backStackEntry.arguments?.getString("instanceId") ?: ""
            val branchId = backStackEntry.arguments?.getString("branchId") ?: ""
            com.example.ui.ThemeParkDetailScreen(navController, viewModel, instanceId, branchId)
        }
        composable("theme_park_marketing/{instanceId}/{branchId}") { backStackEntry ->
            val instanceId = backStackEntry.arguments?.getString("instanceId") ?: ""
            val branchId = backStackEntry.arguments?.getString("branchId") ?: ""
            com.example.ui.MarketingAgencyScreen(navController, viewModel, instanceId, branchId)
        }
        composable("theme_park_facilities/{instanceId}/{branchId}") { backStackEntry ->
            val instanceId = backStackEntry.arguments?.getString("instanceId") ?: ""
            val branchId = backStackEntry.arguments?.getString("branchId") ?: ""
            com.example.ui.FacilityCatalogScreen(navController, viewModel, instanceId, branchId)
        }
        composable("aviation_dashboard/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            com.example.ui.AviationDashboardScreen(navController, viewModel, id)
        }
        composable("cruise_dashboard/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            com.example.ui.CruiseDashboardUI(navController, viewModel, id)
        }
        composable("cruise_shipyard/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            com.example.ui.ShipyardUI(navController, viewModel, id)
        }
        composable("cruise_route_manager/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            com.example.ui.RouteManagerUI(navController, viewModel, id)
        }
        composable("aviation_catalog_screen/{businessId}") { backStackEntry ->
            val businessId = backStackEntry.arguments?.getString("businessId") ?: ""
            com.example.ui.AviationCatalogScreen(navController, viewModel, businessId)
        }
        composable("aviation_hub_catalog/{businessId}") { backStackEntry ->
            val businessId = backStackEntry.arguments?.getString("businessId") ?: ""
            com.example.ui.HubCatalogScreen(navController, viewModel, businessId)
        }
        composable("hospitality_dashboard/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            com.example.ui.HospitalityDashboardScreen(navController, viewModel, id)
        }
        composable("build_hotel_property/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            com.example.ui.BuildHotelPropertyScreen(navController, viewModel, id)
        }
        composable("hotel_detail/{businessId}/{hotelId}") { backStackEntry ->
            val businessId = backStackEntry.arguments?.getString("businessId") ?: ""
            val hotelId = backStackEntry.arguments?.getString("hotelId") ?: ""
            com.example.ui.HotelDetailScreen(navController, viewModel, businessId, hotelId)
        }
        composable("room_management/{businessId}/{hotelId}") { backStackEntry ->
            val businessId = backStackEntry.arguments?.getString("businessId") ?: ""
            val hotelId = backStackEntry.arguments?.getString("hotelId") ?: ""
            com.example.ui.RoomManagementScreen(navController, viewModel, businessId, hotelId)
        }
        composable("holding_dashboard/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            com.example.ui.HoldingDashboardScreen(navController, viewModel, id)
        }
        composable("mega_holding_detail") { 
            com.example.ui.MegaHoldingDetailScreen(navController, viewModel) 
        }
        composable("content_creator_screen") {
            com.example.ui.ContentCreatorScreen(
                externalCash = viewModel.playerState.collectAsState().value.cash,
                onAddCash = { viewModel.addCash(it) },
                onDeductCash = { viewModel.deductCash(it) },
                onSyncData = { level, income -> viewModel.syncContentCreator(level, income) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(BottomNavItem.Earnings.screen_route) { EarningsScreen(viewModel) }
        composable(BottomNavItem.Items.screen_route) { com.example.ui.CollectionScreen(navController, viewModel) }
        composable("garage") { com.example.ui.GarageScreen(navController, viewModel) }
        composable("housing") { com.example.ui.HousingScreen(navController, viewModel) }
        composable("tax_legal") { com.example.ui.TaxLegalScreen(navController, viewModel) }
        composable("global_tycoon_index") { com.example.ui.GlobalTycoonIndexScreen(navController, viewModel) }
        composable(BottomNavItem.Profile.screen_route) { ProfileScreen(navController, viewModel) }
        composable("family_office") { com.example.ui.FamilyOfficeScreen(navController, viewModel) }
        composable("private_ledger") { com.example.ui.PrivateLedgerScreen(navController, viewModel) }
        composable("tax_and_audit") { com.example.ui.TaxAndAuditScreen(navController, viewModel) }
        composable("private_lifestyle") { com.example.ui.lifestyle.LifestyleDashboardScreen(navController, viewModel) }
        composable("private_travel_concierge") { com.example.ui.lifestyle.TravelConciergeScreen(navController, viewModel) }
    }
}
