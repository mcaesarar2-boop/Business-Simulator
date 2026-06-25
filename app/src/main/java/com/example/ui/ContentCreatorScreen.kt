package com.example.ui

import android.content.Context
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.pow
import kotlin.random.Random

// --- 🎨 DEKLARASI WARNA TEMA KHUSUS (Neon / Streamer Vibes) ---
private val bgDark = Color(0xFF0F0F13)
private val cardDark = Color(0xFF1A1A24)
private val neonPurple = Color(0xFFB933FF)
private val neonBlue = Color(0xFF33D1FF)
private val gold = Color(0xFFFFD700)
private val neonGreen = Color(0xFF00FF55)
private val textGray = Color(0xFFA0A0B0)

// --- ⚙️ VIEW MODEL TERSENDIRI UNTUK LOGIKA MODULE INI ---
// Ini bertugas mengatur penyimpanan data agar tidak hilang, dan menghitung uang, level, dll.
class ContentCreatorViewModel(context: Context) : ViewModel() {
    // SharedPreferences adalah fitur Android untuk menyimpan data sederhana secara permanen
    private val prefs = context.getSharedPreferences("content_creator_prefs", Context.MODE_PRIVATE)

    var level by mutableIntStateOf(prefs.getInt("cc_level", 1))
        private set
    var subscribers by mutableLongStateOf(prefs.getLong("cc_subs", 100L))
        private set
    var employees by mutableIntStateOf(prefs.getInt("cc_employees", 0))
        private set
    var isOfficeUnlocked by mutableStateOf(prefs.getBoolean("cc_office", false))
        private set
        
    // Uang khusus untuk modul ini, berfungsi jika modul ini belum terhubung ke dompet utama game
    var internalCash by mutableLongStateOf(prefs.getLong("cc_cash", 5000L)) 
        private set

    var cycleProgress by mutableFloatStateOf(0f)
        private set
    var brandDealNotification by mutableStateOf<String?>(null)
        private set
    
    // Callback (penghubung) untuk dipanggil ketika ada penghasilan/pengeluaran jika disambung ke game utama
    var onAddGlobalCash: ((Long) -> Unit)? = null
    var onDeductGlobalCash: ((Long) -> Boolean)? = null
    var onSyncGlobalData: ((Int, Long) -> Unit)? = null

    init {
        // 🔥 GAME LOOP KHUSUS CONTENT CREATOR (120 Detik)
        // Ini berjalan terus di latar belakang selama layar terbuka
        viewModelScope.launch {
            while (true) {
                delay(1200) // Proses 1% setiap 1.2 detik
                cycleProgress += 0.01f
                if (cycleProgress >= 1f) {
                    cycleProgress = 0f
                    performIncomeCycle()
                }
            }
        }
    }

    fun triggerSync() {
        onSyncGlobalData?.invoke(level, getEstimatedIncome())
    }

    private fun save() {
        // Simpan data setiap kali ada perubahan
        prefs.edit()
            .putInt("cc_level", level)
            .putLong("cc_subs", subscribers)
            .putInt("cc_employees", employees)
            .putBoolean("cc_office", isOfficeUnlocked)
            .putLong("cc_cash", internalCash)
            .apply()
        triggerSync()
    }

    fun getPhaseName(): String = when (level) {
        in 1..20 -> "Indie Creator"
        in 21..40 -> "Small-Medium Office"
        in 41..60 -> "Medium-Large Office"
        in 61..80 -> "PT & Brand Empire"
        else -> "YouTube Conglomerate"
    }

    // 🧮 ALGORITMA EKPONENSIAL BIAYA & PENDAPATAN
    fun getNextLevelCost(): Long {
        return (500.0 * 1.18.pow(level - 1)).toLong()
    }
    
    fun getHireEmployeeCost(): Long {
        return (1500.0 * 1.2.pow(employees)).toLong()
    }

    fun getEstimatedIncome(): Long {
        val base = (subscribers * 0.05).toLong()
        val multiplier = 1.0 + (employees * 0.05) // Tiap karyawan ngasih bonus 5%
        return (base * multiplier).toLong()
    }

    // Fungsi kurangi uang
    private fun deductCash(amount: Long, externalCash: Long?): Boolean {
        if (onDeductGlobalCash != null && externalCash != null) {
            return onDeductGlobalCash!!(amount)
        }
        if (internalCash >= amount) {
            internalCash -= amount
            save()
            return true
        }
        return false
    }

