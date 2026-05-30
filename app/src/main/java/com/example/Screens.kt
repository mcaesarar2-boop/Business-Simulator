package com.example

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

// Format Uang Default
val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US).apply { maximumFractionDigits = 0 }

// ==========================================
// 1. INVESTING SCREEN (Dashboard Investasi)
// ==========================================
@Composable
fun InvestingScreen(navController: NavHostController) {
    // Theme Colors
    val bgDark = Color(0xFF121212)
    val cardDark = Color(0xFF1E1E1E)
    val slateDark = Color(0xFF252A34)
    val gold = Color(0xFFFFD700)
    val darkGold = Color(0xFFB8860B)
    val neonGreen = Color(0xFF00FF00)
    val textGray = Color(0xFFA0A0A0)
    val red = Color(0xFFFF3B30)

    var activeTab by remember { mutableStateOf("Stocks") }
    val tabs = listOf("Stocks", "Real Estate", "Crypto", "Startups")

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
                                    Text("$ 14,500,200.00", color = neonGreen, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("+ $1.2M (8.5%)", color = neonGreen, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text("Estimated yield per month", color = textGray, fontSize = 14.sp)
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
                            val stableIncome = listOf(
                                Triple("GLBL", "5.32%", Icons.Default.Public),
                                Triple("INFR", "4.15%", Icons.Default.Domain)
                            )
                            
                            val growthPotential = listOf(
                                Pair("TECHX", "+20.25%"),
                                Pair("AIINC", "+45.10%")
                            )

                            Column {
                                Text("Stable income", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(12.dp))
                                stableIncome.forEach { (ticker, yield, icon) ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(shape = CircleShape, color = cardDark, modifier = Modifier.size(40.dp)) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(icon, contentDescription = null, tint = gold, modifier = Modifier.size(20.dp))
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(ticker, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                                Text(yield, color = textGray, fontSize = 12.sp)
                                            }
                                        }
                                        Button(
                                            onClick = { },
                                            colors = ButtonDefaults.buttonColors(containerColor = gold, contentColor = Color.Black),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Text("Buy", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(24.dp))
                                Text("Growth potential", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(12.dp))
                                growthPotential.forEach { (ticker, growth) ->
                                     Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(ticker, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                            Text(growth, color = neonGreen, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                        }
                                        Button(
                                            onClick = { },
                                            colors = ButtonDefaults.buttonColors(containerColor = gold, contentColor = Color.Black),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Text("Buy", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    "Real Estate" -> {
                        item {
                            Column {
                                Text("$ 14,342,657.42", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Rental income per month", color = textGray, fontSize = 14.sp)
                            }
                        }
                        
                        item {
                            Surface(
                                color = slateDark,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth().height(150.dp)
                            ) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    Icon(Icons.Default.Public, contentDescription = null, tint = Color.White.copy(alpha = 0.05f), modifier = Modifier.align(Alignment.CenterEnd).size(120.dp).offset(x = 20.dp))
                                    Column(modifier = Modifier.padding(20.dp).align(Alignment.BottomStart)) {
                                        Text("Real estate market", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("Buy properties all over the world...", color = textGray, fontSize = 14.sp)
                                    }
                                }
                            }
                        }

                        item {
                            Surface(
                                color = slateDark,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth().height(150.dp)
                            ) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    Icon(Icons.Default.Home, contentDescription = null, tint = Color.White.copy(alpha = 0.05f), modifier = Modifier.align(Alignment.CenterEnd).size(120.dp).offset(x = 20.dp))
                                    Column(modifier = Modifier.padding(20.dp).align(Alignment.BottomStart)) {
                                        Text("My property", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("List of my properties", color = textGray, fontSize = 14.sp)
                                    }
                                    Surface(
                                        color = red,
                                        shape = CircleShape,
                                        modifier = Modifier.align(Alignment.TopEnd).padding(16.dp).size(12.dp)
                                    ) {}
                                }
                            }
                        }
                    }
                    "Crypto" -> {
                        item {
                            Surface(
                                color = cardDark,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(24.dp)) {
                                    Text("Total cryptocurrency value", color = textGray, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("$ 120,400.00", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("+ $19.6M (3.31%)", color = neonGreen, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = { },
                                        colors = ButtonDefaults.buttonColors(containerColor = slateDark),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                                        modifier = Modifier.height(36.dp)
                                    ) {
                                        Text("Trade", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }
                        }

                        item {
                            val coins = listOf(
                                Triple("Bitcoin", "500.00 BTC", Pair("$ 20.5 M", "+28.21%")),
                                Triple("Ethereum", "4500.00 ETH", Pair("$ 12.5 M", "+15.42%")),
                                Triple("Solana", "10000.00 SOL", Pair("$ 1.5 M", "-5.21%"))
                            )
                            
                            Column {
                                Text("Coins", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(12.dp))
                                coins.forEach { (coin, amount, valuePair) ->
                                    val (fiat, change) = valuePair
                                    val isGaining = change.startsWith("+")
                                    val colorIcon = when(coin) {
                                        "Bitcoin" -> Color(0xFFF7931A)
                                        "Ethereum" -> Color(0xFF627EEA)
                                        else -> Color(0xFF14F195)
                                    }
                                    
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(shape = CircleShape, color = colorIcon, modifier = Modifier.size(40.dp)) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(Icons.Default.CurrencyBitcoin, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(coin, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                                Text(amount, color = textGray, fontSize = 12.sp)
                                            }
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(fiat, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(if (isGaining) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown, contentDescription = null, tint = if(isGaining) neonGreen else red, modifier = Modifier.size(16.dp))
                                                Text(change, color = if(isGaining) neonGreen else red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
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
                            text = currencyFormat.format(totalProjectedIncome),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Total pendapatan per bulan",
                            fontSize = 14.sp,
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
                progress = { playerState.monthProgress },
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
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(viewModel: GameViewModel) {
    val player by viewModel.playerState.collectAsState()
    
    // Theme Colors
    val bgDark = Color(0xFF121212)
    val cardDark = Color(0xFF1A1E24)
    val gold = Color(0xFFFFD700)
    val darkGold = Color(0xFFB8860B)
    val neonGreen = Color(0xFF00FF00)
    val textGray = Color(0xFFA0A0A0)
    val dividerColor = Color(0xFF333333)

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

            // 2. FORTUNE & PORTFOLIO DISTRIBUTION
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "$ 3.9 B", 
                        color = neonGreen,
                        fontSize = 40.sp,
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
                        Box(modifier = Modifier.weight(0.3f).fillMaxHeight().background(Color(0xFF2196F3)))
                        Box(modifier = Modifier.weight(0.4f).fillMaxHeight().background(Color(0xFFF44336)))
                        Box(modifier = Modifier.weight(0.15f).fillMaxHeight().background(Color(0xFF9C27B0)))
                        Box(modifier = Modifier.weight(0.1f).fillMaxHeight().background(Color(0xFFFF9800)))
                        Box(modifier = Modifier.weight(0.05f).fillMaxHeight().background(Color(0xFF4CAF50)))
                    }
                }
            }

            // 3. WEALTH BREAKDOWN (Grid)
            item {
                val wealthItems = listOf(
                    Triple("Balance", currencyFormat.format(player.cash), Color(0xFF2196F3)),
                    Triple("Businesses", "$ 1.2 B", Color(0xFFF44336)),
                    Triple("Stocks", "$ 450.0 M", Color(0xFFFF9800)),
                    Triple("Real estate", "$ 800.0 M", Color(0xFF9C27B0)),
                    Triple("Crypto", "$ 210.3 M", Color(0xFFE91E63)),
                    Triple("Collections", "$ 180.0 M", Color(0xFF00BCD4)),
                    Triple("Vehicles", "$ 40.0 M", Color(0xFF4CAF50)),
                    Triple("Banks", "$ 154.2 M", Color(0xFFFFC107))
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
                        modifier = Modifier.fillMaxWidth()
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
}

// ==========================================
// 6. GLOBAL STOCK MARKET SCREEN
// ==========================================
data class StockItem(
    val id: String,
    val ticker: String,
    val name: String,
    val currentPrice: Double,
    val changeAbsolute: Double,
    val changePercentage: Double,
    val sector: String
)

fun generateStockData(): List<StockItem> {
    val coreList = listOf(
        StockItem("1", "AAPL", "Apple Inc.", 175.50, 1.20, 0.68, "Technology"),
        StockItem("2", "MSFT", "Microsoft Corp.", 330.12, 2.50, 0.76, "Technology"),
        StockItem("3", "GOOGL", "Alphabet Inc.", 135.40, -1.10, -0.80, "Technology"),
        StockItem("4", "NVDA", "NVIDIA Corp.", 450.20, 15.30, 3.50, "Technology"),
        StockItem("5", "TSLA", "Tesla Inc.", 240.10, -5.20, -2.10, "Automotive"),
        StockItem("6", "BBCA.JK", "Bank Central Asia", 60.50, 0.50, 0.83, "Finance"),
        StockItem("7", "BBRI.JK", "Bank Raky. Indo.", 40.20, 0.20, 0.50, "Finance"),
        StockItem("8", "GOTO.JK", "GoTo Gojek Toko", 5.50, -0.10, -1.80, "Technology"),
        StockItem("9", "TLKM.JK", "Telkom Indonesia", 30.10, 0.15, 0.50, "Communication"),
        StockItem("10", "TM", "Toyota Motor", 160.00, 1.20, 0.75, "Automotive"),
        StockItem("11", "RACE", "Ferrari N.V.", 300.50, 3.40, 1.10, "Automotive"),
        StockItem("12", "MC.PA", "LVMH", 800.00, 12.00, 1.50, "Luxury"),
        StockItem("13", "META", "Meta Platforms", 300.00, 5.00, 1.60, "Technology"),
        StockItem("14", "AMZN", "Amazon.com Inc.", 140.20, -0.50, -0.35, "Retail"),
        StockItem("15", "JPM", "JPMorgan Chase", 150.00, 1.10, 0.70, "Finance"),
        StockItem("16", "BAC", "Bank of America", 28.50, -0.20, -0.70, "Finance"),
        StockItem("17", "WMT", "Walmart Inc.", 160.00, 0.50, 0.30, "Retail"),
        StockItem("18", "JNJ", "Johnson & Johnson", 165.00, -1.00, -0.60, "Healthcare"),
        StockItem("19", "V", "Visa Inc.", 240.00, 2.00, 0.80, "Finance"),
        StockItem("20", "PG", "Procter & Gamble", 155.00, 0.80, 0.50, "Consumer"),
        StockItem("21", "MA", "Mastercard Inc.", 400.00, 3.50, 0.80, "Finance"),
        StockItem("22", "HD", "Home Depot Inc.", 330.00, -2.00, -0.60, "Retail"),
        StockItem("23", "CVX", "Chevron Corp.", 160.00, 1.50, 0.90, "Energy"),
        StockItem("24", "LLY", "Eli Lilly", 500.00, 4.00, 0.80, "Healthcare"),
        StockItem("25", "ABBV", "AbbVie Inc.", 150.00, -1.50, -1.00, "Healthcare"),
        StockItem("26", "MRK", "Merck & Co.", 110.00, 0.50, 0.40, "Healthcare"),
        StockItem("27", "PEP", "PepsiCo Inc.", 180.00, 1.00, 0.50, "Consumer"),
        StockItem("28", "KO", "Coca-Cola Co.", 60.00, 0.20, 0.30, "Consumer"),
        StockItem("29", "AVGO", "Broadcom Inc.", 850.00, 10.00, 1.10, "Technology"),
        StockItem("30", "ASML", "ASML Holding", 650.00, -5.00, -0.70, "Technology"),
        StockItem("31", "TTE", "TotalEnergies", 60.00, 0.50, 0.80, "Energy"),
        StockItem("32", "NVO", "Novo Nordisk", 100.00, 2.00, 2.00, "Healthcare"),
        StockItem("33", "NVS", "Novartis AG", 100.00, 1.00, 1.00, "Healthcare"),
        StockItem("34", "AZN", "AstraZeneca", 70.00, -0.50, -0.70, "Healthcare"),
        StockItem("35", "SHEL", "Shell plc", 65.00, 0.80, 1.20, "Energy"),
        StockItem("36", "TMUS", "T-Mobile US", 140.00, 1.50, 1.00, "Communication"),
        StockItem("37", "CMCSA", "Comcast Corp.", 45.00, -0.20, -0.40, "Communication"),
        StockItem("38", "DIS", "Walt Disney Co", 80.00, -1.00, -1.20, "Communication"),
        StockItem("39", "NFLX", "Netflix Inc.", 400.00, 5.00, 1.25, "Communication"),
        StockItem("40", "ADBE", "Adobe Inc.", 520.00, 6.00, 1.10, "Technology")
    )
    val list = mutableListOf<StockItem>()
    list.addAll(coreList)
    
    // Auto generate to reach 200 items (realistic proxy)
    var idCounter = 41
    for (i in 1..4) {
        coreList.forEach { stock ->
            val randomFactor = 1.0 + (Math.random() * 0.2 - 0.1) // +/- 10%
            val newPrice = stock.currentPrice * randomFactor
            val newChange = stock.changeAbsolute * randomFactor * (if (Math.random() > 0.5) 1 else -1)
            val newChangePct = (newChange / newPrice) * 100
            
            list.add(
                stock.copy(
                    id = idCounter.toString(),
                    ticker = "${stock.ticker}.R$i",
                    name = "${stock.name} Rg$i",
                    currentPrice = newPrice,
                    changeAbsolute = newChange,
                    changePercentage = newChangePct
                )
            )
            idCounter++
        }
    }
    return list.take(200)
}

@Composable
fun GlobalStockMarketScreen(navController: NavHostController) {
    // Theme Colors
    val bgDark = Color(0xFF121212)
    val cardDark = Color(0xFF1E1E1E)
    val dividerColor = Color(0xFF333333)
    val logoBg = Color(0xFF2A2A2A)
    val gold = Color(0xFFFFD700)
    val textGray = Color(0xFFA0A0A0)
    val neonGreen = Color(0xFF00FF00)
    val red = Color(0xFFFF3B30)

    val filters = listOf("All", "Top Gainers", "Top Losers", "US Tech", "IDX Bluechips", "Dividends")
    var activeFilter by remember { mutableStateOf("All") }

    val stockData = remember { generateStockData() }

    Scaffold(
        containerColor = bgDark,
        topBar = {
            Surface(color = bgDark, modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(16.dp).statusBarsPadding()
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.clickable { navController.navigateUp() }
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Global Stock Market", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                }
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            // Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filters.forEach { filter ->
                    val isSelected = activeFilter == filter
                    Surface(
                        color = if (isSelected) bgDark else cardDark,
                        shape = RoundedCornerShape(20.dp),
                        border = if (isSelected) BorderStroke(1.dp, gold) else null,
                        modifier = Modifier.clickable { activeFilter = filter }
                    ) {
                        Text(
                            text = filter,
                            color = if (isSelected) gold else textGray,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 14.sp
                        )
                    }
                }
            }
            
            // Stock List
            val filteredStocks = stockData.filter { 
                when(activeFilter) {
                    "Top Gainers" -> it.changeAbsolute > 0
                    "Top Losers" -> it.changeAbsolute < 0
                    "US Tech" -> it.sector == "Technology" && !it.ticker.contains(".JK")
                    "IDX Bluechips" -> it.ticker.contains(".JK")
                    "Dividends" -> it.sector == "Finance" // proxy for dividend stocks
                    else -> true
                }
            }

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(filteredStocks) { stock ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Logo Placeholder
                        Surface(
                            shape = CircleShape,
                            color = logoBg,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = stock.name.take(1).uppercase(Locale.ROOT),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }
                        }
                        
                        // Middle Info
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 12.dp)
                        ) {
                            Text(
                                text = stock.name,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                maxLines = 1
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = stock.ticker,
                                    color = textGray,
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stock.sector,
                                    color = textGray.copy(alpha = 0.7f),
                                    fontSize = 12.sp
                                )
                            }
                        }
                        
                        // Right Price Info
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "$ ${String.format(Locale.US, "%.2f", stock.currentPrice)}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            val isPositive = stock.changeAbsolute >= 0
                            val changeColor = if (isPositive) neonGreen else red
                            val sign = if (isPositive) "+" else "-"
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isPositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                    contentDescription = null,
                                    tint = changeColor,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "$sign $${String.format(Locale.US, "%.2f", Math.abs(stock.changeAbsolute))} (${String.format(Locale.US, "%.2f", Math.abs(stock.changePercentage))}%)",
                                    color = changeColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    HorizontalDivider(color = dividerColor)
                }
            }
        }
    }
}
