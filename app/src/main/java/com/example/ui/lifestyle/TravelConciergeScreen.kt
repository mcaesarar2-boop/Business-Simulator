package com.example.ui.lifestyle

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.viewmodel.GameViewModel
import kotlin.math.ceil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TravelConciergeScreen(
    navController: NavHostController,
    viewModel: GameViewModel
) {
    val playerState by viewModel.playerState.collectAsState()
    
    val darkBg = Color(0xFF0A0E14)
    val luxuryGold = Color(0xFFFFD700)
    val neonBlue = Color(0xFF00E5FF)
    
    var showBookingDestination by remember { mutableStateOf<com.example.data.TravelDestination?>(null) }
    var showAddCustomDialog by remember { mutableStateOf(false) }
    
    // Status message for successful booking
    var toastMessage by remember { mutableStateOf<String?>(null) }
    var toastSuccess by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = luxuryGold,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Private Travel Concierge",
                            color = Color.White,
                            fontSize = 18.sp,
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 80.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Hero Premium Card (Header)
                item(span = { GridItemSpan(2) }) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .border(2.dp, luxuryGold.copy(alpha = 0.4f), RoundedCornerShape(24.dp)),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF141923))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        ) {
                            Text(
                                text = "👑 KAS PRIBADI CEO",
                                color = luxuryGold,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "$" + String.format("%,d", playerState.privateBalance),
                                color = Color.White,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Layanan eksklusif Family Office untuk merancang perjalanan super-lux, charter jet pribadi, dan petualangan VVIP tanpa kompromi.",
                                color = Color(0xFF90A4AE),
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = Color(0xFF232B36))
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Statistik Perjalanan",
                                        color = Color(0xFF90A4AE),
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = "${playerState.totalTripsTaken} Perjalanan Mewah",
                                        color = neonBlue,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Button(
                                    onClick = { showAddCustomDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                                    border = BorderStroke(1.dp, neonBlue.copy(alpha = 0.5f)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = neonBlue, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Kustom Destinasi", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // 2. Section Header: "Pilih Destinasi Elite"
                item(span = { GridItemSpan(2) }) {
                    Text(
                        text = "KATALOG DESTINASI ELITE",
                        color = luxuryGold,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                // 3. Grid of Destinations
                items(playerState.travelDestinations) { dest ->
                    DestinationCard(
                        destination = dest,
                        onClick = { showBookingDestination = dest }
                    )
                }

                // 4. "Rencanakan Destinasi Kustom" Grid Card at the very end
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(230.dp)
                            .border(1.dp, neonBlue.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                            .clickable { showAddCustomDialog = true },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF101622)),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFF1A2333)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = neonBlue,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Destinasi Kustom",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Rancang petualangan baru",
                                color = Color(0xFF90A4AE),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // Elegant Notification Banner (Toast) at the bottom
            if (toastMessage != null) {
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = if (toastSuccess) Color(0xFF00FF00) else Color(0xFFEF5350),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF141923)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = if (toastSuccess) Color(0xFF00FF00) else Color(0xFFEF5350),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = toastMessage!!,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(
                            onClick = { toastMessage = null },
                            colors = ButtonDefaults.textButtonColors(contentColor = Color.LightGray)
                        ) {
                            Text("OK")
                        }
                    }
                }
            }
        }
    }

    // Modal Booking Destination Dialog
    if (showBookingDestination != null) {
        BookingDialog(
            destination = showBookingDestination!!,
            viewModel = viewModel,
            privateBalance = playerState.privateBalance,
            onDismiss = { showBookingDestination = null },
            onBookingComplete = { success, msg ->
                toastMessage = msg
                toastSuccess = success
                showBookingDestination = null
            }
        )
    }

    // Modal Add Custom Destination Dialog
    if (showAddCustomDialog) {
        AddCustomDestinationDialog(
            viewModel = viewModel,
            onDismiss = { showAddCustomDialog = false },
            onComplete = { name ->
                toastMessage = "Berhasil membuat destinasi kustom baru: $name!"
                toastSuccess = true
                showAddCustomDialog = false
            }
        )
    }
}