    // Fungsi nambah uang
    private fun addCash(amount: Long) {
        if (onAddGlobalCash != null) {
            onAddGlobalCash!!(amount)
        } else {
            internalCash += amount
            save()
        }
    }

    // Fungsi aksi naik level
    fun levelUp(externalCash: Long?) {
        if (level >= 100) return
        if (level == 40 && !isOfficeUnlocked) return // Tertahan di level 40 jika belum beli kantor

        val cost = getNextLevelCost()
        if (deductCash(cost, externalCash)) {
            level++
            // Penambahan Subscribers eksponensial setiap naik level
            subscribers += (100.0 * 1.16.pow(level)).toLong()
            save()
        }
    }

    // Fungsi aksi Upgrade Kantor
    fun unlockMediumOffice(externalCash: Long?) {
        val cost = 5_000_000L // 5 Juta Cash
        if (level == 40 && !isOfficeUnlocked) {
            if (deductCash(cost, externalCash)) {
                isOfficeUnlocked = true
                save()
            }
        }
    }

    // Fungsi aksi Rekrut Karyawan
    fun hireEmployee(externalCash: Long?) {
        val maxEmp = when {
            level >= 81 -> 100
            level >= 61 -> 50
            level >= 41 -> 20
            level >= 21 -> 5
            else -> 0
        }
        if (employees >= maxEmp) return

        val cost = getHireEmployeeCost()
        if (deductCash(cost, externalCash)) {
            employees++
            save()
        }
    }

    // Proses penghasilan tiap siklus terpenuhi
    private fun performIncomeCycle() {
        var income = (subscribers * 0.05).toLong()
        val multiplier = 1.0 + (employees * 0.05)
        income = (income * multiplier).toLong()

        // 🌟 Mekanik Brand Deals (Fase 4 & 5 - Mulai level 61)
        if (level >= 61) {
            if (Random.nextInt(100) < 10) { // 10% Peluang dapat bonus besar
                val brandBonus = Random.nextLong(100_000, 500_000) * (level / 10)
                addCash(brandBonus) // Ekstra bonus, selalu ditambahkan
                viewModelScope.launch {
                    brandDealNotification = "🔥 BRAND DEAL! Konglomerasi masuk mendanai sebesar $${NumberFormat.getInstance().format(brandBonus)}!"
                    delay(5000) // Tampilkan notifikasi selama 5 detik
                    brandDealNotification = null
                }
            }
        }
        
        // Hindari double-income jika sudah tersambung ke game utama (karena Cash Flow bulanan game utama akan otomatis mengambil ini)
        if (onAddGlobalCash == null) {
            addCash(income)
        }
        
        // Pasif nambah subscribers terus setiap siklus
        subscribers += (level * (employees + 1) * Random.nextInt(5, 15))
        save()
    }
}

// 🏭 FACTORY: Ini kode wajib Android untuk bikin ViewModel yang butuh (Context) di parameter
class ContentCreatorViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ContentCreatorViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ContentCreatorViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


