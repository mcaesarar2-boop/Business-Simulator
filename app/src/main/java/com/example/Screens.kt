package com.example

import com.example.data.*

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.compose.foundation.clickable
import java.text.NumberFormat
import java.util.Locale

import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import coil.compose.AsyncImage
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.border

// Format Uang Default
val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US).apply { maximumFractionDigits = 0 }

// ==========================================
// 1. INVESTING SCREEN (Dashboard Investasi)
// ==========================================
@Composable
fun InvestingScreen(navController: NavHostController, viewModel: GameViewModel) {
    // Theme Colors
    val bgDark = Color(0xFF121212)
    val cardDark = Color(0xFF1E1E1E)
    val slateDark = Color(0xFF252A34)
    val gold = Color(0xFFFFD700)
    val darkGold = Color(0xFFB8860B)
    val neonGreen = Color(0xFF00FF00)
    val textGray = Color(0xFFA0A0A0)
    val red = Color(0xFFFF3B30)

    val playerState by viewModel.playerState.collectAsState()
    val monthProgress by viewModel.monthProgress.collectAsState()
    val stockList by viewModel.stockList.collectAsState()

    var activeTab by remember { mutableStateOf("Stocks") }
    val tabs = listOf("Stocks", "Real Estate", "Crypto", "Startups")

    var showBuyDialog by remember { mutableStateOf(false) }
    var selectedStockToBuy by remember { mutableStateOf<StockItem?>(null) }
    var buySharesAmount by remember { mutableStateOf("") }
    var buySuccessMessage by remember { mutableStateOf<String?>(null) }

    // Calculate real stock portfolio values
    var totalCostBasis = 0.0
    var currentStocksValue = 0.0
    var estimatedYieldPerMonth = 0.0 // Simplified calculation

    playerState.ownedStocks.forEach { owned ->
        val liveStock = stockList.find { it.ticker == owned.ticker }
        val livePrice = liveStock?.currentPrice ?: owned.averagePrice
        currentStocksValue += owned.shares * livePrice
        totalCostBasis += owned.shares * owned.averagePrice
        
        // Let's estimate yield simply based on divYield or just a flat percentage for demonstration
        if (liveStock != null) {
            val stats = getMarketStats(liveStock.ticker, liveStock.currentPrice)
            estimatedYieldPerMonth += (owned.shares * livePrice) * (stats.dividendYield / 100.0 / 12.0)
        }
    }

    val profitLoss = currentStocksValue - totalCostBasis
    val profitLossPct = if (totalCostBasis > 0) (profitLoss / totalCostBasis) * 100 else 0.0
    val profitColor = if (profitLoss >= 0) neonGreen else red
    val profitSign = if (profitLoss >= 0) "+" else ""

    Scaffold(
        containerColor = bgDark
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header Content
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                Text(
                    text = "Investing",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                // Custom Tabs
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(androidx.compose.foundation.rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    tabs.forEach { title ->
                        val isSelected = activeTab == title
                        Column(
                            modifier = Modifier
                                .clickable { activeTab = title }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = title,
                                color = if (isSelected) gold else textGray,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            if (isSelected) {
                                Box(modifier = Modifier.height(2.dp).fillMaxWidth(0.5f).background(gold))
                            } else {
                                Spacer(modifier = Modifier.height(2.dp))
                            }
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                when (activeTab) {
                    "Stocks" -> {
                        item {
                            Surface(
                                color = Color(0xFF1A1E24),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(24.dp)) {
                                    Text("My stock portfolio", color = textGray, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("$ ${String.format(Locale.US, "%,.2f", currentStocksValue)}", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("$profitSign$ ${String.format(Locale.US, "%,.2f", Math.abs(profitLoss))} (${String.format(Locale.US, "%.2f", profitLossPct)}%)", color = profitColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text("Estimated yield per month: $ ${String.format(Locale.US, "%,.2f", estimatedYieldPerMonth)}", color = textGray, fontSize = 14.sp)
                                }
                            }
                        }

                        item {
                            Button(
                                onClick = { navController.navigate("global_stock_market") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = darkGold)
                            ) {
                                Text("Global Stock Market -> View all available offers", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }

                        item {
                            val stableIncome = stockList
                                .filter { it.changePercentage >= 0 }
                                .sortedBy { it.changePercentage }
                                .take(2)
                            
                            val growthPotential = stockList
                                .sortedByDescending { it.changePercentage }
                                .take(2)

                            Column {
                                Text("Stable income", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(12.dp))
                                stableIncome.forEach { stock ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(shape = CircleShape, color = cardDark, modifier = Modifier.size(40.dp)) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(Icons.Default.AccountBalance, contentDescription = null, tint = gold, modifier = Modifier.size(20.dp))
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text("${stock.ticker} - ${stock.name}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1)
                                                Text(String.format(Locale.US, "$%,.2f (%.2f%%)", stock.currentPrice, stock.changePercentage), color = textGray, fontSize = 12.sp)
                                            }
                                        }
                                        Button(
                                            onClick = { 
                                                selectedStockToBuy = stock
                                                showBuyDialog = true
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = gold, contentColor = Color.Black),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Text("Beli", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(24.dp))
                                Text("Growth potential", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(12.dp))
                                growthPotential.forEach { stock ->
                                     Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("${stock.ticker} - ${stock.name}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1)
                                            Text(String.format(Locale.US, "$%,.2f (+%.2f%%)", stock.currentPrice, stock.changePercentage), color = neonGreen, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Button(
                                            onClick = { 
                                                selectedStockToBuy = stock
                                                showBuyDialog = true
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = gold, contentColor = Color.Black),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Text("Beli", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    "Real Estate" -> {
                        item { com.example.ui.RealEstateScreen(viewModel) }
                    }
                    "Crypto" -> {
                        item { com.example.ui.CryptoScreen(viewModel) }
                    }
                    "Startups" -> {
                        item {
                            Surface(
                                color = slateDark,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(24.dp)) {
                                    Text("Startup Portfolio Value", color = textGray, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("$ 5,000,000.00", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }
                        }

                        item {
                            val startups = listOf(
                                Triple("NeuroLink AI", "Success Probability: 12%", "$1M"),
                                Triple("Quantum Bio", "Success Probability: 5%", "$5M")
                            )
                            
                            Column {
                                Text("Pitch Decks", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(12.dp))
                                startups.forEach { (name, prob, fund) ->
                                    Surface(
                                        color = cardDark,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                                Text(prob, color = textGray, fontSize = 12.sp)
                                            }
                                            Button(
                                                onClick = { },
                                                colors = ButtonDefaults.buttonColors(containerColor = gold, contentColor = Color.Black),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                                                modifier = Modifier.height(32.dp)
                                            ) {
                                                Text("Fund $fund", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(100.dp)) }
            }
        }
        
        // --- Buy Dialog ---
        if (showBuyDialog && selectedStockToBuy != null) {
            val stock = selectedStockToBuy!!
            val isIndo = stock.ticker.contains(".JK")
            val currentPrice = stock.currentPrice
            val balanceStr = String.format(Locale.US, "$%,.2f", playerState.cash.toDouble())
            
            androidx.compose.ui.window.Dialog(onDismissRequest = { showBuyDialog = false }) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = cardDark,
                    modifier = Modifier.padding(16.dp).fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("Beli Saham", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("${stock.name} (${stock.ticker})", color = textGray)
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text("Harga per Lembar", color = textGray, fontSize = 12.sp)
                        Text(String.format(Locale.US, "$%,.2f", currentPrice), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        OutlinedTextField(
                            value = buySharesAmount,
                            onValueChange = { buySharesAmount = it },
                            label = { Text("Jumlah Lembar") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = neonGreen,
                                unfocusedBorderColor = textGray,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        val quantity = buySharesAmount.toIntOrNull() ?: 0
                        val totalRequired = quantity * currentPrice
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Biaya:", color = textGray)
                            Text(String.format(Locale.US, "$%,.2f", totalRequired), color = if (totalRequired > playerState.cash) red else neonGreen)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Saldo Anda:", color = textGray)
                            Text(balanceStr, color = Color.White)
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        val canBuy = quantity > 0 && totalRequired <= playerState.cash
                        
                        Button(
                            onClick = {
                                viewModel.buyStock(stock.ticker, currentPrice, quantity)
                                buySuccessMessage = "Berhasil membeli $quantity lembar saham ${stock.ticker}"
                                buySharesAmount = ""
                                showBuyDialog = false
                            },
                            enabled = canBuy,
                            colors = ButtonDefaults.buttonColors(containerColor = neonGreen),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Text("Konfirmasi Beli", color = bgDark, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            onClick = { showBuyDialog = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Batal", color = textGray)
                        }
                    }
                }
            }
        }
        
        // --- Success Snackbar/Dialog (optional if wanted) ---
        if (buySuccessMessage != null) {
            androidx.compose.ui.window.Dialog(onDismissRequest = { buySuccessMessage = null }) {
                Surface(shape = RoundedCornerShape(12.dp), color = neonGreen.copy(alpha = 0.2f), border = BorderStroke(1.dp, neonGreen)) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(buySuccessMessage!!, color = Color.White, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { buySuccessMessage = null }, colors = ButtonDefaults.buttonColors(containerColor = neonGreen)) {
                            Text("OK", color = bgDark)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 2. BUSINESS SCREENS (Dashboard, Catalog, Detail)
// ==========================================
@Composable
fun BusinessDashboardScreen(navController: NavHostController, viewModel: GameViewModel) {
    val playerState by viewModel.playerState.collectAsState()

    var totalProjectedIncome = 0L
    playerState.ownedBusinesses.forEach { owned ->
        val catalogItem = businessCatalog.find { it.id == owned.catalogId }
        if (catalogItem != null) {
            val (rev, _) = getBusinessStats(owned, catalogItem)
            totalProjectedIncome += rev
        }
    }

    val stockList by viewModel.stockList.collectAsState()
    var estimatedStockYield = 0.0
    playerState.ownedStocks.forEach { owned ->
        val liveStock = stockList.find { it.ticker == owned.ticker }
        if (liveStock != null) {
            val stats = getMarketStats(liveStock.ticker, liveStock.currentPrice)
            estimatedStockYield += (owned.shares * liveStock.currentPrice) * (stats.dividendYield / 100.0 / 12.0)
        }
    }

    val totalMonthlyIncome = totalProjectedIncome + estimatedStockYield.toLong()

    Scaffold(
        containerColor = Color.Transparent // 1. SCREEN BACKGROUND
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 2. HEADER
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Bisnis",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Slot Bisnis",
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // 3. TOTAL INCOME CARD
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            text = currencyFormat.format(totalMonthlyIncome),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Total pendapatan per bulan (termasuk Dividen: ${currencyFormat.format(estimatedStockYield)})",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // 4. ACTION BUTTONS ROW
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { navController.navigate("business_catalog") },
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)
                    ) {
                        Text("Mulai Bisnis Baru", fontWeight = FontWeight.SemiBold)
                    }
                    
                    Button(
                        onClick = { /* Dummy action */ },
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        Text("Merger Bisnis", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // 5. LIST HEADER ("My companies")
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "Bisnis Saya",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${playerState.ownedBusinesses.size}/11",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // 6. COMPANY CARDS
            if (playerState.ownedBusinesses.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Belum ada bisnis", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                    }
                }
            } else {
                items(playerState.ownedBusinesses) { owned ->
                    val catalogItem = businessCatalog.find { it.id == owned.catalogId }
                    if (catalogItem != null) {
                        val (rev, _) = getBusinessStats(owned, catalogItem)
                        
                        val iconImage = when (catalogItem.category) {
                            BusinessCategory.PROPERTY -> Icons.Default.Home
                            BusinessCategory.FINANCE -> Icons.Default.Star
                            BusinessCategory.RETAIL, BusinessCategory.CULINARY -> Icons.Default.ShoppingCart
                            else -> Icons.Default.Build
                        }

                        Box(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { navController.navigate("business_detail/${catalogItem.id}") },
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(16.dp),
                                shadowElevation = 2.dp
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Left: Icon
                                    Surface(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = CircleShape,
                                        modifier = Modifier.size(50.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                iconImage,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.width(16.dp))
                                    
                                    // Middle: Content
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = catalogItem.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "Sektor ${catalogItem.category.name}",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                        )
                                        
                                        Spacer(modifier = Modifier.height(12.dp))
                                        
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Default.Star,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Lvl ${owned.level}",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Icon(
                                                Icons.Default.Add,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp),
                                                tint = MaterialTheme.colorScheme.secondary
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "${owned.purchasedUpgrades.size} / ${catalogItem.upgrades.size}",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                            )
                                        }
                                        
                                        Spacer(modifier = Modifier.height(8.dp))
                                        
                                        Row(verticalAlignment = Alignment.Bottom) {
                                            Text(
                                                text = if (rev == 0L) "$ 0" else currencyFormat.format(rev),
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (rev == 0L) "Pending" else "/bln",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                                modifier = Modifier.padding(bottom = 2.dp)
                                            )
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.width(8.dp))
                                    
                                    // Right (Arrow)
                                    Icon(
                                        Icons.Default.KeyboardArrowRight,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    )
                                }
                            }
                            
                            // Notification Badge (Red Circle) - Always on Finance as requested dummy
                            if (catalogItem.category == BusinessCategory.FINANCE) {
                                Surface(
                                    color = MaterialTheme.colorScheme.error, // Red
                                    shape = CircleShape,
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .offset(x = 8.dp, y = (-8).dp)
                                ) {
                                    Text(
                                        text = "!", // Required dummy
                                        color = MaterialTheme.colorScheme.onError,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(80.dp)) } // padding for bottom nav
        }
    }
}

@Composable
fun BusinessCatalogScreen(navController: NavHostController, viewModel: GameViewModel) {
    val playerState by viewModel.playerState.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Button(onClick = { navController.popBackStack() }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Kembali")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("🛒 Katalog Bisnis", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(businessCatalog) { catalogItem ->
                val isOwned = playerState.ownedBusinesses.any { it.catalogId == catalogItem.id }
                val canAfford = playerState.cash >= catalogItem.costToBuy

                if (!isOwned) {
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(50.dp).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Business, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(catalogItem.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                    Text("Sektor: ${catalogItem.category.name}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Text("Potensi Pendapatan: ${if(catalogItem.isFluctuating) "Fluktuaktif" else currencyFormat.format(catalogItem.monthlyRevenue) + "/bln"}", style = MaterialTheme.typography.bodyMedium)
                            Text("Biaya Perawatan: ${currencyFormat.format(catalogItem.monthlyMaintenanceCost)}/bln", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = { 
                                    viewModel.buyBusiness(catalogItem.id)
                                    navController.popBackStack()
                                },
                                enabled = canAfford,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = if (canAfford) MaterialTheme.colorScheme.secondary else Color.Gray)
                            ) {
                                Text(if (canAfford) "Beli ${currencyFormat.format(catalogItem.costToBuy)}" else "Dana Tidak Cukup", color = if (canAfford) MaterialTheme.colorScheme.onSecondary else Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BusinessDetailScreen(navController: NavHostController, viewModel: GameViewModel, businessId: String) {
    val playerState by viewModel.playerState.collectAsState()
    val catalogItem = businessCatalog.find { it.id == businessId }
    val ownedData = playerState.ownedBusinesses.find { it.catalogId == businessId }

    if (catalogItem == null || ownedData == null) {
        Text("Data tidak ditemukan.", modifier = Modifier.padding(16.dp))
        return
    }

    val (currentRev, currentMaint) = getBusinessStats(ownedData, catalogItem)

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Button(onClick = { navController.popBackStack() }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Kembali")
        }
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(catalogItem.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Sektor: ${catalogItem.category.name} • Level ${ownedData.level}", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        if (catalogItem.category == BusinessCategory.FINANCE) {
            val phase = when (ownedData.level) {
                in 1..10 -> "Microfinance/Credit Union"
                in 11..25 -> "Local City Bank"
                in 26..50 -> "National Commercial Bank"
                else -> "Global Investment Mega-Bank"
            }
            Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFD700).copy(alpha = 0.2f))) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD700))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Global Finance Empire", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                        Text("Fase Saat Ini: $phase", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
        
        // Financial Stats
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Row(modifier = Modifier.fillMaxWidth().padding(20.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Pendapatan / Bulan", color = Color.Gray, style = MaterialTheme.typography.labelMedium)
                    Text("+${currencyFormat.format(currentRev)}", color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Biaya / Bulan", color = Color.Gray, style = MaterialTheme.typography.labelMedium)
                    Text("-${currencyFormat.format(currentMaint)}", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Text("🔥 Upgrade Tersedia", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(catalogItem.upgrades) { upgrade ->
                val currentLevel = ownedData.upgradeLevels[upgrade.id] ?: if (ownedData.purchasedUpgrades.contains(upgrade.id)) 1 else 0
                val isMaxedOut = currentLevel >= upgrade.maxLevel
                val cost = getUpgradeCost(upgrade, currentLevel)
                val canAfford = playerState.cash >= cost

                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = if (isMaxedOut) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("${upgrade.name} (Lvl $currentLevel/${if (upgrade.maxLevel > 1) upgrade.maxLevel else "-"})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = if(isMaxedOut) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface)
                            if (isMaxedOut) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Maksimal", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(upgrade.description, style = MaterialTheme.typography.bodySmall, color = if(isMaxedOut) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha=0.7f) else Color.Gray)
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        if (!isMaxedOut) {
                            Button(
                                onClick = { viewModel.purchaseUpgrade(businessId, upgrade.id) },
                                enabled = canAfford,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = if (canAfford) MaterialTheme.colorScheme.secondary else Color.Gray)
                            ) {
                                Text(if (canAfford) "Upgrade (${currencyFormat.format(cost)})" else "Uang Kurang", color = if (canAfford) MaterialTheme.colorScheme.onSecondary else Color.White)
                            }
                        } else {
                            Text("Level Maksimal", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 3. EARNINGS SCREEN (Laporan Keuangan)
// ==========================================
@Composable
fun EarningsScreen(viewModel: GameViewModel) {
    val playerState by viewModel.playerState.collectAsState()
    val monthProgress by viewModel.monthProgress.collectAsState()
    
    // Hitung estimasi (Projected)
    var projectedIncome = 0L
    var projectedExpense = 0L
    playerState.ownedBusinesses.forEach { owned ->
        val catalogItem = businessCatalog.find { it.id == owned.catalogId }
        if (catalogItem != null) {
            val (rev, maint) = getBusinessStats(owned, catalogItem)
            projectedIncome += rev
            projectedExpense += maint
        }
    }
    
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            // Header & Time Tracker
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("💰 Keuangan", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Column(horizontalAlignment = Alignment.End) {
                    Text("Tahun ${playerState.inGameYear}", color = Color.Gray, style = MaterialTheme.typography.labelMedium)
                    Text("Bulan ${playerState.inGameMonth}", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            
            // Progress Payday
            Text("Progres ke Payday (Akhir Bulan):", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = monthProgress,
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
            
            // Global Summary Card
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(modifier = Modifier.padding(20.dp).fillMaxWidth()) {
                    Text("Net Profit (Bulan Lalu)", color = Color.Gray, style = MaterialTheme.typography.labelLarge)
                    val profitColor = if (playerState.lastMonthNetProfit >= 0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
                    val profitSign = if (playerState.lastMonthNetProfit >= 0) "+" else "-"
                    Text(
                        "$profitSign ${currencyFormat.format(kotlin.math.abs(playerState.lastMonthNetProfit.toDouble()))}", 
                        style = MaterialTheme.typography.displaySmall, 
                        color = profitColor, 
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color.DarkGray)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Estimasi Pemasukan", color = Color.Gray, style = MaterialTheme.typography.labelMedium)
                            Text(currencyFormat.format(projectedIncome), color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Estimasi Pengeluaran", color = Color.Gray, style = MaterialTheme.typography.labelMedium)
                            Text(currencyFormat.format(projectedExpense), color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            Text("Pemasukan (Cash Flow)", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        
        if (playerState.ownedBusinesses.isEmpty()) {
            item {
                Text("Belum ada sumber pendapatan. Beli bisnis untuk menghasilkan uang!", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            items(playerState.ownedBusinesses) { owned ->
                val catalogItem = businessCatalog.find { it.id == owned.catalogId }
                if (catalogItem != null) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${catalogItem.name} (Lvl ${owned.level})", color = MaterialTheme.colorScheme.onBackground)
                        val (rev, _) = getBusinessStats(owned, catalogItem)
                        val revStr = if (catalogItem.isFluctuating) "~${currencyFormat.format(rev)}" else currencyFormat.format(rev)
                        Text("+$revStr", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Pengeluaran (Expenses)", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        
        if (playerState.ownedBusinesses.isEmpty()) {
            item {
                Text("Belum ada pengeluaran rutin.", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            items(playerState.ownedBusinesses) { owned ->
                val catalogItem = businessCatalog.find { it.id == owned.catalogId }
                if (catalogItem != null) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${catalogItem.name} (Maintenance)", color = MaterialTheme.colorScheme.onBackground)
                        val (_, maint) = getBusinessStats(owned, catalogItem)
                        Text("-${currencyFormat.format(maint)}", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ==========================================
// 4. ITEMS SCREEN (Aset & Koleksi)
// ==========================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ItemsScreen() {
    // Premium Dark Mode Colors
    val bgDark = Color(0xFF121212)
    val cardDark = Color(0xFF1E1E1E)
    val slateDark = Color(0xFF1A1E24)
    val accentGold = Color(0xFFFFD700)
    val neonGreen = Color(0xFF39FF14)
    val dividerColor = Color(0xFF333333)

    // Data for collections
    val collectionsData = listOf(
        Triple("Retro Cars", Icons.Default.DirectionsCar, Color(0xFFE53935)),
        Triple("Jewels", Icons.Default.Diamond, Color(0xFF00BCD4)),
        Triple("Fine Art", Icons.Default.Palette, Color(0xFFFF9800)),
        Triple("NFTs", Icons.Default.Token, Color(0xFF9C27B0)),
        Triple("Private Islands", Icons.Default.Landscape, Color(0xFF4CAF50)),
        Triple("Sports Franchises", Icons.Default.SportsBasketball, Color(0xFFFF5722)),
        Triple("Space Rockets", Icons.Default.RocketLaunch, Color(0xFF607D8B)),
        Triple("Historical Artifacts", Icons.Default.Museum, Color(0xFF795548))
    )

    Scaffold(
        containerColor = bgDark
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 1. HEADER
            item {
                Text(
                    text = "Items",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // 2. FACILITIES ROW
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val facilities = listOf(
                        "Garage" to Icons.Default.Build,
                        "Hangar" to Icons.Default.Flight,
                        "Harbor" to Icons.Default.DirectionsBoat
                    )
                    facilities.forEach { (name, icon) ->
                        Surface(
                            color = cardDark,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.size(100.dp).padding(horizontal = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Icon(icon, contentDescription = name, tint = Color.White, modifier = Modifier.size(32.dp))
                                Text(name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // 3. SHOPS ROW
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val shops = listOf(
                        "Car Showroom" to Icons.Default.Storefront,
                        "Aircraft Shop" to Icons.Default.Store,
                        "Yacht Shop" to Icons.Default.Sailing
                    )
                    shops.forEach { (name, icon) ->
                        Surface(
                            color = bgDark,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, accentGold),
                            modifier = Modifier.size(100.dp).padding(horizontal = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Box(
                                    modifier = Modifier.weight(1f).fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(icon, contentDescription = name, tint = accentGold, modifier = Modifier.size(28.dp))
                                }
                                Surface(
                                    color = Color(0xFFB8860B), // Dark Gold
                                    modifier = Modifier.fillMaxWidth().height(28.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp, textAlign = TextAlign.Center)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 4. DIVIDER
            item {
                HorizontalDivider(color = dividerColor, thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))
            }

            // 5. RESIDENCE (Prime Asset Card)
            item {
                Surface(
                    color = slateDark,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().height(120.dp)
                    ) {
                        Surface(
                            color = Color(0xFF8B6508), // Dark bronze/gold
                            shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp),
                            modifier = Modifier.width(120.dp).fillMaxHeight()
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Home, contentDescription = "Residence", tint = Color.White, modifier = Modifier.size(48.dp))
                            }
                        }
                        
                        Column(
                            modifier = Modifier.weight(1f).padding(16.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Residence", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Row {
                                repeat(3) {
                                    Icon(Icons.Default.Star, contentDescription = "Tier", tint = accentGold, modifier = Modifier.size(16.dp))
                                }
                            }
                            Text("$ 664.1 M", color = neonGreen, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp)
                        }
                    }
                }
            }

            // 6. DIVIDER
            item {
                HorizontalDivider(color = dividerColor, thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))
            }

            // 7. COLLECTIONS
            item {
                Text("Collections", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }

            items(collectionsData.chunked(2)) { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    rowItems.forEach { (name, icon, iconColor) ->
                        Surface(
                            color = cardDark,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.weight(1f).aspectRatio(1f) // Square somewhat
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(icon, contentDescription = name, tint = iconColor, modifier = Modifier.size(40.dp))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp, textAlign = TextAlign.Center)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("0 of 20", color = Color.Gray, fontSize = 12.sp)
                            }
                        }
                    }
                    if (rowItems.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
            
            // padding for bottom nav
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

// ==========================================
// 5. PROFILE SCREEN (Ringkasan Pemain)
// ==========================================
fun parseSvgPath(pathStr: String): Path {
    val path = Path()
    try {
        val tokens = pathStr.trim().split(Regex("[\\s,]+"))
        var idx = 0
        while (idx < tokens.size) {
            val token = tokens[idx].uppercase()
            if (token.isEmpty()) {
                idx++
                continue
            }
            when (token) {
                "M" -> {
                    if (idx + 2 < tokens.size) {
                        val x = tokens[idx + 1].toFloatOrNull() ?: 0f
                        val y = tokens[idx + 2].toFloatOrNull() ?: 0f
                        path.moveTo(x, y)
                        idx += 3
                    } else {
                        idx++
                    }
                }
                "L" -> {
                    if (idx + 2 < tokens.size) {
                        val x = tokens[idx + 1].toFloatOrNull() ?: 0f
                        val y = tokens[idx + 2].toFloatOrNull() ?: 0f
                        path.lineTo(x, y)
                        idx += 3
                    } else {
                        idx++
                    }
                }
                "C" -> {
                    if (idx + 6 < tokens.size) {
                        val x1 = tokens[idx + 1].toFloatOrNull() ?: 0f
                        val y1 = tokens[idx + 2].toFloatOrNull() ?: 0f
                        val x2 = tokens[idx + 3].toFloatOrNull() ?: 0f
                        val y2 = tokens[idx + 4].toFloatOrNull() ?: 0f
                        val x3 = tokens[idx + 5].toFloatOrNull() ?: 0f
                        val y3 = tokens[idx + 6].toFloatOrNull() ?: 0f
                        path.cubicTo(x1, y1, x2, y2, x3, y3)
                        idx += 7
                    } else {
                        idx++
                    }
                }
                "Z" -> {
                    path.close()
                    idx++
                }
                else -> {
                    idx++
                }
            }
        }
    } catch (e: Exception) {
        path.reset()
        path.moveTo(50f, 10f)
        path.lineTo(90f, 95f)
        path.lineTo(10f, 95f)
        path.close()
    }
    return path
}

@Composable
fun SvgLogoPreview(
    svgPathStr: String,
    tintColorHex: String,
    modifier: Modifier = Modifier
) {
    val logoColor = remember(tintColorHex) {
        try {
            Color(android.graphics.Color.parseColor(tintColorHex))
        } catch (e: Exception) {
            Color(0xFFFFD700)
        }
    }
    
    androidx.compose.foundation.Canvas(modifier = modifier) {
        val scaleX = size.width / 100f
        val scaleY = size.height / 100f
        
        val composePath = parseSvgPath(svgPathStr)
        
        drawContext.canvas.save()
        drawContext.canvas.scale(scaleX, scaleY)
        
        drawPath(
            path = composePath,
            color = logoColor,
            style = androidx.compose.ui.graphics.drawscope.Fill
        )
        
        drawPath(
            path = composePath,
            color = Color.White.copy(alpha = 0.3f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f)
        )
        
        drawContext.canvas.restore()
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(viewModel: GameViewModel) {
    val player by viewModel.playerState.collectAsState()
    var showSettingsDialog by remember { mutableStateOf(false) }
    
    // Theme Colors
    val bgDark = Color(0xFF121212)
    val cardDark = Color(0xFF1A1E24)
    val gold = Color(0xFFFFD700)
    val darkGold = Color(0xFFB8860B)
    val neonGreen = Color(0xFF00FF00)
    val textGray = Color(0xFFA0A0A0)
    val dividerColor = Color(0xFF333333)
 
    val companyLogoSvgPath by viewModel.companyLogoSvgPath.collectAsState()
    val companyLogoFillColorHex by viewModel.companyLogoFillColorHex.collectAsState()
    val difficulty by viewModel.gameDifficulty.collectAsState()
    val stockList by viewModel.stockList.collectAsState()
    val cryptoList by viewModel.cryptoList.collectAsState()
    val realEstateMarket by viewModel.realEstateMarket.collectAsState()

    val stocksValue = player.ownedStocks.sumOf { owned ->
        val livePrice = stockList.find { it.ticker == owned.ticker }?.currentPrice ?: owned.averagePrice
        (owned.shares * livePrice).toLong()
    }
    
    val cryptoValue = player.ownedCrypto.sumOf { owned ->
        val livePrice = cryptoList.find { it.symbol == owned.symbol }?.currentPrice ?: owned.averagePrice
        (owned.amount * livePrice).toLong()
    }
    
    val realEstateValue = player.ownedProperties.sumOf { owned ->
        val prop = realEstateMarket.find { it.id == owned.propertyId }
        prop?.basePrice ?: owned.purchasedPrice
    }
    
    val businessValue = player.ownedBusinesses.sumOf { it.level * 5000L } // Rough estimation
    val totalWealth = player.cash + stocksValue + businessValue + cryptoValue + realEstateValue

    Scaffold(
        containerColor = bgDark
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 1. HEADER
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Profile",
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = cardDark,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = "Avatar",
                                    tint = gold,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = "Menu",
                            tint = gold
                        )
                    }
                }
            }

            // 1.5 COMPANIES BRAND IDENTITY (Dynamic SVG Player Branding)
            item {
                Surface(
                    color = cardDark,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, gold.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = bgDark,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(10.dp)) {
                                SvgLogoPreview(
                                    svgPathStr = companyLogoSvgPath,
                                    tintColorHex = companyLogoFillColorHex,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("META CONGLOMERATE CORP", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Scenario Mode: $difficulty", color = gold, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Text("Custom SVG Branding Active", color = textGray, fontSize = 11.sp)
                        }
                    }
                }
            }
 
            // 2. FORTUNE & PORTFOLIO DISTRIBUTION
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = currencyFormat.format(totalWealth), 
                        color = neonGreen,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Total Fortune",
                        color = textGray,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Segmented Progress Bar dummy
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                    ) {
                        val barWeights = mutableListOf(
                            Pair(Math.max(1f, (player.cash.toFloat() / totalWealth.toFloat())), Color(0xFF2196F3)),
                            Pair(Math.max(0f, (stocksValue.toFloat() / totalWealth.toFloat())), Color(0xFFFF9800)),
                            Pair(Math.max(0f, (businessValue.toFloat() / totalWealth.toFloat())), Color(0xFFF44336)),
                            Pair(Math.max(0f, (realEstateValue.toFloat() / totalWealth.toFloat())), Color(0xFF9C27B0)),
                            Pair(Math.max(0f, (cryptoValue.toFloat() / totalWealth.toFloat())), Color(0xFFE91E63))
                        )
                        barWeights.forEach { (w, color) ->
                            if (w > 0f) {
                                Box(modifier = Modifier.weight(w).fillMaxHeight().background(color))
                            }
                        }
                    }
                }
            }

            // 3. WEALTH BREAKDOWN (Grid)
            item {
                val wealthItems = listOf(
                    Triple("Balance", currencyFormat.format(player.cash), Color(0xFF2196F3)),
                    Triple("Businesses", currencyFormat.format(businessValue), Color(0xFFF44336)),
                    Triple("Stocks", currencyFormat.format(stocksValue), Color(0xFFFF9800)),
                    Triple("Real estate", currencyFormat.format(realEstateValue), Color(0xFF9C27B0)),
                    Triple("Crypto", currencyFormat.format(cryptoValue), Color(0xFFE91E63)),
                    Triple("Collections", "$0 (Not Dev)", Color(0xFF00BCD4)),
                    Triple("Vehicles", "$0 (Not Dev)", Color(0xFF4CAF50)),
                    Triple("Banks", "$0 (Not Dev)", Color(0xFFFFC107))
                )
                
                // Chunk into rows of 2
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    wealthItems.chunked(2).forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowItems.forEach { (label, value, barColor) ->
                                Surface(
                                    color = cardDark,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .width(4.dp)
                                                .height(32.dp)
                                                .clip(RoundedCornerShape(2.dp))
                                                .background(barColor)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(label, color = textGray, fontSize = 12.sp)
                                            Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        }
                                    }
                                }
                            }
                            if (rowItems.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            // 4. TAX HAVENS & LEGAL
            item {
                Surface(
                    color = Color(0xFF1E293B), // Dark blue/gray
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, gold.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.AccountBalance, contentDescription = "Tax", tint = gold, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Tax & Legal Department", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = textGray)
                    }
                }
            }

            // 5. GLOBAL TYCOON INDEX
            item {
                Surface(
                    color = darkGold,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Global Tycoon Index", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("#189 - Your Rating", color = Color.Black, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                        }
                        Icon(Icons.Default.EmojiEvents, contentDescription = "Trophy", tint = Color.Black.copy(alpha = 0.5f), modifier = Modifier.size(48.dp))
                    }
                }
            }

            // 6. MASTER STATISTICS & EARNINGS BOARD
            item {
                Surface(
                    color = Color(0xFF0F2027), // deep emerald/teal to dark blueish block
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        // Section A: Statistics
                        Text("Statistics", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        val stats = listOf(
                            "Number of businesses" to "${player.ownedBusinesses.size}",
                            "Real estate" to "79 of 138",
                            "Cars" to "1",
                            "Aircraft" to "0",
                            "Private Islands" to "2"
                        )
                        
                        stats.forEach { (label, value) ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(label, color = textGray, fontSize = 14.sp)
                                Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = dividerColor)
                        Spacer(modifier = Modifier.height(16.dp))

                        // Section B: Earned
                        Text("Earned", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        val earnings = listOf(
                            "In business" to "$ 3.0 B",
                            "On rent" to "$ 150.5 M",
                            "On dividends" to "$ 80.0 M",
                            "On crypto trading" to "$ 12.3 M"
                        )
                        
                        earnings.forEach { (label, value) ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(label, color = textGray, fontSize = 14.sp)
                                Text(value, color = neonGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }

            // 7. FOOTER
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        color = cardDark,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showSettingsDialog = true }
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(16.dp)) {
                            Text("Help & Settings", color = gold, fontWeight = FontWeight.Bold)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Join our community:", color = textGray, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val icons = listOf(Icons.Default.Chat, Icons.Default.Send, Icons.Default.Tag, Icons.Default.Language)
                        icons.forEach { icon ->
                            Surface(
                                shape = CircleShape,
                                color = cardDark,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    if (showSettingsDialog) {
        val isNotificationEnabled by viewModel.isNotificationEnabled.collectAsState()
        val isDarkModeSimulated by viewModel.isDarkModeSimulated.collectAsState()
        val soundVolume by viewModel.soundVolume.collectAsState()
        val gameDifficulty by viewModel.gameDifficulty.collectAsState()
        
        val monthDurationSeconds by viewModel.monthDurationSeconds.collectAsState()
        val stockIntervalSeconds by viewModel.stockIntervalSeconds.collectAsState()
        val marketVolatilityFactor by viewModel.marketVolatilityFactor.collectAsState()
        
        val companyLogoSvgPath by viewModel.companyLogoSvgPath.collectAsState()
        val companyLogoFillColorHex by viewModel.companyLogoFillColorHex.collectAsState()
        
        var selectedTab by remember { mutableStateOf(0) } // 0: Umum, 1: Advanced
        
        // Modal copy values
        var localSvgPath by remember { mutableStateOf(companyLogoSvgPath) }
        var localColorHex by remember { mutableStateOf(companyLogoFillColorHex) }
        
        var localNotification by remember { mutableStateOf(isNotificationEnabled) }
        var localDarkTheme by remember { mutableStateOf(isDarkModeSimulated) }
        var localVolume by remember { mutableStateOf(soundVolume) }
        var localDifficulty by remember { mutableStateOf(gameDifficulty) }
        
        var localMonthDuration by remember { mutableStateOf(monthDurationSeconds) }
        var localStockInterval by remember { mutableStateOf(stockIntervalSeconds) }
        var localVolatility by remember { mutableStateOf(marketVolatilityFactor) }
        
        var showResetProgressConfirm by remember { mutableStateOf(false) }

        // States for Adding Real Estate
        var customPropName by remember { mutableStateOf("") }
        var customPropLocation by remember { mutableStateOf("") }
        var customPropPrice by remember { mutableStateOf("") }
        var customPropRent by remember { mutableStateOf("") }


        androidx.compose.ui.window.Dialog(
            onDismissRequest = { showSettingsDialog = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.85f)
                    .clip(RoundedCornerShape(24.dp)),
                color = Color(0xFF15181F),
                border = BorderStroke(1.dp, Color(0xFF2C3240))
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Header Area
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1F2430))
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Settings, contentDescription = null, tint = gold, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Help & Settings", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                        IconButton(onClick = { showSettingsDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }
                    
                    // Tabs
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color(0xFF1A1F2B),
                        contentColor = gold
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Settings, contentDescription = null, tint = if (selectedTab == 0) gold else textGray, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Umum", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }},
                            selectedContentColor = gold,
                            unselectedContentColor = textGray
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Tune, contentDescription = null, tint = if (selectedTab == 1) gold else textGray, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Advanced Settings", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }},
                            selectedContentColor = gold,
                            unselectedContentColor = textGray
                        )
                    }

                    // Content Scrollable
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        if (selectedTab == 0) {
                            Text("PENGATURAN UMUM", color = gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)

                            // simulated dark mode
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Simulated Dark Theme", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Mengaktifkan tema gelap kustom premium", color = textGray, fontSize = 11.sp)
                                }
                                Switch(
                                    checked = localDarkTheme,
                                    onCheckedChange = { 
                                        localDarkTheme = it
                                        viewModel.updateGeneralSettings(localNotification, localDarkTheme, localVolume, localDifficulty, localVolatility)
                                    },
                                    colors = SwitchDefaults.colors(checkedThumbColor = gold, checkedTrackColor = gold.copy(alpha = 0.5f))
                                )
                            }

                            HorizontalDivider(color = dividerColor.copy(alpha = 0.5f))

                            // warning alerts
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Transaction Warnings", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Dapatkan konfirmasi peringatan transaksi beresiko tinggi", color = textGray, fontSize = 11.sp)
                                }
                                Switch(
                                    checked = localNotification,
                                    onCheckedChange = { 
                                        localNotification = it
                                        viewModel.updateGeneralSettings(localNotification, localDarkTheme, localVolume, localDifficulty, localVolatility)
                                    },
                                    colors = SwitchDefaults.colors(checkedThumbColor = gold, checkedTrackColor = gold.copy(alpha = 0.5f))
                                )
                            }

                            HorizontalDivider(color = dividerColor.copy(alpha = 0.5f))

                            // SFX volume slider
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("System Sound & SFX Volume", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("${(localVolume * 100).toInt()}%", color = gold, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                }
                                Slider(
                                    value = localVolume,
                                    onValueChange = { 
                                        localVolume = it
                                        viewModel.updateGeneralSettings(localNotification, localDarkTheme, localVolume, localDifficulty, localVolatility)
                                    },
                                    valueRange = 0f..1f,
                                    colors = SliderDefaults.colors(thumbColor = gold, activeTrackColor = gold)
                                )
                            }

                            HorizontalDivider(color = dividerColor.copy(alpha = 0.5f))

                            // Game preset scenario simulation modes
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("SKENARIO GAMEPLAY", color = gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                
                                val difficulties = listOf(
                                    "Easy" to "Wirausaha Pemula (Peluang modal besar, krisis 0%)",
                                    "Normal" to "Konglomerat (Standard multiplier)",
                                    "Hard" to "Krisis Finansial (-15% profit bisnis secara berkala)",
                                    "Elite Tycoon" to "Krisis Ekonomi Global (Uang disusut inflasi 2.5% per tahun)"
                                )
                                
                                difficulties.forEach { (diff, desc) ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { 
                                                localDifficulty = diff
                                                viewModel.updateGeneralSettings(localNotification, localDarkTheme, localVolume, localDifficulty, localVolatility)
                                            }
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = localDifficulty == diff,
                                            onClick = { 
                                                localDifficulty = diff
                                                viewModel.updateGeneralSettings(localNotification, localDarkTheme, localVolume, localDifficulty, localVolatility)
                                            },
                                            colors = RadioButtonDefaults.colors(selectedColor = gold, unselectedColor = textGray)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(diff, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text(desc, color = textGray, fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        } else {
                            // ==========================================
                            // TAB 1: ADVANCED SETTINGS (EXPERIMENT MINI GAME)
                            // ==========================================
                            Text("EXPERIMENT: CORPORATE BRAND LOGO (CUSTOM SVG)", color = gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF0F1218), RoundedCornerShape(12.dp))
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = Color(0xFF1D222E),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.size(80.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(12.dp)) {
                                        SvgLogoPreview(
                                            svgPathStr = localSvgPath,
                                            tintColorHex = localColorHex,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text("Logo ID Preview (Canvas)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Diskalakan dinamis range (0,0) s.d (100,100)", color = textGray, fontSize = 11.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Logo Color Tint: $localColorHex", color = gold, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                            
                            // Presets grid (2 Columns / row structure)
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Pilih Preset Desain Bawaan:", color = textGray, fontSize = 12.sp)
                                val presets = listOf(
                                    "Crown" to "M 10 90 L 10 30 L 35 60 L 50 20 L 65 60 L 90 30 L 90 90 Z",
                                    "Star" to "M 50 5 L 63 33 L 94 38 L 71 61 L 76 92 L 50 78 L 24 92 L 29 61 L 6 38 L 37 33 Z",
                                    "Diamond" to "M 50 5 L 85 40 L 50 95 L 15 40 Z",
                                    "Shield" to "M 50 5 L 90 20 L 90 60 C 90 85 50 98 50 98 C 50 98 10 85 10 60 L 10 20 Z",
                                    "Rocket" to "M 50 5 C 65 25 70 50 70 80 Q 60 75 50 95 Q 40 75 30 80 C 30 50 35 25 50 5 Z"
                                )
                                
                                presets.chunked(3).forEach { rowItems ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        rowItems.forEach { (name, path) ->
                                            Surface(
                                                color = if (localSvgPath.trim() == path.trim()) gold else Color(0xFF222834),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clickable { 
                                                        localSvgPath = path
                                                        viewModel.updateCompanyLogo(localSvgPath, localColorHex)
                                                    }
                                            ) {
                                                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 8.dp)) {
                                                    Text(
                                                        text = name,
                                                        color = if (localSvgPath.trim() == path.trim()) Color.Black else Color.White,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                        if (rowItems.size < 3) {
                                            repeat(3 - rowItems.size) {
                                                Spacer(modifier = Modifier.weight(1f))
                                            }
                                        }
                                    }
                                }
                            }
                            
                            // Custom Path Text Entry
                            OutlinedTextField(
                                value = localSvgPath,
                                onValueChange = { 
                                    localSvgPath = it
                                    viewModel.updateCompanyLogo(localSvgPath, localColorHex)
                                },
                                label = { Text("Custom SVG Path Data", color = gold) },
                                textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 11.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace),
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = gold,
                                    unfocusedBorderColor = Color(0xFF2C3240)
                                )
                            )
                            
                            // Tint Color Code Entry
                            OutlinedTextField(
                                value = localColorHex,
                                onValueChange = { 
                                    localColorHex = it
                                    viewModel.updateCompanyLogo(localSvgPath, localColorHex)
                                },
                                label = { Text("Warna Logo (Hex Code, misal: #FFD700)", color = gold) },
                                textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 13.sp),
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = gold,
                                    unfocusedBorderColor = Color(0xFF2C3240)
                                )
                            )

                            HorizontalDivider(color = dividerColor.copy(alpha = 0.5f))

                            Text("EXPERIMENT: LOOP DURATION CYCLES", color = gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)

                            // Slider: Month game cycle speed rate
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Kecepatan Durasi 1 Bulan Game", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("${localMonthDuration.toInt()} Detik", color = gold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                                Slider(
                                    value = localMonthDuration,
                                    onValueChange = { 
                                        localMonthDuration = it
                                        viewModel.updateMonthDuration(localMonthDuration)
                                    },
                                    valueRange = 5f..300f,
                                    colors = SliderDefaults.colors(thumbColor = gold, activeTrackColor = gold)
                                )
                                Text(
                                    text = "Mengatur seberapa lama (dalam detik nyata) satu siklus bulan pendapatan bisnis Anda berganti.",
                                    color = textGray,
                                    fontSize = 11.sp
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(6.dp))

                            // Slider: Stock fluctuating frequency
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Rentang Fluktuasi Pasar Saham", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("${String.format(Locale.US, "%.1f", localStockInterval)} Detik", color = gold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                                Slider(
                                    value = localStockInterval,
                                    onValueChange = { 
                                        localStockInterval = it
                                        viewModel.updateStockInterval(localStockInterval)
                                    },
                                    valueRange = 1.0f..15.0f,
                                    colors = SliderDefaults.colors(thumbColor = gold, activeTrackColor = gold)
                                )
                                Text(
                                    text = "Kecepatan waktu delay refresh pergeseran harga saham naik/turun di tab bursa investasi.",
                                    color = textGray,
                                    fontSize = 11.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Slider: Volatility
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Intensitas Volatilitas Pasar", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("${String.format(Locale.US, "%.1f", localVolatility)}x", color = gold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                                Slider(
                                    value = localVolatility,
                                    onValueChange = { 
                                        localVolatility = it
                                        viewModel.updateGeneralSettings(localNotification, localDarkTheme, localVolume, localDifficulty, localVolatility)
                                    },
                                    valueRange = 0.1f..5.0f,
                                    colors = SliderDefaults.colors(thumbColor = gold, activeTrackColor = gold)
                                )
                                Text(
                                    text = "Memperkuat efek naik turun grafik/harga saham. Nilai tinggi membuat bursa kian liar (Sangat Spekulatif!).",
                                    color = textGray,
                                    fontSize = 11.sp
                                )
                            }

                            HorizontalDivider(color = dividerColor.copy(alpha = 0.5f))

                            // Custom Real Estate entry
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("TAMBAH PROPERTI KUSTOM", color = gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                OutlinedTextField(
                                    value = customPropName,
                                    onValueChange = { customPropName = it },
                                    label = { Text("Nama Properti", color = textGray) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = gold, unfocusedBorderColor = textGray, focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = customPropLocation,
                                    onValueChange = { customPropLocation = it },
                                    label = { Text("Lokasi", color = textGray) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = gold, unfocusedBorderColor = textGray, focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = customPropPrice,
                                    onValueChange = { customPropPrice = it.filter { char -> char.isDigit() } },
                                    label = { Text("Harga Beli ($)", color = textGray) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = gold, unfocusedBorderColor = textGray, focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = customPropRent,
                                    onValueChange = { customPropRent = it.filter { char -> char.isDigit() } },
                                    label = { Text("Sewa Bulanan ($)", color = textGray) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = gold, unfocusedBorderColor = textGray, focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                                    singleLine = true
                                )
                                Button(
                                    onClick = {
                                        val price = customPropPrice.toLongOrNull()
                                        val rent = customPropRent.toLongOrNull()
                                        if (customPropName.isNotBlank() && customPropLocation.isNotBlank() && price != null && rent != null) {
                                            viewModel.addProperty(customPropName, customPropLocation, price, rent)
                                            customPropName = ""
                                            customPropLocation = ""
                                            customPropPrice = ""
                                            customPropRent = ""
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = gold, contentColor = Color.Black)
                                ) {
                                    Text("Simpan ke Pasar", fontWeight = FontWeight.Bold)
                                }
                            }
                            
                            HorizontalDivider(color = dividerColor.copy(alpha = 0.5f))

                            // Reset Progression controls
                            Text("DANGER ZONE", color = Color(0xFFFF3B30), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            
                            Button(
                                onClick = { showResetProgressConfirm = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF3B30)),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.DeleteForever, contentDescription = null, tint = Color.White)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Reset Seluruh Progress Game", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Reset Confirmation dialogues
        if (showResetProgressConfirm) {
            AlertDialog(
                onDismissRequest = { showResetProgressConfirm = false },
                containerColor = Color(0xFF1E222D),
                title = { Text("Hapus Semua Progress?", color = Color.White, fontWeight = FontWeight.Bold) },
                text = { Text("Apakah Anda yakin ingin mengatur ulang profil konglomerat Anda ke kondisi awal awal ($5,000)? Semua sejarah, portofolio, dan kepemilikan bisnis akan dihapus.", color = textGray) },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.resetGameProgress()
                            showResetProgressConfirm = false
                            showSettingsDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF3B30))
                    ) {
                        Text("Reset Permanen", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetProgressConfirm = false }) {
                        Text("Batal", color = Color.White)
                    }
                }
            )
        }
    }
}

// ==========================================