@Composable
fun DestinationCard(
    destination: com.example.data.TravelDestination,
    onClick: () -> Unit
) {
    val luxuryGold = Color(0xFFFFD700)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(230.dp)
            .border(1.dp, Color(0xFF232B36), RoundedCornerShape(20.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141923)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Image
            if (destination.imageUrl.isNotEmpty()) {
                AsyncImage(
                    model = destination.imageUrl,
                    contentDescription = destination.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            // Dark Gradient Overlay for text legibility
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.9f)
                            ),
                            startY = 100f
                        )
                    )
            )

            // Badges & Details
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top row with Custom indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (destination.isCustom) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF00E5FF).copy(alpha = 0.2f))
                                .border(1.dp, Color(0xFF00E5FF), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("Kustom", color = Color(0xFF00E5FF), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Bottom Content
                Column {
                    Text(
                        text = destination.region.uppercase(),
                        color = luxuryGold,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = destination.name,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$${String.format("%,d", destination.pricePerDay)} / Hari",
                        color = Color(0xFF00FF00),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingDialog(
    destination: com.example.data.TravelDestination,
    viewModel: GameViewModel,
    privateBalance: Long,
    onDismiss: () -> Unit,
    onBookingComplete: (Boolean, String) -> Unit
) {
    var daysText by remember { mutableStateOf("7") }
    var includePrivateJet by remember { mutableStateOf(false) }
    var includeVipSecurity by remember { mutableStateOf(false) }

    val days = daysText.toLongOrNull() ?: 1L
    val baseCost = days * destination.pricePerDay
    val jetCost = if (includePrivateJet) 50000L else 0L
    val securityCost = if (includeVipSecurity) 10000L else 0L
    val totalCost = baseCost + jetCost + securityCost

    // Dynamic "Days to In-Game Months" Conversion info
    val inGameMonthsCost = maxOf(1, ceil(days / 30.0).toInt())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Konfirmasi Booking VVIP",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        containerColor = Color(0xFF141923),
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Destination details
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1D2433)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (destination.imageUrl.isNotEmpty()) {
                            AsyncImage(
                                model = destination.imageUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF101622)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("✈️", fontSize = 24.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(destination.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(destination.region, color = Color.Gray, fontSize = 12.sp)
                            Text("$${String.format("%,d", destination.pricePerDay)} / Hari", color = Color(0xFF00FF00), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Input Days (Free text input as requested)
                OutlinedTextField(
                    value = daysText,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() }) {
                            daysText = input
                        }
                    },
                    label = { Text("Durasi Liburan (Hari)", color = Color.Gray) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFFFD700),
                        unfocusedBorderColor = Color(0xFF232B36),
                        focusedLabelColor = Color(0xFFFFD700),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Visual Indicator for in-game month conversion
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1E2616))
                        .border(1.dp, Color(0xFF81C784).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "🗓️ ESTIMASI WAKTU IN-GAME",
                            color = Color(0xFF81C784),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Durasi Perjalanan Setara: $inGameMonthsCost Bulan In-Game",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // VIP Add-ons Checkboxes
                Text("Layanan Tambahan VVIP", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { includePrivateJet = !includePrivateJet },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = includePrivateJet,
                        onCheckedChange = { includePrivateJet = it },
                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFFFFD700))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Charter Private Jet", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("Terbang langsung dari hanggar kustom CEO (+$50,000)", color = Color.Gray, fontSize = 11.sp)
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { includeVipSecurity = !includeVipSecurity },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = includeVipSecurity,
                        onCheckedChange = { includeVipSecurity = it },
                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFFFFD700))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Layanan Paspampres & Visa VIP", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("Pengamanan bersenjata & clearance jalur hijau kustom (+$10,000)", color = Color.Gray, fontSize = 11.sp)
                    }
                }

                HorizontalDivider(color = Color(0xFF232B36))

                // Billing details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Biaya Dasar ($days hari)", color = Color.Gray, fontSize = 13.sp)
                    Text("$${String.format("%,d", baseCost)}", color = Color.White, fontSize = 13.sp)
                }

                if (includePrivateJet) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Charter Private Jet", color = Color.Gray, fontSize = 13.sp)
                        Text("+$50,000", color = Color(0xFFFFD700), fontSize = 13.sp)
                    }
                }

                if (includeVipSecurity) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Layanan Paspampres & Visa VIP", color = Color.Gray, fontSize = 13.sp)
                        Text("+$10,000", color = Color(0xFFFFD700), fontSize = 13.sp)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("TOTAL BIAYA", color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
                    Text("$${String.format("%,d", totalCost)}", color = Color(0xFF00FF00), fontWeight = FontWeight.Black, fontSize = 16.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (days <= 0) return@Button
                    val detailsList = mutableListOf<String>()
                    if (includePrivateJet) detailsList.add("Private Jet")
                    if (includeVipSecurity) detailsList.add("Paspampres")
                    val extraStr = if (detailsList.isNotEmpty()) detailsList.joinToString(", ") else "Standard Luxe"
                    
                    val success = viewModel.bookPrivateTravel(
                        destinationId = destination.id,
                        days = days.toInt(),
                        totalCost = totalCost,
                        extraDetails = extraStr
                    )
                    
                    if (success) {
                        onBookingComplete(true, "Berhasil memesan perjalanan VVIP ke ${destination.name} selama $days hari!")
                    } else {
                        onBookingComplete(false, "Kas Pribadi Anda tidak mencukupi untuk perjalanan VVIP senilai $${String.format("%,d", totalCost)} ini!")
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FF00), contentColor = Color.Black),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Bayar & Terbang ✈️", fontWeight = FontWeight.Black)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Batal", color = Color.Gray)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCustomDestinationDialog(
    viewModel: GameViewModel,
    onDismiss: () -> Unit,
    onComplete: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var region by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rencanakan Destinasi Kustom", color = Color.White, fontWeight = FontWeight.Bold) },
        containerColor = Color(0xFF141923),
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
                    label = { Text("Nama Destinasi", color = Color.Gray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFFFD700),
                        unfocusedBorderColor = Color(0xFF232B36),
                        focusedLabelColor = Color(0xFFFFD700),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = region,
                    onValueChange = { region = it },
                    label = { Text("Region / Negara", color = Color.Gray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFFFD700),
                        unfocusedBorderColor = Color(0xFF232B36),
                        focusedLabelColor = Color(0xFFFFD700),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = priceText,
                    onValueChange = { input -> if (input.all { it.isDigit() }) priceText = input },
                    label = { Text("Biaya dasar / Hari ($)", color = Color.Gray) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFFFD700),
                        unfocusedBorderColor = Color(0xFF232B36),
                        focusedLabelColor = Color(0xFFFFD700),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    label = { Text("URL Gambar Latar Belakang (Opsional)", color = Color.Gray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFFFD700),
                        unfocusedBorderColor = Color(0xFF232B36),
                        focusedLabelColor = Color(0xFFFFD700),
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
                    if (name.isBlank() || region.isBlank() || priceVal == null) {
                        errorMsg = "Semua field wajib diisi dengan benar!"
                    } else {
                        val finalImg = if (imageUrl.isBlank()) {
                            "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=500&q=80"
                        } else {
                            imageUrl
                        }
                        viewModel.addCustomTravelDestination(
                            name = name,
                            region = region,
                            pricePerDay = priceVal,
                            imageUrl = finalImg
                        )
                        onComplete(name)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FF00), contentColor = Color.Black)
            ) {
                Text("Simpan Destinasi", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = Color.LightGray)
            }
        }
    )
}