// --- 🎨 TAMPILAN (UI) CONTENT CREATOR ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentCreatorScreen(
    externalCash: Long? = null,
    onAddCash: ((Long) -> Unit)? = null,
    onDeductCash: ((Long) -> Boolean)? = null,
    onSyncData: ((Int, Long) -> Unit)? = null,
    onBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val viewModel: ContentCreatorViewModel = viewModel(
        factory = ContentCreatorViewModelFactory(context)
    )

    // Menyambungkan callback UI ke ViewModel ketika komponen muncul
    LaunchedEffect(onAddCash, onDeductCash, onSyncData) {
        viewModel.onAddGlobalCash = onAddCash
        viewModel.onDeductGlobalCash = onDeductCash
        viewModel.onSyncGlobalData = onSyncData
        viewModel.triggerSync() // Sync when screen opens
    }

    // Format tampilan angka biar rapi (Pakai koma)
    val currFormat = remember { NumberFormat.getCurrencyInstance(Locale.US).apply { maximumFractionDigits = 0 } }
    val subsFormat = remember { NumberFormat.getNumberInstance(Locale.US) }
    
    // Tampilan Saldo Cash (Gunakan eksternal jika disambungkan, atau internal dari modul ini)
    val displayCash = externalCash ?: viewModel.internalCash

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Applet", color = Color.Transparent) },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                        }
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
                .background(bgDark) // Mode gelap
                .padding(paddingValues)
                .padding(16.dp)
        ) {
        item {
            // JUDUL DAN HEADER LAYAR
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Studio Content Creator",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = "Fase: ${viewModel.getPhaseName()}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = neonBlue,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
                
                // Valuasi Usaha
                val totalUpgradeCost = (1 until viewModel.level).fold(0.0) { acc, i -> acc + (500.0 * Math.pow(1.18, i.toDouble())) }.toLong()
                val valuation = 500L + totalUpgradeCost + (viewModel.getEstimatedIncome() * 12)
                Card(colors = CardDefaults.cardColors(containerColor = cardDark)) {
                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), horizontalAlignment = Alignment.End) {
                        Text("Valuasi Usaha", fontSize = 12.sp, color = textGray)
                        Text(currFormat.format(valuation), fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = neonGreen)
                    }
                }
            }

            // KARTU INFORMASI SALDO KAS
            Card(
                colors = CardDefaults.cardColors(containerColor = cardDark),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = "Uang", tint = gold, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Kas Usaha (Content Creator)", color = textGray, fontSize = 14.sp)
                        Text(currFormat.format(displayCash), color = gold, fontSize = 22.sp, fontWeight = FontWeight.Bold)
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
                    Text("📊 Statistik Utama", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("LEVEL ${viewModel.level}/100", color = neonPurple, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("${subsFormat.format(viewModel.subscribers)} Subscribers", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // PROGRESS BAR SIKLUS PENDAPATAN
                    Text("Siklus Pendapatan AdSense (120 Detik)", color = textGray, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { viewModel.cycleProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp)),
                        color = neonBlue,
                        trackColor = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Pendapatan per putaran: + ${currFormat.format(viewModel.getEstimatedIncome())}", color = neonGreen, fontWeight = FontWeight.Bold)
                }
            }
        }

        // NOTIFIKASI BRAND DEALS MUNCUL DISINI (Hanya hitungan detik)
        if (viewModel.brandDealNotification != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF4A004A)), // Background ungu terang
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                ) {
                    Text(
                        text = viewModel.brandDealNotification!!,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }

        item {
            // BAGIAN TOMBOL-TOMBOL AKSI
            Text("🚀 Upgrade & Ekspansi", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
            
            // 1. TOMBOL NAIK LEVEL
            val levelCost = viewModel.getNextLevelCost()
            val canLevelUp = displayCash >= levelCost && viewModel.level < 100
            
            if (viewModel.level == 40 && !viewModel.isOfficeUnlocked) {
                // TUGAS KHUSUS FASE 3: Beli Kantor dulu!
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF332200)),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("⚠️ Batas Fase Telah Tercapai", color = gold, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Anda tidak bisa naik level lagi. Beli Kantor (Medium-Large Office) untuk melanjutkan perkembangan karir Anda.", color = Color.White, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.unlockMediumOffice(externalCash) },
                            enabled = displayCash >= 5_000_000L,
                            colors = ButtonDefaults.buttonColors(containerColor = neonPurple),
                            modifier = Modifier.fillMaxWidth().height(50.dp)
                        ) {
                            Text("Beli Kantor Mewah (- $5,000,000)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                Button(
                    onClick = { viewModel.levelUp(externalCash) },
                    enabled = canLevelUp,
                    modifier = Modifier.fillMaxWidth().height(60.dp).padding(bottom = 12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = neonBlue, disabledContainerColor = Color.DarkGray),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Tingkatkan Level Channel", color = Color.Black, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                        Text("- ${currFormat.format(levelCost)}", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // 2. TOMBOL REKRUT KARYAWAN (Tersedia mulai Fase 2 / Level 21)
            if (viewModel.level >= 21) {
                val empCost = viewModel.getHireEmployeeCost()
                val maxEmp = when {
                    viewModel.level >= 81 -> 100
                    viewModel.level >= 61 -> 50
                    viewModel.level >= 41 -> 20
                    viewModel.level >= 21 -> 5
                    else -> 0
                }
                
                val canHire = displayCash >= empCost && viewModel.employees < maxEmp
                Card(
                    colors = CardDefaults.cardColors(containerColor = cardDark),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("👥 Tim Produksi (Bonus Income +5% per pegawai)", color = Color.White, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Staf Aktif: ${viewModel.employees} / $maxEmp Orang", color = textGray, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.hireEmployee(externalCash) },
                            enabled = canHire,
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = neonGreen, disabledContainerColor = Color.DarkGray)
                        ) {
                            Text("Rekrut Tim Baru (- ${currFormat.format(empCost)})", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
        
        item { 
           Spacer(modifier = Modifier.height(30.dp)) 
        }
    }
}
}
