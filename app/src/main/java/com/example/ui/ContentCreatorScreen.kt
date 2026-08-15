package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.data.ActiveCreatorContract
import com.example.data.BrandDealGenerator
import com.example.data.BrandDealOffer
import com.example.data.BrandDealType
import com.example.viewmodel.GameViewModel
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.pow
import kotlin.random.Random

// --- 🎨 DEKLARASI WARNA TEMA KHUSUS (Neon / Streamer Cyber Vibes) ---
private val bgDark = Color(0xFF0C0D14)
private val cardDark = Color(0xFF151824)
private val cardDarkElevated = Color(0xFF1D2132)
private val neonPurple = Color(0xFFC040FD)
private val neonBlue = Color(0xFF29D2FE)
private val gold = Color(0xFFFFC727)
private val neonGreen = Color(0xFF00FF7F)
private val textGray = Color(0xFF9EA3B5)
private val textMuted = Color(0xFF6B7280)
private val crimson = Color(0xFFFF3B5C)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentCreatorScreen(
    navController: NavController,
    gameViewModel: GameViewModel
) {
    val playerState by gameViewModel.playerState.collectAsState()
    val business = playerState.ownedBusinesses.find { it.catalogId == "content_creator" }

    if (business == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Studio Content Creator", color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = bgDark)
                )
            },
            containerColor = bgDark
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                    Icon(Icons.Default.VideoCameraBack, contentDescription = null, tint = textGray, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Bisnis Belum Dimiliki", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Anda belum mendirikan channel YouTube / Content Creator.", color = textGray, fontSize = 13.sp, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { navController.popBackStack() },
                        colors = ButtonDefaults.buttonColors(containerColor = neonBlue),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Kembali ke Menu Utama", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        return
    }

    val level = business.level
    val subscribers = business.contentCreatorSubscribers
    val employees = business.contentCreatorEmployees
    val isOfficeUnlocked = business.contentCreatorOfficeUnlocked
    val contentCreatorCash = business.contentCreatorCash
    val cycleProgress = business.contentCreatorProgress
    val activeContracts = business.contentCreatorContracts

    // Format mata uang & subscribers
    val currFormat = remember { NumberFormat.getCurrencyInstance(Locale.US).apply { maximumFractionDigits = 0 } }
    val subsFormat = remember { NumberFormat.getNumberInstance(Locale.US) }

    // Dialog state
    var showSuntikDialog by remember { mutableStateOf(false) }
    var showTarikDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var contractToTerminate by remember { mutableStateOf<ActiveCreatorContract?>(null) }
    var amountInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // ==========================================
    // BRAND DEALS (SPONSORSHIP) ENGINE
    // ==========================================
    var activeBrandDeal by remember { mutableStateOf<BrandDealOffer?>(null) }
    var dealTimeLeft by remember { mutableStateOf(15) }
    var dealFeedbackMsg by remember { mutableStateOf<String?>(null) }
    var dealFeedbackIsPositive by remember { mutableStateOf(true) }
    var pitchCooldown by remember { mutableStateOf(0) }

    // Periodic auto-scout for sponsorships
    LaunchedEffect(subscribers, activeContracts.size) {
        while (true) {
            delay(20_000L) // Cek peluang setiap 20 detik
            if (activeBrandDeal == null) {
                val newDeal = BrandDealGenerator.generateBrandDeal(
                    subscribers = subscribers,
                    activeContractsCount = activeContracts.size,
                    forceSpawn = false
                )
                if (newDeal != null) {
                    activeBrandDeal = newDeal
                    dealTimeLeft = newDeal.durationSeconds
                }
            }
        }
    }

    // Cooldown countdown for manual pitch
    LaunchedEffect(pitchCooldown) {
        if (pitchCooldown > 0) {
            delay(1000L)
            pitchCooldown -= 1
        }
    }

    // 15s Countdown timer for active brand deal
    LaunchedEffect(activeBrandDeal) {
        if (activeBrandDeal != null) {
            dealTimeLeft = 15
            while (dealTimeLeft > 0 && activeBrandDeal != null) {
                delay(1000L)
                dealTimeLeft -= 1
            }
            if (dealTimeLeft <= 0 && activeBrandDeal != null) {
                val expiredBrand = activeBrandDeal?.brandName ?: "Sponsor"
                activeBrandDeal = null
                dealFeedbackMsg = "⏳ Penawaran dari $expiredBrand telah kedaluwarsa."
                dealFeedbackIsPositive = false
            }
        }
    }

    // Auto-clear feedback notification after 5s
    LaunchedEffect(dealFeedbackMsg) {
        if (dealFeedbackMsg != null) {
            delay(5000L)
            dealFeedbackMsg = null
        }
    }

    // Phase Name
    val phaseName = when (level) {
        in 1..20 -> "Indie Creator"
        in 21..40 -> "Small-Medium Studio"
        in 41..60 -> "Medium-Large Production"
        in 61..80 -> "PT Media & Brand Empire"
        else -> "Global Creator Conglomerate"
    }

    // Formulas
    val levelCost = (500.0 * 1.18.pow(level - 1)).toLong()
    val empCost = (1500.0 * 1.2.pow(employees)).toLong()
    val baseIncome = (subscribers * 0.05).toLong()
    val multiplier = 1.0 + (employees * 0.05)
    val estimatedIncome = (baseIncome * multiplier).toLong()
    val monthlyContractTotal = activeContracts.sumOf { it.monthlyPayout }
    val totalEstimatedMonthly = estimatedIncome + monthlyContractTotal

    val totalUpgradeCost = (1 until level).fold(0.0) { acc, i -> acc + (500.0 * 1.18.pow(i - 1)) }.toLong()
    val valuation = 500L + totalUpgradeCost + (totalEstimatedMonthly * 12)

    val maxEmp = when {
        level >= 81 -> 100
        level >= 61 -> 50
        level >= 41 -> 20
        level >= 21 -> 5
        else -> 0
    }

    val tierInfo = remember(subscribers) { BrandDealGenerator.getTierInfo(subscribers) }

    // Pulsing effect for glowing sponsor card
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(crimson.copy(alpha = 0.2f))
                                .border(1.dp, crimson, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = crimson, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Studio Content Creator", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("YouTube & Sponsorship Engine", color = textGray, fontSize = 11.sp)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                    }
                },
                actions = {
                    // Manual Pitch Action with Cooldown
                    OutlinedButton(
                        onClick = {
                            if (pitchCooldown == 0 && activeBrandDeal == null) {
                                pitchCooldown = 15
                                val rolled = BrandDealGenerator.generateBrandDeal(
                                    subscribers = subscribers,
                                    activeContractsCount = activeContracts.size,
                                    forceSpawn = true
                                )
                                if (rolled != null) {
                                    activeBrandDeal = rolled
                                    dealTimeLeft = rolled.durationSeconds
                                }
                            }
                        },
                        enabled = pitchCooldown == 0 && activeBrandDeal == null,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = gold,
                            disabledContentColor = textMuted
                        ),
                        border = BorderStroke(1.dp, if (pitchCooldown == 0 && activeBrandDeal == null) gold.copy(alpha = 0.6f) else Color.DarkGray),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .height(30.dp)
                    ) {
                        Icon(Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (pitchCooldown > 0) "Pitch (${pitchCooldown}s)" else "Pitch Sponsor",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bgDark)
            )
        },
        containerColor = bgDark
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(bgDark)
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // ==========================================
            // HEADER & VALUASI
            // ==========================================
            item {
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Dashboard Channel",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(neonGreen)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = phaseName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = neonBlue
                            )
                        }
                    }

                    // Valuasi Usaha Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = cardDark),
                        border = BorderStroke(1.dp, Color(0xFF262C42)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text("Valuasi Channel", fontSize = 10.sp, color = textGray)
                            Text(
                                text = currFormat.format(valuation),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = neonGreen,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                // ==========================================
                // KARTU SALDO KAS USAHA
                // ==========================================
                Card(
                    colors = CardDefaults.cardColors(containerColor = cardDark),
                    border = BorderStroke(1.dp, Color(0xFF262C42)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(gold.copy(alpha = 0.15f))
                                        .border(1.dp, gold.copy(alpha = 0.5f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Kas", tint = gold, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Kas Usaha Channel", color = textGray, fontSize = 11.sp)
                                    Text(
                                        text = currFormat.format(contentCreatorCash),
                                        color = gold,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Tombol Suntik & Tarik Dana
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    amountInput = ""
                                    errorMessage = null
                                    showSuntikDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = neonGreen),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Suntik", tint = Color.Black, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Suntik Modal", color = Color.Black, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                            }

                            Button(
                                onClick = {
                                    amountInput = ""
                                    errorMessage = null
                                    showTarikDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = neonBlue),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.ArrowForward, contentDescription = "Tarik", tint = Color.Black, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Tarik Profit", color = Color.Black, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // ==========================================
            // STATISTIK UTAMA (SLEEK AD SENSE + CONTRACTS PAYOUT)
            // ==========================================
            item {
                val secondsRemaining = ((1f - cycleProgress) * 120).toInt().coerceAtLeast(0)

                Card(
                    colors = CardDefaults.cardColors(containerColor = cardDark),
                    border = BorderStroke(1.dp, Color(0xFF262C42)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Analytics, contentDescription = null, tint = neonPurple, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Statistik Utama", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(neonPurple.copy(alpha = 0.15f))
                                    .border(1.dp, neonPurple.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "LEVEL $level/100",
                                    color = neonPurple,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Metrik 2 Kolom: Subscribers & Sponsor Tier
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = cardDarkElevated),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("SUBSCRIBERS", color = textGray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = subsFormat.format(subscribers),
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text("Status: ${tierInfo.name.substringBefore(":")}", color = neonBlue, fontSize = 10.sp)
                                }
                            }

                            Card(
                                colors = CardDefaults.cardColors(containerColor = cardDarkElevated),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("TIM PRODUKSI", color = textGray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = "$employees Staf",
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    val bonusPct = (employees * 5)
                                    Text("Bonus AdSense: +$bonusPct%", color = neonGreen, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // SLEEK AD SENSE PROGRESS BAR
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = neonBlue, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Siklus Payday (1 Bulan Game)", color = textGray, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            }
                            Text(
                                text = "${secondsRemaining}s (${(cycleProgress * 100).toInt()}%)",
                                color = neonBlue,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Sleek Thin Progress Bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF0F111A))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(fraction = cycleProgress.coerceIn(0f, 1f))
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(neonBlue, neonGreen)
                                        )
                                    )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Rincian Penghasilan Per Siklus (AdSense + Kontrak)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(cardDarkElevated)
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("AdSense Video:", color = textGray, fontSize = 11.sp)
                                Text("+${currFormat.format(estimatedIncome)} / bln", color = Color.White, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            }
                            if (activeContracts.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(3.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Kontrak Sponsorship (${activeContracts.size}):", color = gold, fontSize = 11.sp)
                                    Text("+${currFormat.format(monthlyContractTotal)} / bln", color = gold, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                }
                            }
                            HorizontalDivider(color = Color(0xFF2B3248), thickness = 0.8.dp, modifier = Modifier.padding(vertical = 4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Total Estimasi Payout:", color = textMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    text = "+ ${currFormat.format(totalEstimatedMonthly)} / bln",
                                    color = neonGreen,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }

            // ==========================================
            // TUGAS 3: SECTION "MANAJEMEN KONTRAK AKTIF" (SLOT 3 KONTRAK)
            // ==========================================
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = gold, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Manajemen Kontrak Aktif",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Slot Indicator Chips
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                        repeat(3) { index ->
                            val isOccupied = index < activeContracts.size
                            Box(
                                modifier = Modifier
                                    .size(width = 16.dp, height = 7.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(if (isOccupied) gold else Color(0xFF262C42))
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${activeContracts.size}/3 Slot",
                            color = if (activeContracts.size >= 3) crimson else gold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Daftar Kontrak Aktif
            if (activeContracts.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = cardDark),
                        border = BorderStroke(1.dp, Color(0xFF202638)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1B2030)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Assignment, contentDescription = null, tint = textGray, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Belum Ada Kontrak Aktif",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Terima tawaran bertipe 'Kontrak Resmi' untuk mendapatkan suntikan gaji pasif setiap siklus bulanan tiba (Maks. 3 slot).",
                                    color = textGray,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            } else {
                items(activeContracts, key = { it.id }) { contract ->
                    val tierColor = when (contract.tierLevel) {
                        0, 1 -> neonBlue
                        2, 3 -> gold
                        4, 5 -> neonPurple
                        else -> Color(0xFFFF5252)
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = cardDark),
                        border = BorderStroke(1.dp, tierColor.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            // Header Kontrak
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(26.dp)
                                            .clip(CircleShape)
                                            .background(tierColor.copy(alpha = 0.2f))
                                            .border(1.dp, tierColor, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Verified, contentDescription = null, tint = tierColor, modifier = Modifier.size(14.dp))
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = contract.brandName,
                                            color = Color.White,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                        Text(
                                            text = contract.categoryTag,
                                            color = textGray,
                                            fontSize = 10.sp
                                        )
                                    }
                                }

                                // Payout Bulanan Badge
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(gold.copy(alpha = 0.15f))
                                        .border(1.dp, gold.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "+${currFormat.format(contract.monthlyPayout)} / bln",
                                        color = gold,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Progress Durasi Kontrak
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Sisa Waktu: ${contract.remainingMonths} dari ${contract.totalMonths} Bulan",
                                    color = textGray,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Diterima: ${currFormat.format(contract.totalPaidSoFar)}",
                                    color = neonGreen,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Sleek Progress Bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Color(0xFF0F111A))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(fraction = contract.progressFraction.coerceIn(0f, 1f))
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(tierColor, gold)
                                            )
                                        )
                                    )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Footer: Total Kontrak & Tombol Putus Kontrak
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Total Kontrak: ${currFormat.format(contract.totalContractValue)}",
                                    color = textMuted,
                                    fontSize = 10.sp
                                )

                                TextButton(
                                    onClick = { contractToTerminate = contract },
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                                    modifier = Modifier.height(26.dp)
                                ) {
                                    Icon(Icons.Default.Cancel, contentDescription = null, tint = crimson.copy(alpha = 0.8f), modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("Putus Kontrak", color = crimson.copy(alpha = 0.8f), fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }

            // ==========================================
            // NOTIFIKASI TOAST / FEEDBACK BANNER
            // ==========================================
            if (dealFeedbackMsg != null) {
                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    AnimatedVisibility(
                        visible = dealFeedbackMsg != null,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (dealFeedbackIsPositive) Color(0xFF0D281E) else Color(0xFF281118)
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (dealFeedbackIsPositive) neonGreen.copy(alpha = 0.6f) else crimson.copy(alpha = 0.6f)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 14.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (dealFeedbackIsPositive) Icons.Default.CheckCircle else Icons.Default.Info,
                                    contentDescription = null,
                                    tint = if (dealFeedbackIsPositive) neonGreen else crimson,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = dealFeedbackMsg ?: "",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // ==========================================
            // TUGAS 2: SECTION "PETI MASUK PENAWARAN (SPONSORSHIP)"
            // ==========================================
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Mail, contentDescription = null, tint = neonBlue, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Peti Masuk Penawaran (Sponsorship)",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (activeBrandDeal != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(crimson.copy(alpha = 0.2f))
                                .border(1.dp, crimson, RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "🔥 1 TAWARAN MASUK",
                                color = crimson,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }

                // DYNAMIC BRAND DEAL CARD (WITH FOMO COUNTDOWN & GLOW)
                AnimatedContent(
                    targetState = activeBrandDeal,
                    transitionSpec = {
                        (fadeIn(animationSpec = tween(300)) + slideInVertically(animationSpec = tween(300)))
                            .togetherWith(fadeOut(animationSpec = tween(200)) + slideOutVertically(animationSpec = tween(200)))
                    },
                    label = "brandDealAnim"
                ) { deal ->
                    if (deal != null) {
                        // KARTU TAMPIL DENGAN EFEK POP-OUT & GLOW
                        val isContract = deal.dealType == BrandDealType.CONTRACT
                        val tierColor = when (deal.tierLevel) {
                            0, 1 -> neonBlue
                            2, 3 -> gold
                            4, 5 -> neonPurple
                            else -> Color(0xFFFF5252)
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF141727)),
                            border = BorderStroke(
                                1.5.dp,
                                Brush.linearGradient(
                                    if (isContract) listOf(gold.copy(alpha = glowAlpha), neonPurple.copy(alpha = glowAlpha))
                                    else listOf(neonGreen.copy(alpha = glowAlpha), neonBlue.copy(alpha = glowAlpha))
                                )
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(8.dp, RoundedCornerShape(14.dp), ambientColor = if (isContract) gold else neonGreen, spotColor = tierColor)
                                .padding(bottom = 14.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                // Header: Type Badge, Sponsor Tag & Timer
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        // Deal Type Badge
                                        if (isContract) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(Brush.horizontalGradient(listOf(gold.copy(alpha = 0.25f), neonPurple.copy(alpha = 0.25f))))
                                                    .border(1.dp, gold, RoundedCornerShape(6.dp))
                                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = gold, modifier = Modifier.size(12.dp))
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Text("KONTRAK RESMI", color = gold, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
                                                }
                                            }
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(neonGreen.copy(alpha = 0.18f))
                                                    .border(1.dp, neonGreen, RoundedCornerShape(6.dp))
                                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.Bolt, contentDescription = null, tint = neonGreen, modifier = Modifier.size(12.dp))
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Text("SPONSOR SEKILAS", color = neonGreen, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = deal.tierName.substringBefore(":"),
                                            color = tierColor,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    // Countdown Tag (FOMO 15s)
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFF2E151B))
                                            .border(1.dp, crimson.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Timer, contentDescription = null, tint = crimson, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "${dealTimeLeft}s",
                                                color = crimson,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Live Countdown Progress Bar
                                val timeFraction = (dealTimeLeft.toFloat() / 15f).coerceIn(0f, 1f)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(Color(0xFF222638))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(fraction = timeFraction)
                                            .fillMaxHeight()
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(
                                                if (dealTimeLeft <= 4) crimson else if (isContract) gold else neonGreen
                                            )
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Brand Name & Nilai Kontrak
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = deal.brandName,
                                            color = Color.White,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                        Text(
                                            text = if (isContract) "Kontrak Eksklusif ${deal.durationMonths} Bulan • ${deal.categoryTag}"
                                            else "Sponsorship 1 Video • ${deal.categoryTag}",
                                            color = textGray,
                                            fontSize = 11.sp
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        if (isContract) {
                                            Text("Gaji Bulanan", color = gold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            Text(
                                                text = "+${currFormat.format(deal.monthlyPayout)} / bln",
                                                color = gold,
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontFamily = FontFamily.Monospace
                                            )
                                            Text("Total: ${currFormat.format(deal.contractValue)}", color = textGray, fontSize = 10.sp)
                                        } else {
                                            Text("Bayaran Instan", color = neonGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            Text(
                                                text = "+${currFormat.format(deal.contractValue)}",
                                                color = neonGreen,
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Tombol Terima Kontrak & Tolak
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // Tombol Tolak (Memberikan bonus reputasi / integritas)
                                    OutlinedButton(
                                        onClick = {
                                            val integritySubs = (50..200).random().toLong() * (deal.tierLevel + 1)
                                            gameViewModel.rejectContentCreatorBrandDeal(integritySubs)
                                            dealFeedbackMsg = "🛡️ Menolak tawaran ${deal.brandName}! Integritas konten terjaga (+${subsFormat.format(integritySubs)} Subs)."
                                            dealFeedbackIsPositive = true
                                            activeBrandDeal = null
                                        },
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.LightGray),
                                        border = BorderStroke(1.dp, Color(0xFF3B4158)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(42.dp)
                                    ) {
                                        Text("Tolak", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }

                                    // Tombol Terima Kontrak
                                    val isSlotFull = isContract && activeContracts.size >= 3
                                    Button(
                                        onClick = {
                                            if (isSlotFull) {
                                                dealFeedbackMsg = "⚠️ Slot Kontrak Penuh (3/3)! Putus salah satu kontrak aktif terlebih dahulu."
                                                dealFeedbackIsPositive = false
                                                return@Button
                                            }
                                            val success = gameViewModel.acceptContentCreatorBrandDealOffer(deal)
                                            if (success) {
                                                if (isContract) {
                                                    dealFeedbackMsg = "🎉 Kontrak ${deal.brandName} Resmi Ditandatangani! Gaji +${currFormat.format(deal.monthlyPayout)}/bln selama ${deal.durationMonths} bulan aktif."
                                                } else {
                                                    dealFeedbackMsg = "⚡ Sponsorship ${deal.brandName} Selesai! +${currFormat.format(deal.contractValue)} langsung masuk Kas Usaha."
                                                }
                                                dealFeedbackIsPositive = true
                                                activeBrandDeal = null
                                            } else {
                                                dealFeedbackMsg = "Gagal memproses penawaran."
                                                dealFeedbackIsPositive = false
                                            }
                                        },
                                        enabled = !isSlotFull,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isContract) gold else neonGreen,
                                            disabledContainerColor = Color(0xFF262C42)
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .weight(1.7f)
                                            .height(42.dp)
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isSlotFull) "Slot Penuh (3/3)"
                                            else if (isContract) "Tanda Tangan Kontrak"
                                            else "Terima Sponsor",
                                            color = if (isSlotFull) Color.Gray else Color.Black,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // KONDISI KOSONG (STANDBY / SCOUTING)
                        Card(
                            colors = CardDefaults.cardColors(containerColor = cardDark),
                            border = BorderStroke(1.dp, Color(0xFF202638)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 14.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF1B2030)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Radar,
                                        contentDescription = null,
                                        tint = neonBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Menunggu Tawaran Brand Masuk...",
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Brand Tier ${tierInfo.tier} (${tierInfo.name.substringAfter(": ")}) memantau channel Anda. Tipe tawaran acak (Sponsor Sekilas / Kontrak Resmi).",
                                        color = textGray,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ==========================================
            // UPGRADE & EKSPANSI (IMMERSIVE CREATOR GEAR & TEAM)
            // ==========================================
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "🚀 Upgrade & Ekspansi",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                val canLevelUp = contentCreatorCash >= levelCost && level < 100

                // 1. KARTU UPGRADE LEVEL / GEAR KONTEN
                if (level == 40 && !isOfficeUnlocked) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF281C10)),
                        border = BorderStroke(1.dp, gold.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 14.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = gold, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Batas Fase Tercapai (Level 40)", color = gold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Beli Studio Kantor Mewah (Medium-Large Office) untuk membuka akses rekrutmen hingga 20 staf dan lanjut ke level berikutnya.",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { gameViewModel.unlockOfficeContentCreator() },
                                enabled = contentCreatorCash >= 5_000_000L,
                                colors = ButtonDefaults.buttonColors(containerColor = neonPurple),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                            ) {
                                Text("Beli Kantor Studio (- $5,000,000)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = cardDark),
                        border = BorderStroke(1.dp, Color(0xFF262C42)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Upgrade Studio & Resolusi 4K", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Naikkan Level Channel ke ${level + 1} (+Pertumbuhan Subs Cepat)", color = textGray, fontSize = 11.sp)
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { gameViewModel.levelUpContentCreator() },
                                enabled = canLevelUp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = neonBlue, disabledContainerColor = Color(0xFF202636)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Tingkatkan Level Channel", color = if (canLevelUp) Color.Black else Color.Gray, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                                    Text("- ${currFormat.format(levelCost)}", color = if (canLevelUp) Color.Black else Color.Gray, fontWeight = FontWeight.Bold, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                                }
                            }
                        }
                    }
                }

                // 2. KARTU REKRUT TIM PRODUKSI (Tersedia mulai Level 21)
                if (level >= 21) {
                    val canHire = contentCreatorCash >= empCost && employees < maxEmp
                    val roleName = when (employees % 4) {
                        0 -> "Lead Video Editor (DaVinci Resolve Studio)"
                        1 -> "Scriptwriter & Deep Investigator"
                        2 -> "Audio & Voice Engineer"
                        else -> "Creative Director & 3D Artist"
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = cardDark),
                        border = BorderStroke(1.dp, Color(0xFF262C42)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 14.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Group, contentDescription = null, tint = neonGreen, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Tim Produksi Khusus", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                                Text("Kapasitas: $employees/$maxEmp", color = neonGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Rekrut $roleName (+5% Bonus AdSense)",
                                color = textGray,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { gameViewModel.hireEmployeeContentCreator() },
                                enabled = canHire,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(42.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = neonGreen, disabledContainerColor = Color(0xFF202636))
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Rekrut Staf Baru", color = if (canHire) Color.Black else Color.Gray, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                                    Text("- ${currFormat.format(empCost)}", color = if (canHire) Color.Black else Color.Gray, fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                                }
                            }
                        }
                    }
                }
            }

            // ==========================================
            // TOMBOL HAPUS BISNIS DIPERKECIL DI PALING BAWAH
            // ==========================================
            item {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color(0xFF1E2232), thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 36.dp),
                    contentAlignment = Alignment.Center
                ) {
                    TextButton(
                        onClick = { showDeleteDialog = true },
                        colors = ButtonDefaults.textButtonColors(contentColor = textMuted),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Hapus",
                            tint = crimson.copy(alpha = 0.6f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Tutup Channel & Likuidasi Bisnis",
                            color = crimson.copy(alpha = 0.7f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }

    // ==========================================
    // DIALOG DIALOG INTERAKTIF
    // ==========================================

    // 1. DIALOG PUTUS KONTRAK
    if (contractToTerminate != null) {
        val contract = contractToTerminate!!
        AlertDialog(
            onDismissRequest = { contractToTerminate = null },
            icon = { Icon(Icons.Default.Cancel, contentDescription = null, tint = crimson, modifier = Modifier.size(32.dp)) },
            title = { Text("Putus Kontrak Sponsorship?", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "Apakah Anda yakin ingin memutus kontrak dengan ${contract.brandName}? Sisa durasi (${contract.remainingMonths} bulan) dan potensi gaji (+${currFormat.format(contract.monthlyPayout * contract.remainingMonths)}) akan dibatalkan, namun 1 slot kontrak akan kembali kosong.",
                    color = textGray,
                    fontSize = 13.sp
                )
            },
            containerColor = cardDark,
            confirmButton = {
                Button(
                    onClick = {
                        gameViewModel.terminateContentCreatorContract(contract.id)
                        dealFeedbackMsg = "🚫 Kontrak ${contract.brandName} telah diputus. Slot kontrak kini tersedia."
                        dealFeedbackIsPositive = false
                        contractToTerminate = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = crimson)
                ) {
                    Text("Ya, Putus Kontrak", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { contractToTerminate = null }) {
                    Text("Batal", color = Color.LightGray)
                }
            }
        )
    }

    // 2. DIALOG SUNTIK DANA
    if (showSuntikDialog) {
        AlertDialog(
            onDismissRequest = { showSuntikDialog = false },
            icon = { Icon(Icons.Default.Add, contentDescription = "Suntik", tint = neonGreen) },
            title = { Text("Suntik Modal Usaha", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Pindahkan saldo dari Kas CEO / Holdings ke Kas Channel Content Creator.", color = textGray, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Kas CEO Tersedia: ${currFormat.format(playerState.cash)}", color = neonGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = amountInput,
                        onValueChange = { amountInput = it },
                        label = { Text("Nominal Suntik ($)") },
                        singleLine = true,
                        isError = errorMessage != null,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = neonGreen,
                            focusedLabelColor = neonGreen,
                            cursorColor = neonGreen,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (errorMessage != null) {
                        Text(errorMessage!!, color = crimson, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            },
            containerColor = cardDark,
            confirmButton = {
                Button(
                    onClick = {
                        val amount = amountInput.toLongOrNull()
                        if (amount == null || amount <= 0) {
                            errorMessage = "Masukkan nominal angka yang valid!"
                        } else if (amount > playerState.cash) {
                            errorMessage = "Saldo Kas CEO Anda tidak mencukupi!"
                        } else {
                            val success = gameViewModel.injectCashToContentCreator(amount)
                            if (success) {
                                showSuntikDialog = false
                            } else {
                                errorMessage = "Gagal melakukan suntik dana."
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = neonGreen)
                ) {
                    Text("Suntik Modal", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSuntikDialog = false }) {
                    Text("Batal", color = Color.LightGray)
                }
            }
        )
    }

    // 3. DIALOG TARIK PROFIT
    if (showTarikDialog) {
        AlertDialog(
            onDismissRequest = { showTarikDialog = false },
            icon = { Icon(Icons.Default.ArrowForward, contentDescription = "Tarik", tint = neonBlue) },
            title = { Text("Tarik Keuntungan Channel", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Tarik saldo dari Kas Channel ke Kas Utama CEO / Holdings.", color = textGray, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Kas Usaha Tersedia: ${currFormat.format(contentCreatorCash)}", color = neonBlue, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = amountInput,
                        onValueChange = { amountInput = it },
                        label = { Text("Nominal Tarik ($)") },
                        singleLine = true,
                        isError = errorMessage != null,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = neonBlue,
                            focusedLabelColor = neonBlue,
                            cursorColor = neonBlue,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (errorMessage != null) {
                        Text(errorMessage!!, color = crimson, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            },
            containerColor = cardDark,
            confirmButton = {
                Button(
                    onClick = {
                        val amount = amountInput.toLongOrNull()
                        if (amount == null || amount <= 0) {
                            errorMessage = "Masukkan nominal angka yang valid!"
                        } else if (amount > contentCreatorCash) {
                            errorMessage = "Saldo Kas Usaha tidak mencukupi!"
                        } else {
                            val success = gameViewModel.withdrawCashFromContentCreator(amount)
                            if (success) {
                                showTarikDialog = false
                            } else {
                                errorMessage = "Gagal melakukan penarikan profit."
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = neonBlue)
                ) {
                    Text("Tarik Profit", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTarikDialog = false }) {
                    Text("Batal", color = Color.LightGray)
                }
            }
        )
    }

    // 4. DIALOG DESTRUKTIF HAPUS BISNIS (AMAN & DIKONFIRMASI GANDA)
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = "Peringatan", tint = crimson, modifier = Modifier.size(36.dp)) },
            title = { Text("Konfirmasi Penutupan Channel", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "Apakah Anda yakin ingin menutup channel ini secara permanen? Seluruh subscribers (${subsFormat.format(subscribers)} subs), ${activeContracts.size} kontrak sponsorship, aset produksi, dan sisa saldo Kas Usaha (${currFormat.format(contentCreatorCash)}) akan dihapus.",
                    color = textGray,
                    fontSize = 13.sp
                )
            },
            containerColor = cardDark,
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        gameViewModel.deleteContentCreatorBusiness()
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = crimson)
                ) {
                    Text("Ya, Tutup Channel", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Batal", color = Color.LightGray)
                }
            }
        )
    }
}
