package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.viewmodel.GameViewModel
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.pow

// --- 🎨 DEKLARASI WARNA TEMA KHUSUS (Neon / Streamer Vibes) ---
private val bgDark = Color(0xFF0F0F13)
private val cardDark = Color(0xFF1A1A24)
private val neonPurple = Color(0xFFB933FF)
private val neonBlue = Color(0xFF33D1FF)
private val gold = Color(0xFFFFD700)
private val neonGreen = Color(0xFF00FF55)
private val textGray = Color(0xFFA0A0B0)

// --- 🎨 TAMPILAN (UI) CONTENT CREATOR ---
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
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Bisnis belum dimiliki atau telah dihapus.", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { navController.popBackStack() },
                        colors = ButtonDefaults.buttonColors(containerColor = neonBlue)
                    ) {
                        Text("Kembali ke Menu Utama", color = Color.Black)
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

    // Format tampilan angka biar rapi (Pakai koma)
    val currFormat = remember { NumberFormat.getCurrencyInstance(Locale.US).apply { maximumFractionDigits = 0 } }
    val subsFormat = remember { NumberFormat.getNumberInstance(Locale.US) }

    // Dialog state
    var showSuntikDialog by remember { mutableStateOf(false) }
    var showTarikDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var amountInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Phase Name
    val phaseName = when (level) {
        in 1..20 -> "Indie Creator"
        in 21..40 -> "Small-Medium Office"
        in 41..60 -> "Medium-Large Office"
        in 61..80 -> "PT & Brand Empire"
        else -> "YouTube Conglomerate"
    }

    // Formulas
    val levelCost = (500.0 * 1.18.pow(level - 1)).toLong()
    val empCost = (1500.0 * 1.2.pow(employees)).toLong()
    val baseIncome = (subscribers * 0.05).toLong()
    val multiplier = 1.0 + (employees * 0.05)
    val estimatedIncome = (baseIncome * multiplier).toLong()

    val totalUpgradeCost = (1 until level).fold(0.0) { acc, i -> acc + (500.0 * 1.18.pow(i - 1)) }.toLong()
    val valuation = 500L + totalUpgradeCost + (estimatedIncome * 12)

    val maxEmp = when {
        level >= 81 -> 100
        level >= 61 -> 50
        level >= 41 -> 20
        level >= 21 -> 5
        else -> 0
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Studio Content Creator", color = Color.White, fontWeight = FontWeight.Bold) },
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(bgDark)
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                // JUDUL DAN HEADER LAYAR
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Dashboard Channel",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "Fase: $phaseName",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = neonBlue
                        )
                    }
                    
                    // Valuasi Usaha
                    Card(colors = CardDefaults.cardColors(containerColor = cardDark)) {
                        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), horizontalAlignment = Alignment.End) {
                            Text("Valuasi Usaha", fontSize = 11.sp, color = textGray)
                            Text(currFormat.format(valuation), fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = neonGreen)
                        }
                    }
                }

                // KARTU INFORMASI SALDO KAS
                Card(
                    colors = CardDefaults.cardColors(containerColor = cardDark),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = "Uang", tint = gold, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Kas Usaha (Content Creator)", color = textGray, fontSize = 13.sp)
                                Text(currFormat.format(contentCreatorCash), color = gold, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Row berisi Tombol Suntik & Tarik Dana
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { 
                                    amountInput = ""
                                    errorMessage = null
                                    showSuntikDialog = true 
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = neonGreen),
                                modifier = Modifier.weight(1f).height(44.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Suntik", tint = Color.Black, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Suntik Dana", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Button(
                                onClick = { 
                                    amountInput = ""
                                    errorMessage = null
                                    showTarikDialog = true 
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = neonBlue),
                                modifier = Modifier.weight(1f).height(44.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.ArrowForward, contentDescription = "Tarik", tint = Color.Black, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Tarik Keuntungan", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            item {
                // KARTU INFORMASI STATISTIK CHANNEL
                Card(
                    colors = CardDefaults.cardColors(containerColor = cardDark),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("📊 Statistik Utama", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("LEVEL $level/100", color = neonPurple, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("${subsFormat.format(subscribers)} Subscribers", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // PROGRESS BAR SIKLUS PENDAPATAN GLOBALLY UPDATING
                        Text("Siklus Pendapatan AdSense (120 Detik)", color = textGray, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { cycleProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp)),
                            color = neonBlue,
                            trackColor = Color.DarkGray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Estimasi per putaran: + ${currFormat.format(estimatedIncome)}", color = neonGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }

            // INFO BRAND DEALS JIKA LEVEL >= 61
            if (level >= 61) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF330044)),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "🔥 BONUS BRAND DEALS AKTIF!",
                                color = neonPurple,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Setiap putaran AdSense selesai, terdapat peluang 10% mendapatkan pendanaan brand tambahan secara acak.",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            item {
                // BAGIAN TOMBOL-TOMBOL AKSI
                Text("🚀 Upgrade & Ekspansi", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
                
                // 1. TOMBOL NAIK LEVEL
                val canLevelUp = contentCreatorCash >= levelCost && level < 100
                
                if (level == 40 && !isOfficeUnlocked) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF332200)),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("⚠️ Batas Fase Telah Tercapai", color = gold, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Anda tidak bisa naik level lagi. Beli Kantor (Medium-Large Office) untuk melanjutkan perkembangan karir Anda.", color = Color.White, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { gameViewModel.unlockOfficeContentCreator() },
                                enabled = contentCreatorCash >= 5_000_000L,
                                colors = ButtonDefaults.buttonColors(containerColor = neonPurple),
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            ) {
                                Text("Beli Kantor Mewah (- $5,000,000)", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    Button(
                        onClick = { gameViewModel.levelUpContentCreator() },
                        enabled = canLevelUp,
                        modifier = Modifier.fillMaxWidth().height(54.dp).padding(bottom = 12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = neonBlue, disabledContainerColor = Color.DarkGray),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Tingkatkan Level Channel", color = Color.Black, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                            Text("- ${currFormat.format(levelCost)}", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }

                // 2. TOMBOL REKRUT KARYAWAN (Tersedia mulai Fase 2 / Level 21)
                if (level >= 21) {
                    val canHire = contentCreatorCash >= empCost && employees < maxEmp
                    Card(
                        colors = CardDefaults.cardColors(containerColor = cardDark),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("👥 Tim Produksi (Bonus Income +5% per pegawai)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Staf Aktif: $employees / $maxEmp Orang", color = textGray, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { gameViewModel.hireEmployeeContentCreator() },
                                enabled = canHire,
                                modifier = Modifier.fillMaxWidth().height(44.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = neonGreen, disabledContainerColor = Color.DarkGray)
                            ) {
                                Text("Rekrut Tim Baru (- ${currFormat.format(empCost)})", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            // FITUR HAPUS / TUTUP CHANNEL DESTRUKTIF
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { showDeleteDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF3B30)),
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Tutup Channel & Hapus Bisnis", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }

    // --- DIALOG DIALOG INTERAKTIF ---

    // 1. DIALOG SUNTIK DANA
    if (showSuntikDialog) {
        AlertDialog(
            onDismissRequest = { showSuntikDialog = false },
            icon = { Icon(Icons.Default.Add, contentDescription = "Suntik", tint = neonGreen) },
            title = { Text("Suntik Dana", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Pindahkan saldo dari Kas Holdings / CEO Anda ke Kas Usaha Content Creator.", color = Color.LightGray, fontSize = 13.sp)
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
                        Text(errorMessage!!, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
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
                    Text("Suntik", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSuntikDialog = false }) {
                    Text("Batal", color = Color.LightGray)
                }
            }
        )
    }

    // 2. DIALOG TARIK KEUNTUNGAN
    if (showTarikDialog) {
        AlertDialog(
            onDismissRequest = { showTarikDialog = false },
            icon = { Icon(Icons.Default.ArrowForward, contentDescription = "Tarik", tint = neonBlue) },
            title = { Text("Tarik Keuntungan", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Tarik saldo dari Kas Usaha Content Creator ke Kas Holdings / CEO Anda.", color = Color.LightGray, fontSize = 13.sp)
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
                        Text(errorMessage!!, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
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
                                errorMessage = "Gagal melakukan penarikan keuntungan."
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = neonBlue)
                ) {
                    Text("Tarik", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTarikDialog = false }) {
                    Text("Batal", color = Color.LightGray)
                }
            }
        )
    }

    // 3. DIALOG DESTRUKTIF HAPUS BISNIS
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = "Peringatan", tint = Color(0xFFFF3B30), modifier = Modifier.size(40.dp)) },
            title = { Text("Peringatan Berbahaya!", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text("Apakah Anda yakin ingin menghapus channel ini? Seluruh aset, subscribers, dan Kas Usaha yang belum ditarik akan HANGUS. Tindakan ini tidak dapat dibatalkan.", color = Color.LightGray) },
            containerColor = cardDark,
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        gameViewModel.deleteContentCreatorBusiness()
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF3B30))
                ) {
                    Text("Ya, Hapus Permanen", color = Color.White, fontWeight = FontWeight.Bold)
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
