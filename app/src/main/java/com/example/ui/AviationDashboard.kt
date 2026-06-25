package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.data.AVIATION_AIRCRAFT_CATALOG
import com.example.data.DUMMY_AIRCRAFTS
import com.example.data.DUMMY_HUB_UPGRADES
import com.example.viewmodel.GameViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AviationDashboardScreen(navController: NavHostController, viewModel: GameViewModel, instanceId: String) {
    val playerState by viewModel.playerState.collectAsState()
    val useShortFormat by viewModel.useShortNumberFormat.collectAsState()

    val ownedData = playerState.ownedBusinesses.find { it.instanceId == instanceId }
        ?: playerState.holdingCompanies.flatMap { it.subsidiaries }.find { it.instanceId == instanceId }

    if (ownedData == null) {
        Box(
            modifier = Modifier.fillMaxSize().background(Color(0xFF0F0F14)),
            contentAlignment = Alignment.Center
        ) {
            Text("Data Maskapai tidak ditemukan.", color = Color.White, fontSize = 18.sp)
        }
        return
    }

    // Tab state: 0 = SURVEY / UTAMA, 1 = FLEET / ARMADA, 2 = HUB / BANDARA, 3 = ROUTES / RUTE
    var selectedTab by remember { mutableStateOf(0) }

    // Dialog flags
    var showBuyHubDialog by remember { mutableStateOf(false) }
    var showAddRouteDialog by remember { mutableStateOf(false) }
    var showHubUpgradesDialog by remember { mutableStateOf(false) }
    var selectedHubForUpgrades by remember { mutableStateOf<com.example.data.AviationHub?>(null) }
    var showLiquidationDialog by remember { mutableStateOf(false) }
    var showLiquidationConfirmSecondDialog by remember { mutableStateOf(false) }

    // Transfer Kas states
    var showTransferDialog by remember { mutableStateOf(false) }
    var isDeposit by remember { mutableStateOf(false) }
    var transferAmount by remember { mutableStateOf("") }
    val context = androidx.compose.ui.platform.LocalContext.current

    Scaffold(
        containerColor = Color(0xFF0F0F14),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = ownedData.customName ?: "Aviation Group",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Aviation Command Center",
                            fontSize = 12.sp,
                            color = Color(0xFFFFD700)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF161622),
                    titleContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Dashboard Balance and Main KPI Banner
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2F)),
                shape = RoundedCornerShape(0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("SALDO PERUSAHAAN", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            Text(
                                text = formatCurrencyRingkas(ownedData.companyCash.toLong(), useShortFormat),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFFFD700)
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("STATUS MASKAPAI", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            Text(
                                text = if (ownedData.airlineHubsComplex.isEmpty()) "Menunggu Hub Pertama" else "${ownedData.airlineHubsComplex.size} Hub Aktif",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (ownedData.airlineHubsComplex.isEmpty()) Color(0xFFE57373) else Color(0xFF81C784)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, Color(0xFFFF5252).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .background(Color(0xFFFF5252).copy(alpha = 0.1f))
                                .clickable {
                                    isDeposit = false
                                    showTransferDialog = true
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = "Tarik Kas",
                                tint = Color(0xFFFF5252),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Tarik Kas",
                                color = Color(0xFFFF5252),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, Color(0xFF4CAF50).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .background(Color(0xFF4CAF50).copy(alpha = 0.1f))
                                .clickable {
                                    isDeposit = true
                                    showTransferDialog = true
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Suntik Kas",
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Suntik Kas",
                                color = Color(0xFF4CAF50),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Material 3 Elegant Tab Selector
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFF161622),
                contentColor = Color(0xFFFFD700)
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Utama", fontSize = 12.sp) },
                    icon = { Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(20.dp)) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Fleet", fontSize = 12.sp) },
                    icon = { Icon(Icons.Default.Flight, contentDescription = null, modifier = Modifier.size(20.dp)) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Hubs", fontSize = 12.sp) },
                    icon = { Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(20.dp)) }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text("Routes", fontSize = 12.sp) },
                    icon = { Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(20.dp)) }
                )
            }

            // Tab contents
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                when (selectedTab) {
                    0 -> OverviewTabContent(
                        ownedData = ownedData,
                        useShortFormat = useShortFormat,
                        onRentHubClick = { navController.navigate("aviation_hub_catalog/$instanceId") },
                        onLiquidateClick = { showLiquidationDialog = true }
                    )
                    1 -> FleetTabContent(
                        ownedData = ownedData,
                        playerCash = playerState.cash,
                        useShortFormat = useShortFormat,
                        onBuyAircraftClick = { navController.navigate("aviation_catalog_screen/$instanceId") },
                        onRepairAircraft = { pl -> 
                            viewModel.updateBusiness(instanceId, cost = 2_000_000L) { biz ->
                                val upd = biz.airlineFleetComplex.map { plane ->
                                    if (plane.id == pl.id) plane.copy(condition = 100.0) else plane
                                }
                                biz.copy(airlineFleetComplex = upd)
                            }
                        },
                        onAssignHub = { plane, hubId ->
                            viewModel.assignAircraftToHubComplex(instanceId, plane.id, hubId)
                        }
                    )
                    2 -> HubsTabContent(
                        ownedData = ownedData,
                        playerCash = playerState.cash,
                        useShortFormat = useShortFormat,
                        onRentHubClick = { navController.navigate("aviation_hub_catalog/$instanceId") },
                        onOpenUpgrade = { hub ->
                            selectedHubForUpgrades = hub
                            showHubUpgradesDialog = true
                        }
                    )
                    3 -> RoutesTabContent(
                        ownedData = ownedData,
                        useShortFormat = useShortFormat,
                        onCreateRouteClick = { showAddRouteDialog = true },
                        onDeleteRoute = { routeId ->
                            viewModel.deleteFlightRouteComplex(instanceId, routeId)
                        },
                        onAssignPlane = { route, plId ->
                            viewModel.assignAircraftToRouteComplex(instanceId, plId, route.id)
                        }
                    )
                }
            }
        }
    }

    if (showLiquidationDialog) {
        AlertDialog(
            onDismissRequest = { showLiquidationDialog = false },
            containerColor = Color(0xFF1E2630),
            title = { Text("Likuidasi Maskapai?", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text("Apakah Anda yakin ingin melikuidasi unit bisnis ini? Semua armada dan hub yang beroperasi akan ditutup, dan Anda menerima nilai pemulihan sebesar 70% dari aset perusahaan.", color = Color.LightGray) },
            confirmButton = {
                Button(
                    onClick = {
                        showLiquidationDialog = false
                        showLiquidationConfirmSecondDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252))
                ) {
                    Text("Lanjut", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLiquidationDialog = false }) {
                    Text("Batal", color = Color.LightGray)
                }
            }
        )
    }

    if (showLiquidationConfirmSecondDialog) {
        AlertDialog(
            onDismissRequest = { showLiquidationConfirmSecondDialog = false },
            containerColor = Color(0xFF1E2630),
            title = { Text("Konfirmasi Akhir", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text("Peringatan: Keputusan ini tidak dapat dibatalkan! Yakinkah Anda mau melikuidasi keseluruhan divisi maskapai ini sekarang?", color = Color.LightGray) },
            confirmButton = {
                Button(
                    onClick = {
                        showLiquidationConfirmSecondDialog = false
                        viewModel.liquidateBusiness(instanceId)
                        android.widget.Toast.makeText(context, "Bisnis maskapai telah dilikuidasi!", android.widget.Toast.LENGTH_LONG).show()
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("YA, TUTUP USAHA", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLiquidationConfirmSecondDialog = false }) {
                    Text("Batal", color = Color.LightGray)
                }
            }
        )
    }

    if (showTransferDialog) {
        val titleText = if (isDeposit) "Suntik Dana Murni" else "Tarik Dana Divisi"
        val descText = if (isDeposit) {
            val holdingParent = playerState.holdingCompanies.find { it.instanceId == ownedData.parentId }
            if (holdingParent != null) {
                "Tarik uang dari kas induk (${holdingParent.name} - Saldo: ${formatCurrencyRingkas(holdingParent.holdingCash, useShortFormat)}) ke divisi maskapai ini."
            } else {
                "Tarik uang dari dompet pribadi (Saldo: ${formatCurrencyRingkas(playerState.cash, useShortFormat)}) ke divisi maskapai ini."
            }
        } else {
            val holdingParent = playerState.holdingCompanies.find { it.instanceId == ownedData.parentId }
            if (holdingParent != null) {
                "Kirim dividend dari divisi maskapai ini ke kas induk (${holdingParent.name}).\nSisa Kas Divisi: ${formatCurrencyRingkas(ownedData.companyCash.toLong(), useShortFormat)}"
            } else {
                "Kirim dividend dari divisi maskapai ini ke dompet pribadi.\nSisa Kas Divisi: ${formatCurrencyRingkas(ownedData.companyCash.toLong(), useShortFormat)}"
            }
        }

        AlertDialog(
            onDismissRequest = { showTransferDialog = false },
            title = { Text(titleText, color = Color.White) },
            text = {
                Column {
                    Text(descText, color = Color.LightGray, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = transferAmount,
                        onValueChange = { transferAmount = it },
                        label = { Text("Jumlah Uang ($)", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFFD700),
                            unfocusedBorderColor = Color.DarkGray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val amountLong = transferAmount.toLongOrNull()
                    if (amountLong != null && amountLong > 0) {
                        if (isDeposit) {
                            val holdingParent = playerState.holdingCompanies.find { it.instanceId == ownedData.parentId }
                            val maxAvailable = holdingParent?.holdingCash?.toLong() ?: playerState.cash
                            if (amountLong <= maxAvailable) {
                                val success = viewModel.injectCapitalToBusiness(instanceId, amountLong)
                                if (success) {
                                    showTransferDialog = false
                                    transferAmount = ""
                                    android.widget.Toast.makeText(context, "Suntik Dana Berhasil!", android.widget.Toast.LENGTH_SHORT).show()
                                } else {
                                    android.widget.Toast.makeText(context, "Suntik Dana Gagal. Silakan coba lagi.", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                android.widget.Toast.makeText(context, "Dana Tidak Cukup", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            if (amountLong <= ownedData.companyCash) {
                                val success = viewModel.withdrawCapitalFromBusiness(instanceId, amountLong)
                                if (success) {
                                    showTransferDialog = false
                                    transferAmount = ""
                                    android.widget.Toast.makeText(context, "Tarik Dana Berhasil!", android.widget.Toast.LENGTH_SHORT).show()
                                } else {
                                    android.widget.Toast.makeText(context, "Tarik Dana Gagal. Silakan coba lagi.", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                android.widget.Toast.makeText(context, "Kas Divisi Tidak Cukup", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        android.widget.Toast.makeText(context, "Nominal tidak valid", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text(if (isDeposit) "Suntik Dana" else "Tarik Dana", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTransferDialog = false; transferAmount = "" }) {
                    Text("Batal", color = Color.White)
                }
            },
            containerColor = Color(0xFF1E1E2F)
        )
    }

    // DIALOG: Hub Upgrades
    if (showHubUpgradesDialog && selectedHubForUpgrades != null) {
        val hub = selectedHubForUpgrades!!
        AlertDialog(
            onDismissRequest = { showHubUpgradesDialog = false },
            containerColor = Color(0xFF1E2630),
            title = { Text("Fasilitas & Upgrade Hub", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn {
                    item {
                        Text("Hub: ${hub.city}", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    items(DUMMY_HUB_UPGRADES) { upg ->
                        val isConstructing = hub.constructionQueue.any { it.upgradeId == upg.id }
                        val isOwned = hub.activeUpgrades.contains(upg.id)
                        val canAfford = playerState.cash >= upg.cost

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF252E38))
                                .clickable(enabled = !isConstructing && !isOwned && canAfford) {
                                    viewModel.startHubUpgradeComplex(instanceId, hub.id, upg.id, upg.cost, upg.buildTime)
                                    showHubUpgradesDialog = false
                                }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(upg.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Konstruksi: ${upg.buildTime} bulan", color = Color.LightGray, fontSize = 11.sp)
                                if (isOwned) {
                                    Text("STATUS: AKTIF", color = Color.Green, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                } else if (isConstructing) {
                                    val itemQ = hub.constructionQueue.find { it.upgradeId == upg.id }
                                    Text("STATUS: DIBANGUN (${itemQ?.monthsRemaining} bln)", color = Color(0xFFFFC107), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            if (!isOwned && !isConstructing) {
                                Text(
                                    text = formatCurrencyRingkas(upg.cost, useShortFormat),
                                    color = Color(0xFFFFD700),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showHubUpgradesDialog = false }) {
                    Text("Tutup", color = Color.LightGray)
                }
            }
        )
    }

    // DIALOG: Create Flight Route
    if (showAddRouteDialog) {
        val destinations = listOf(
            Pair("Singapore (SIN)", 1000),
            Pair("Kuala Lumpur (KUL)", 800),
            Pair("Bangkok (BKK)", 1500),
            Pair("Tokyo Haneda (HND)", 4000),
            Pair("Sydney Kingsford (SYD)", 3500),
            Pair("Melbourne (MEL)", 3200),
            Pair("Hong Kong (HKG)", 2500)
        )
        var selectedHubIndex by remember { mutableStateOf(0) }
        var selectedDestIndex by remember { mutableStateOf(0) }
        var inputTicketPrice by remember { mutableStateOf("250") }

        AlertDialog(
            onDismissRequest = { showAddRouteDialog = false },
            containerColor = Color(0xFF1E2630),
            title = { Text("Buka Rute Baru", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                if (ownedData.airlineHubsComplex.isEmpty()) {
                    Text("Anda belum memiliki bandara Hub sama sekali! Buka Hub pertama di tab Hub sebelum membentangkan rute penerbangan.", color = Color.LightGray)
                } else {
                    Column {
                        Text("Hub Asal:", color = Color.LightGray, fontSize = 11.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { 
                                if (selectedHubIndex > 0) selectedHubIndex-- else selectedHubIndex = ownedData.airlineHubsComplex.size - 1
                            }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
                            }
                            Text(
                                text = ownedData.airlineHubsComplex[selectedHubIndex].city,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center
                            )
                            IconButton(onClick = { 
                                if (selectedHubIndex < ownedData.airlineHubsComplex.size - 1) selectedHubIndex++ else selectedHubIndex = 0
                            }) {
                                Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.White)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text("Destinasi Tujuan:", color = Color.LightGray, fontSize = 11.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { 
                                if (selectedDestIndex > 0) selectedDestIndex-- else selectedDestIndex = destinations.size - 1
                            }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
                            }
                            val dest = destinations[selectedDestIndex]
                            Text(
                                text = "${dest.first} (${dest.second} km)",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center
                            )
                            IconButton(onClick = { 
                                if (selectedDestIndex < destinations.size - 1) selectedDestIndex++ else selectedDestIndex = 0
                            }) {
                                Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.White)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text("Harga Tiket per Pax (USD):", color = Color.LightGray, fontSize = 11.sp)
                        OutlinedTextField(
                            value = inputTicketPrice,
                            onValueChange = { inputTicketPrice = it },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFFD700),
                                unfocusedBorderColor = Color.Gray
                            )
                        )
                    }
                }
            },
            confirmButton = {
                if (ownedData.airlineHubsComplex.isNotEmpty()) {
                    Button(
                        onClick = {
                            val routeOrigin = ownedData.airlineHubsComplex[selectedHubIndex]
                            val dest = destinations[selectedDestIndex]
                            val category = if (dest.second < 1500) "SHORT" else if (dest.second < 3000) "MEDIUM" else "LONG"
                            val price = inputTicketPrice.toIntOrNull() ?: 200
                            val demand = (500..1200).random()
                            viewModel.createFlightRouteComplex(
                                businessId = instanceId,
                                originHubId = routeOrigin.id,
                                destination = dest.first,
                                distanceCategory = category,
                                demand = demand,
                                ticketPrice = price
                            )
                            showAddRouteDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007BFF))
                    ) {
                        Text("Buka Rute")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddRouteDialog = false }) {
                    Text("Batal", color = Color.LightGray)
                }
            }
        )
    }
}

@Composable
fun OverviewTabContent(
    ownedData: com.example.data.OwnedBusiness,
    useShortFormat: Boolean,
    onRentHubClick: () -> Unit,
    onLiquidateClick: () -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize().padding(top = 8.dp)
    ) {
        if (ownedData.airlineHubsComplex.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2E232F)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE57373)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFE57373), modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Menunggu Pembukaan Hub Pertama",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Untuk melayani penumpang dan memperoleh pendapatan, Anda harus membuka setidaknya satu pusat penerbangan (Hub) pertama di tab Hub.",
                            color = Color.LightGray,
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onRentHubClick,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007BFF))
                        ) {
                            Text("Buka Hub Sekarang")
                        }
                    }
                }
            }
        } else {
            item {
                Text("Dashboard Analytics & Keuangan", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2630)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Estimasi Log Pendapatan", color = Color.LightGray)
                            Text("+ ${formatCurrencyRingkas(ownedData.customRevenue?.toLong() ?: 0L, useShortFormat)} /bln", color = Color(0xFF81C784), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = onLiquidateClick,
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFFFF5252)
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF5252).copy(alpha = 0.5f)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Likuidasi Maskapai (Tutup Usaha)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun FleetTabContent(
    ownedData: com.example.data.OwnedBusiness,
    playerCash: Long,
    useShortFormat: Boolean,
    onBuyAircraftClick: () -> Unit,
    onRepairAircraft: (com.example.data.AircraftInstance) -> Unit,
    onAssignHub: (com.example.data.AircraftInstance, String?) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Armada Saya (${ownedData.airlineFleetComplex.size})", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Button(
                    onClick = onBuyAircraftClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Beli Pesawat", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (ownedData.airlineFleetComplex.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Belum ada pesawat terdaftar. Silakan pilih 'Beli Pesawat' di atas.", color = Color.Gray, fontSize = 13.sp)
                }
            }
        } else {
            items(ownedData.airlineFleetComplex) { pl ->
                val modelInfo = AVIATION_AIRCRAFT_CATALOG.find { it.id == pl.modelId } ?: DUMMY_AIRCRAFTS.find { it.id == pl.modelId }
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2630)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text(modelInfo?.model ?: pl.modelId, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                  Text("Registrasi: PK-${pl.id.take(5).uppercase()}", color = Color.Gray, fontSize = 11.sp)
                                  if (pl.isLeased) {
                                      Spacer(modifier = Modifier.width(6.dp))
                                      Box(
                                          modifier = Modifier
                                              .clip(RoundedCornerShape(4.dp))
                                              .background(Color(0xFF2E7D32))
                                              .padding(horizontal = 4.dp, vertical = 1.dp)
                                      ) {
                                          Text("SEWA", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                      }
                                  }
                                }
                            }
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = when (pl.status) {
                                        "DELIVERING" -> Color(0xFF5D4037)
                                        "ASSIGNED" -> Color(0xFF1B5E20)
                                        else -> Color(0xFF37474F)
                                    }
                                ),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = when (pl.status) {
                                        "DELIVERING" -> "INDEN (${pl.monthsUntilDelivery} BLN)"
                                        "ASSIGNED" -> "AKTIF RUTE"
                                        else -> "STANDBY"
                                    },
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Kondisi Lambung:", color = Color.Gray, fontSize = 11.sp)
                                Text("${pl.condition.toInt()}%", color = if (pl.condition < 40) Color.Red else Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Column {
                                Text("Home Hub:", color = Color.Gray, fontSize = 11.sp)
                                val deployedHub = ownedData.airlineHubsComplex.find { it.id == pl.stationedHubId }
                                Text(deployedHub?.city ?: "Belum Ditugaskan", color = Color.White, fontSize = 13.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Maintenance & Repair button and Hub Assignment
                        if (pl.status != "DELIVERING") {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { onRepairAircraft(pl) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    enabled = pl.condition < 100 && playerCash >= 2_000_000L
                                ) {
                                    Text("Perawatan ($2M)", fontSize = 11.sp)
                                }

                                if (ownedData.airlineHubsComplex.isNotEmpty()) {
                                    var showHubDropdown by remember { mutableStateOf(false) }
                                    Box(modifier = Modifier.weight(1f)) {
                                        Button(
                                            onClick = { showHubDropdown = !showHubDropdown },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F51B5)),
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Set Home Hub", fontSize = 11.sp)
                                        }

                                        DropdownMenu(
                                            expanded = showHubDropdown,
                                            onDismissRequest = { showHubDropdown = false },
                                            modifier = Modifier.background(Color(0xFF252636))
                                        ) {
                                            ownedData.airlineHubsComplex.forEach { hub ->
                                                DropdownMenuItem(
                                                    text = { Text(hub.city, color = Color.White) },
                                                    onClick = {
                                                        onAssignHub(pl, hub.id)
                                                        showHubDropdown = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF2C2515), RoundedCornerShape(8.dp))
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "🏭 Antrian Pabrik:",
                                        color = Color(0xFFFF9800),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Sisa ${pl.monthsUntilDelivery} Bulan",
                                        color = Color.LightGray,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { 
                                        val totalEstimate = when (modelInfo?.type) {
                                            "PROPELLER" -> 6f
                                            "REGIONAL_JET" -> 18f
                                            "NARROW_BODY" -> 18f
                                            "WIDE_BODY" -> 36f
                                            "HELICOPTER" -> 12f
                                            else -> 12f
                                        }
                                        val deliveredPct = ((totalEstimate - pl.monthsUntilDelivery) / totalEstimate).coerceIn(0f, 1f)
                                        deliveredPct
                                    },
                                    modifier = Modifier.fillMaxWidth().height(4.dp),
                                    color = Color(0xFFFF9800),
                                    trackColor = Color(0xFF423722)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HubsTabContent(
    ownedData: com.example.data.OwnedBusiness,
    playerCash: Long,
    useShortFormat: Boolean,
    onRentHubClick: () -> Unit,
    onOpenUpgrade: (com.example.data.AviationHub) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Pusat Bandara Hub (${ownedData.airlineHubsComplex.size})", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Button(
                    onClick = onRentHubClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Buka Hub Baru", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (ownedData.airlineHubsComplex.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Belum ada Hub terbuka. Klik 'Buka Hub Baru' untuk memulainya.", color = Color.Gray, fontSize = 13.sp)
                }
            }
        } else {
            items(ownedData.airlineHubsComplex) { hub ->
                val assignedPlanesCount = ownedData.airlineFleetComplex.count { it.stationedHubId == hub.id }
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2630)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(hub.city, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                            Text("$assignedPlanesCount Pesawat Home", color = Color.LightGray, fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (hub.activeUpgrades.isNotEmpty()) {
                            Text("Upgrades Aktif:", color = Color.Gray, fontSize = 11.sp)
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                hub.activeUpgrades.forEach { upgId ->
                                    val upgDef = DUMMY_HUB_UPGRADES.find { it.id == upgId }
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2E7D32)),
                                        shape = RoundedCornerShape(4.dp),
                                        modifier = Modifier.padding(end = 4.dp)
                                    ) {
                                        Text(
                                            text = upgDef?.name?.take(15) ?: upgId,
                                            fontSize = 9.sp,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }

                        if (hub.constructionQueue.isNotEmpty()) {
                            Text("Dalam Konstruksi:", color = Color.Gray, fontSize = 11.sp)
                            hub.constructionQueue.forEach { qi ->
                                val upgDef = DUMMY_HUB_UPGRADES.find { it.id == qi.upgradeId }
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(upgDef?.name ?: qi.upgradeId, color = Color(0xFFFFC107), fontSize = 12.sp)
                                    Text("${qi.monthsRemaining} bulan", color = Color.LightGray, fontSize = 12.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { onOpenUpgrade(hub) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F51B5)),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Mulai Konstruksi & Upgrade")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RoutesTabContent(
    ownedData: com.example.data.OwnedBusiness,
    useShortFormat: Boolean,
    onCreateRouteClick: () -> Unit,
    onDeleteRoute: (String) -> Unit,
    onAssignPlane: (com.example.data.FlightRoute, String) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Jaringan Penerbangan (${ownedData.flightRoutes.size})", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Button(
                    onClick = onCreateRouteClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Buka Rute Penerbangan", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (ownedData.flightRoutes.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Belum ada rute aktif. Buat rute untuk mulai mendatangkan profit.", color = Color.Gray, fontSize = 13.sp)
                }
            }
        } else {
            items(ownedData.flightRoutes) { r ->
                val originHub = ownedData.airlineHubsComplex.find { it.id == r.originHubId }
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2630)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(
                                text = "${originHub?.city ?: "Unknown"} ➔ ${r.destination}",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp
                            )
                            IconButton(onClick = { onDeleteRoute(r.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Hapus Rute", tint = Color.Red)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Kategori Rute:", color = Color.Gray, fontSize = 11.sp)
                                Text(r.distanceCategory, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Column {
                                Text("Market Demand:", color = Color.Gray, fontSize = 11.sp)
                                Text("${r.baseDemand} pax/hari", color = Color.White, fontSize = 13.sp)
                            }
                            Column {
                                Text("Harga Tiket:", color = Color.Gray, fontSize = 11.sp)
                                Text("$${r.ticketPrice}", color = Color(0xFFFFC107), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Assigned Aircraft Section
                        Text("Pesawat Bertugas:", color = Color.Gray, fontSize = 11.sp)

                        val activeAssignedPlanes = ownedData.airlineFleetComplex.filter { r.assignedAircraftIds.contains(it.id) }
                        if (activeAssignedPlanes.isEmpty()) {
                            Text("Belum ada pesawat ditugaskan di rute ini.", color = Color.LightGray, fontSize = 12.sp)
                        } else {
                            activeAssignedPlanes.forEach { p ->
                                val pDef = AVIATION_AIRCRAFT_CATALOG.find { it.id == p.modelId } ?: DUMMY_AIRCRAFTS.find { it.id == p.modelId }
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("PK-${p.id.take(5).uppercase()} (${pDef?.model})", color = Color.Green, fontSize = 12.sp)
                                    Text("Kondisi: ${p.condition.toInt()}%", color = Color.LightGray, fontSize = 12.sp)
                                }
                            }
                        }

                        // Assign Plane dropdown
                        val availablePlanes = ownedData.airlineFleetComplex.filter { 
                            it.status != "DELIVERING" && it.assignedRouteId == null && it.stationedHubId == r.originHubId
                        }
                        if (availablePlanes.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            var showFlyPlaneDropdown by remember { mutableStateOf(false) }
                            Box {
                                Button(
                                    onClick = { showFlyPlaneDropdown = !showFlyPlaneDropdown },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Tugaskan Pesawat Baru", fontSize = 11.sp)
                                }

                                DropdownMenu(
                                    expanded = showFlyPlaneDropdown,
                                    onDismissRequest = { showFlyPlaneDropdown = false },
                                    modifier = Modifier.background(Color(0xFF252636))
                                ) {
                                    availablePlanes.forEach { p ->
                                        val pDef = AVIATION_AIRCRAFT_CATALOG.find { it.id == p.modelId } ?: DUMMY_AIRCRAFTS.find { it.id == p.modelId }
                                        DropdownMenuItem(
                                            text = { Text("PK-${p.id.take(5).uppercase()} - ${pDef?.model}", color = Color.White) },
                                            onClick = {
                                                onAssignPlane(r, p.id)
                                                showFlyPlaneDropdown = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
