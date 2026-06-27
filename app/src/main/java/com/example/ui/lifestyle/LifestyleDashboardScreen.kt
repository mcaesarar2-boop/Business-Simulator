package com.example.ui.lifestyle

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LifestyleDashboardScreen(
    navController: NavHostController,
    viewModel: GameViewModel
) {
    val playerState by viewModel.playerState.collectAsState()
    
    val darkBg = Color(0xFF0F1319)
    val neonGreen = Color(0xFF00FF00)
    
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Langganan", "Tech Gadget", "Ekspedisi", "Filantropi", "Wellness & Proteksi")
    
    // Modals state
    var editingItem by remember { mutableStateOf<com.example.data.LifestyleItem?>(null) }
    var showAddModal by remember { mutableStateOf(false) }

    // Map selected tab to tabCategory key
    val currentTabCategory = when (selectedTab) {
        0 -> "langganan"
        1 -> "gadget"
        2 -> "ekspedisi"
        3 -> "filantropi"
        else -> "wellness"
    }

    // Filter items based on selected tab
    val filteredItems = playerState.allSubscriptions.filter { it.tabCategory == currentTabCategory }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Gaya Hidup & Pengeluaran Pribadi",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = darkBg)
            )
        },
        containerColor = darkBg
    ) { paddingValues ->
        // Single Layer Scroll using a single LazyColumn
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // 1. Header: Kas Pribadi (Always visible for excellent gameplay clarity)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                        .border(2.dp, neonGreen.copy(alpha = 0.35f), RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0C1D12))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "👑 KAS PRIBADI CEO",
                            color = neonGreen,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "$" + String.format("%,d", playerState.privateBalance),
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Danai gaya hidup berkelas dari dividen & gaji murni",
                            color = Color(0xFF90A4AE),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // 2. Navigation TabRow
            item {
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = neonGreen,
                    edgePadding = 20.dp,
                    divider = { HorizontalDivider(color = Color(0xFF232B36)) },
                    indicator = { tabPositions ->
                        if (selectedTab < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = neonGreen
                            )
                        }
                    },
                    modifier = Modifier.padding(vertical = 12.dp)
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    fontSize = 11.sp,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == index) neonGreen else Color(0xFF90A4AE)
                                )
                            }
                        )
                    }
                }
            }

            // 3. Info Card showing Total Cost based on Tab
            item {
                Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
                    when (selectedTab) {
                        0 -> {
                            val activeSubs = filteredItems.filter { it.isActive }
                            val totalCost = activeSubs.sumOf { it.price }
                            InfoCard(
                                title = "Total Biaya Langganan Bulanan",
                                value = "$${String.format("%,d", totalCost)} / bln",
                                subtitle = "${activeSubs.size} Langganan Aktif",
                                themeColor = Color(0xFF00FF00),
                                cardBg = Color(0xFF0B1F11)
                            )
                        }
                        1 -> {
                            val ownedGadgetsCount = filteredItems.filter { it.isOwned }.size
                            InfoCard(
                                title = "Koleksi Gadget Premium & AI",
                                value = "$ownedGadgetsCount Dimiliki",
                                subtitle = "Investasi perangkat keras eksekutif CEO",
                                themeColor = Color(0xFF2196F3),
                                cardBg = Color(0xFF0B1724)
                            )
                        }
                        2 -> {
                            InfoCard(
                                title = "Total Perjalanan Liburan",
                                value = "${playerState.travelHistory} Perjalanan",
                                subtitle = "Aktivitas Healing & Rekreasi CEO",
                                themeColor = Color(0xFFFF9800),
                                cardBg = Color(0xFF22160C)
                            )
                        }
                        3 -> {
                            InfoCard(
                                title = "Total Donasi Filantropi",
                                value = "$${String.format("%,d", playerState.totalCharityDonated)}",
                                subtitle = "Meningkatkan status sosial CEO secara global",
                                themeColor = Color(0xFFE91E63),
                                cardBg = Color(0xFF240D16)
                            )
                        }
                        4 -> {
                            val activeWellness = filteredItems.filter { it.isActive }
                            val totalCost = activeWellness.sumOf { it.price }
                            InfoCard(
                                title = "Total Biaya Wellness & Proteksi",
                                value = "$${String.format("%,d", totalCost)} / bln",
                                subtitle = "${activeWellness.size} Layanan Proteksi Aktif",
                                themeColor = Color(0xFF9C27B0),
                                cardBg = Color(0xFF221124)
                            )
                        }
                    }
                }
            }

            // Special interactive manual input card for Filantropi
            if (selectedTab == 3) {
                item {
                    Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                        CharityDonationInputCard(viewModel = viewModel, playerState = playerState)
                    }
                }
            }

            // Special premium banner for Private Travel Concierge when in Ekspedisi tab
            if (selectedTab == 2) {
                item {
                    Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { navController.navigate("private_travel_concierge") }
                                .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B160C)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF2E2413)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("👑", fontSize = 24.sp)
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Private Travel Concierge",
                                        color = Color(0xFFFFD700),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Rancang rute VVIP kustom & konversi hari perjalanan secara dinamis",
                                        color = Color(0xFFCFD8DC),
                                        fontSize = 11.sp,
                                        lineHeight = 14.sp
                                    )
                                }
                                Text("➔", color = Color(0xFFFFD700), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // 4. Catalog Title
            item {
                Text(
                    text = when (selectedTab) {
                        0 -> "Katalog Langganan Eksekutif"
                        1 -> "Katalog Gadget Premium & AI"
                        2 -> "Eksplorasi Perjalanan Eksklusif"
                        3 -> "Kampanye Donasi Kemanusiaan"
                        else -> "Wellness & Proteksi Kelas Atas"
                    },
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 4.dp)
                )
            }

            // 5. Grouped Catalog Items (Grouping by Section)
            if (filteredItems.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Belum ada item di kategori ini.", color = Color.Gray, fontSize = 13.sp)
                    }
                }
            } else {
                val grouped = filteredItems.groupBy { it.sectionName }
                grouped.forEach { (section, itemsInSection) ->
                    // Section Title Item
                    item {
                        Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 8.dp)) {
                            Text(
                                text = section.uppercase(),
                                color = neonGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            HorizontalDivider(color = Color(0xFF232B36), thickness = 1.dp)
                        }
                    }

                    // Render items as a Grid of 2 items per row
                    val chunked = itemsInSection.chunked(2)
                    items(chunked.size) { rowIndex ->
                        val rowItems = chunked[rowIndex]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            for (i in 0 until 2) {
                                if (i < rowItems.size) {
                                    val item = rowItems[i]
                                    Box(modifier = Modifier.weight(1f)) {
                                        LifestyleItemCard(
                                            item = item,
                                            viewModel = viewModel,
                                            onEditClick = { editingItem = item }
                                        )
                                    }
                                } else {
                                    Box(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }

            // 6. Large universal add custom button at the very bottom
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { showAddModal = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .height(56.dp)
                        .border(1.dp, neonGreen.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("[ + ]", color = neonGreen, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Tambah Item Kustom Baru", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Modal Overlays
    if (editingItem != null) {
        EditLifestyleItemDialog(
            item = editingItem!!,
            viewModel = viewModel,
            onDismiss = { editingItem = null }
        )
    }

    if (showAddModal) {
        AddLifestyleItemDialog(
            viewModel = viewModel,
            initialTabCategory = currentTabCategory,
            onDismiss = { showAddModal = false }
        )
    }
}

@Composable
fun InfoCard(
    title: String,
    value: String,
    subtitle: String,
    themeColor: Color,
    cardBg: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, themeColor.copy(alpha = 0.3f), RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = title,
                    color = Color(0xFF90A4AE),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value,
                    color = themeColor,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = Color(0xFF78909C),
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun CharityDonationInputCard(
    viewModel: GameViewModel,
    playerState: com.example.data.PlayerState
) {
    var donationAmountText by remember { mutableStateOf("") }
    var resultMessage by remember { mutableStateOf<String?>(null) }
    var isSuccessMessage by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF151921)),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Lakukan Donasi Kemanusiaan Bebas",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Masukkan nilai donasi tunai secara bebas untuk mendirikan yayasan sosial, membangun sekolah rakyat, atau mendanai penelitian medis darurat.",
                color = Color(0xFF90A4AE),
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = donationAmountText,
                onValueChange = { input ->
                    if (input.all { it.isDigit() }) {
                        donationAmountText = input
                    }
                },
                label = { Text("Jumlah Donasi ($)", color = Color.Gray) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF00FF00),
                    unfocusedBorderColor = Color(0xFF232B36),
                    focusedLabelColor = Color(0xFF00FF00),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val amt = donationAmountText.toLongOrNull() ?: 0L
                    if (amt <= 0L) {
                        resultMessage = "Silakan masukkan jumlah donasi yang valid!"
                        isSuccessMessage = false
                    } else {
                        val success = viewModel.donateToCharity(amt)
                        if (success) {
                            resultMessage = "Terima kasih atas kemurahan hati Anda! Donasi sebesar $${String.format("%,d", amt)} berhasil disalurkan."
                            isSuccessMessage = true
                            donationAmountText = ""
                        } else {
                            resultMessage = "Kas Pribadi Anda tidak mencukupi untuk donasi ini!"
                            isSuccessMessage = false
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Kirim Donasi", fontWeight = FontWeight.Bold)
            }

            if (resultMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = resultMessage!!,
                    color = if (isSuccessMessage) Color(0xFF00FF00) else Color(0xFFEF5350),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun LifestyleItemCard(
    item: com.example.data.LifestyleItem,
    viewModel: GameViewModel,
    onEditClick: () -> Unit
) {
    val fallbackEmoji = when (item.tabCategory) {
        "langganan" -> "📺"
        "gadget" -> "📱"
        "ekspedisi" -> "✈️"
        "wellness" -> "🩺"
        "filantropi" -> "🎗️"
        else -> "⭐️"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 230.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF151921)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                // Top row with Image/Icon & Edit Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1E293B)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (item.imgUrl.isNotEmpty()) {
                            AsyncImage(
                                model = item.imgUrl,
                                contentDescription = item.name,
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                modifier = Modifier.fillMaxSize(),
                                error = androidx.compose.ui.graphics.painter.ColorPainter(Color.Transparent),
                                fallback = androidx.compose.ui.graphics.painter.ColorPainter(Color.Transparent)
                            )
                        } else {
                            Text(fallbackEmoji, fontSize = 22.sp)
                        }
                    }

                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Text("✏️", fontSize = 16.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = item.name,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = item.desc,
                    color = Color(0xFF90A4AE),
                    fontSize = 11.sp,
                    lineHeight = 14.sp,
                    maxLines = 3
                )
            }

            Column {
                Spacer(modifier = Modifier.height(12.dp))

                // Price display
                val priceLabel = when (item.tabCategory) {
                    "langganan", "wellness" -> "$${String.format("%,d", item.price)}/bln"
                    else -> "$${String.format("%,d", item.price)}"
                }
                Text(
                    text = priceLabel,
                    color = if (item.isActive || item.isOwned) Color(0xFF90A4AE) else Color(0xFF00FF00),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Action buttons
                when (item.tabCategory) {
                    "langganan", "wellness" -> {
                        Button(
                            onClick = { viewModel.toggleLifestyleItemActive(item.id) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (item.isActive) Color(0xFFEF5350) else Color(0xFF00FF00),
                                contentColor = if (item.isActive) Color.White else Color.Black
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(36.dp)
                        ) {
                            Text(
                                text = if (item.isActive) "Nonaktifkan" else "Aktifkan",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    "gadget", "filantropi" -> {
                        if (item.isOwned) {
                            Button(
                                onClick = {},
                                enabled = false,
                                colors = ButtonDefaults.buttonColors(
                                    disabledContainerColor = Color(0xFF1E2530),
                                    disabledContentColor = Color(0xFF455A64)
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(36.dp)
                            ) {
                                Text("Dimiliki", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            var buyError by remember { mutableStateOf<String?>(null) }
                            Button(
                                onClick = {
                                    val success = viewModel.purchaseLifestyleItemOwned(item.id)
                                    if (!success) {
                                        buyError = "Saldo Kurang!"
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF00FF00),
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(36.dp)
                            ) {
                                Text(
                                    text = buyError ?: "Beli",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                    "ekspedisi" -> {
                        var tripResult by remember { mutableStateOf<String?>(null) }
                        Button(
                            onClick = {
                                val success = viewModel.goOnLifestyleExpedition(item.id)
                                tripResult = if (success) "✈️ Berangkat!" else "Saldo Kurang!"
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2196F3),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(36.dp)
                        ) {
                            Text(
                                text = tripResult ?: "Pergi Liburan",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditLifestyleItemDialog(
    item: com.example.data.LifestyleItem,
    viewModel: GameViewModel,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(item.name) }
    var priceText by remember { mutableStateOf(item.price.toString()) }
    var sectionName by remember { mutableStateOf(item.sectionName) }
    var desc by remember { mutableStateOf(item.desc) }
    var imgUrl by remember { mutableStateOf(item.imgUrl) }
    
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Item Gaya Hidup", color = Color.White, fontWeight = FontWeight.Bold) },
        containerColor = Color(0xFF1E293B),
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Item", color = Color.Gray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00FF00),
                        unfocusedBorderColor = Color(0xFF475569),
                        focusedLabelColor = Color(0xFF00FF00),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = priceText,
                    onValueChange = { input -> if (input.all { it.isDigit() }) priceText = input },
                    label = { Text("Harga / Biaya ($)", color = Color.Gray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00FF00),
                        unfocusedBorderColor = Color(0xFF475569),
                        focusedLabelColor = Color(0xFF00FF00),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = sectionName,
                    onValueChange = { sectionName = it },
                    label = { Text("Section / Kategori Grouping", color = Color.Gray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00FF00),
                        unfocusedBorderColor = Color(0xFF475569),
                        focusedLabelColor = Color(0xFF00FF00),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Deskripsi", color = Color.Gray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00FF00),
                        unfocusedBorderColor = Color(0xFF475569),
                        focusedLabelColor = Color(0xFF00FF00),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                OutlinedTextField(
                    value = imgUrl,
                    onValueChange = { imgUrl = it },
                    label = { Text("URL Logo / Gambar (Opsional)", color = Color.Gray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00FF00),
                        unfocusedBorderColor = Color(0xFF475569),
                        focusedLabelColor = Color(0xFF00FF00),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMsg != null) {
                    Text(errorMsg!!, color = Color.Red, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val priceVal = priceText.toLongOrNull()
                    if (name.isBlank() || priceVal == null || sectionName.isBlank() || desc.isBlank()) {
                        errorMsg = "Semua field harus diisi dengan benar!"
                    } else {
                        viewModel.updateLifestyleItem(
                            id = item.id,
                            name = name,
                            price = priceVal,
                            sectionName = sectionName,
                            desc = desc,
                            imgUrl = imgUrl
                        )
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FF00), contentColor = Color.Black)
            ) {
                Text("Simpan", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (item.isCustom) {
                    Button(
                        onClick = {
                            viewModel.deleteLifestyleItem(item.id)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350)),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Hapus", color = Color.White)
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Batal", color = Color.LightGray)
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLifestyleItemDialog(
    viewModel: GameViewModel,
    initialTabCategory: String,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var sectionName by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var imgUrl by remember { mutableStateOf("") }
    var tabCategory by remember { mutableStateOf(initialTabCategory) }
    
    var errorMsg by remember { mutableStateOf<String?>(null) }
    
    val categories = listOf(
        "langganan" to "Langganan",
        "gadget" to "Tech Gadget",
        "ekspedisi" to "Ekspedisi",
        "filantropi" to "Filantropi",
        "wellness" to "Wellness"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Item Gaya Hidup Baru", color = Color.White, fontWeight = FontWeight.Bold) },
        containerColor = Color(0xFF1E293B),
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Kategori Tab", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { (catKey, catLabel) ->
                        val isSelected = tabCategory == catKey
                        FilterChip(
                            selected = isSelected,
                            onClick = { tabCategory = catKey },
                            label = { Text(catLabel) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF00FF00),
                                selectedLabelColor = Color.Black,
                                labelColor = Color.LightGray
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Item", color = Color.Gray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00FF00),
                        unfocusedBorderColor = Color(0xFF475569),
                        focusedLabelColor = Color(0xFF00FF00),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = priceText,
                    onValueChange = { input -> if (input.all { it.isDigit() }) priceText = input },
                    label = { Text("Harga / Biaya ($)", color = Color.Gray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00FF00),
                        unfocusedBorderColor = Color(0xFF475569),
                        focusedLabelColor = Color(0xFF00FF00),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = sectionName,
                    onValueChange = { sectionName = it },
                    label = { Text("Section (Contoh: Entertainment, Health)", color = Color.Gray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00FF00),
                        unfocusedBorderColor = Color(0xFF475569),
                        focusedLabelColor = Color(0xFF00FF00),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Deskripsi", color = Color.Gray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00FF00),
                        unfocusedBorderColor = Color(0xFF475569),
                        focusedLabelColor = Color(0xFF00FF00),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                OutlinedTextField(
                    value = imgUrl,
                    onValueChange = { imgUrl = it },
                    label = { Text("URL Logo / Gambar (Opsional)", color = Color.Gray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00FF00),
                        unfocusedBorderColor = Color(0xFF475569),
                        focusedLabelColor = Color(0xFF00FF00),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMsg != null) {
                    Text(errorMsg!!, color = Color.Red, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val priceVal = priceText.toLongOrNull()
                    if (name.isBlank() || priceVal == null || sectionName.isBlank() || desc.isBlank()) {
                        errorMsg = "Semua field harus diisi dengan benar!"
                    } else {
                        viewModel.addLifestyleItem(
                            tabCategory = tabCategory,
                            sectionName = sectionName,
                            name = name,
                            price = priceVal,
                            imgUrl = imgUrl,
                            desc = desc
                        )
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FF00), contentColor = Color.Black)
            ) {
                Text("Simpan", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = Color.LightGray)
            }
        }
    )
}
