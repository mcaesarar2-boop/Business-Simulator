package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.data.*
import com.example.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoundationDetailScreen(navController: NavHostController, viewModel: GameViewModel, foundationId: String) {
    val playerState by viewModel.playerState.collectAsState()
    val foundation = playerState.foundations.find { it.id == foundationId }

    if (foundation == null) {
        Box(
            modifier = Modifier.fillMaxSize().background(Color(0xFF050C1A)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Yayasan tidak ditemukan", color = Color.White)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { navController.popBackStack() }) {
                    Text("Kembali")
                }
            }
        }
        return
    }

    var showInjectDialog by remember { mutableStateOf(false) }
    var injectAmountString by remember { mutableStateOf("") }
    var injectError by remember { mutableStateOf<String?>(null) }

    var showBuildDialog by remember { mutableStateOf(false) }
    var selectedBlueprint by remember { mutableStateOf<FacilityBlueprint?>(null) }
    var selectedTier by remember { mutableStateOf(FoundationBlueprints.tiers[0]) }
    var customFacilityName by remember { mutableStateOf("") }
    var buildError by remember { mutableStateOf<String?>(null) }

    var showCurriculumDialog by remember { mutableStateOf(false) }
    var curriculumTargetInstitution by remember { mutableStateOf<com.example.data.EducationInstitution?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(foundation.name, fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0B192C))
            )
        },
        containerColor = Color(0xFF050C1A)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Foundation Info & Legal State Card
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF101C2E)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "INFORMASI INSTITUSI",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD4AF37),
                                fontSize = 12.sp
                            )
                            if (foundation.isLegalized) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFF2E7D32).copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("AKTIF", color = Color(0xFF4CAF50), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFFE65100).copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("LEGALISASI", color = Color(0xFFFF9800), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = foundation.name, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(text = "Jenis: ${foundation.type.label}", color = Color.LightGray, fontSize = 13.sp)

                        if (!foundation.isLegalized) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "⚠️ Yayasan sedang dalam tahap legalisasi notaris & hukum. Anda baru dapat menyuntikkan dana abadi dan mendirikan fasilitas setelah status berizin resmi (Sisa ${foundation.constructionMonthsLeft} Bulan).",
                                color = Color(0xFFFF9800),
                                fontSize = 11.sp,
                                lineHeight = 15.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            if (foundation.isLegalized) {
                // Endowment Fund Card (Dana Abadi)
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFF2E7D32).copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1E19)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "🏦 DANA ABADI (ENDOWMENT FUND)",
                                color = Color(0xFF81C784),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = com.example.ui.formatCurrency(foundation.endowmentFund),
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "Dana ini digunakan murni untuk membiayai operasional bulanan fasilitas nirlaba. Pengeluaran fasilitas tidak memotong saldo pribadi bulanan Anda.",
                                color = Color.LightGray,
                                fontSize = 11.sp,
                                lineHeight = 14.sp
                            )

                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = {
                                        injectAmountString = ""
                                        injectError = null
                                        showInjectDialog = true
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                                ) {
                                    Icon(imageVector = Icons.Default.CurrencyExchange, contentDescription = null, tint = Color.White)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Suntik Dana", fontWeight = FontWeight.Bold, color = Color.White)
                                }
                                
                                Button(
                                    onClick = {
                                        selectedBlueprint = FoundationBlueprints.blueprints[foundation.type]?.firstOrNull()
                                        selectedTier = FoundationBlueprints.tiers[0]
                                        customFacilityName = ""
                                        buildError = null
                                        showBuildDialog = true
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37))
                                ) {
                                    Icon(imageVector = Icons.Default.AddBusiness, contentDescription = null, tint = Color(0xFF0F1E36))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Bangun Fasilitas", fontWeight = FontWeight.Bold, color = Color(0xFF0F1E36))
                                }
                            }
                        }
                    }
                }

                if (foundation.type == FoundationType.EDUCATION) {
                    item {
                        Text(
                            text = "Cetak Biru Institusi Pendidikan & Riset",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    val eduList = foundation.educationInstitutions
                    if (eduList.isNotEmpty()) {
                        val chunks = eduList.chunked(2)
                        chunks.forEach { pair ->
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    pair.forEach { inst ->
                                        Box(modifier = Modifier.weight(1.0f)) {
                                            EducationInstitutionCard(
                                                inst = inst,
                                                endowmentFund = foundation.endowmentFund,
                                                onUpgrade = {
                                                    viewModel.upgradeEduFacility(foundation.id, inst.id)
                                                },
                                                onChangeCurriculum = {
                                                    curriculumTargetInstitution = inst
                                                    showCurriculumDialog = true
                                                }
                                            )
                                        }
                                    }
                                    if (pair.size < 2) {
                                        Spacer(modifier = Modifier.weight(1.0f))
                                    }
                                }
                            }
                        }
                    } else {
                        item {
                            Text("Tidak ada data institusi pendidikan.", color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                } else {
                    // Title: Built Facilities
                    item {
                        Text(
                            text = "Fasilitas & Layanan Sosial",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    if (foundation.facilities.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF101B2B)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.HomeWork,
                                        contentDescription = null,
                                        tint = Color.Gray,
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Belum ada fasilitas dibangun.",
                                        color = Color.LightGray,
                                        fontSize = 13.sp,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = "Gunakan tombol di atas untuk mendirikan sekolah, klinik, atau panti.",
                                        color = Color.Gray,
                                        fontSize = 11.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    } else {
                        items(foundation.facilities) { facility ->
                            FacilityItemCard(facility = facility)
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Dialog: Inject Endowment Fund
    if (showInjectDialog) {
        AlertDialog(
            onDismissRequest = { showInjectDialog = false },
            title = { Text("Suntik Dana Abadi (Endowment)", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Donasikan murni dari Kas Pribadi CEO (privateBalance) Anda ke dalam yayasan ini. Dana nirlaba tidak dapat ditarik kembali ke kas pribadi.",
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )
                    OutlinedTextField(
                        value = injectAmountString,
                        onValueChange = { injectAmountString = it },
                        label = { Text("Jumlah Suntikan Saldo ($)") },
                        placeholder = { Text("Cth: 1000000") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    // Quick buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(500_000L, 1_000_000L, 5_000_000L).forEach { quickAmt ->
                            Button(
                                onClick = { injectAmountString = quickAmt.toString() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B3B2B)),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text(com.example.ui.formatCurrencyRingkas(quickAmt, false), fontSize = 11.sp, color = Color.White)
                            }
                        }
                    }

                    Text(
                        text = "Saldo Pribadi Anda: ${com.example.ui.formatCurrency(playerState.privateBalance)}",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32),
                        fontSize = 12.sp
                    )

                    if (injectError != null) {
                        Text(text = injectError!!, color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = injectAmountString.toLongOrNull()
                        if (amt == null || amt <= 0) {
                            injectError = "Jumlah harus berupa angka positif!"
                            return@Button
                        }
                        if (playerState.privateBalance < amt) {
                            injectError = "Saldo Kas Pribadi Anda kurang!"
                            return@Button
                        }
                        val success = viewModel.injectEndowmentFund(foundation.id, amt)
                        if (success) {
                            showInjectDialog = false
                        } else {
                            injectError = "Gagal menyuntikkan dana."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                ) {
                    Text("Donasikan", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showInjectDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // Dialog: Build Facility
    if (showBuildDialog) {
        AlertDialog(
            onDismissRequest = { showBuildDialog = false },
            title = { Text("Bangun Fasilitas Nirlaba", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 450.dp)
                        .padding(horizontal = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = customFacilityName,
                        onValueChange = { customFacilityName = it },
                        label = { Text("Nama Sekolah / Fasilitas") },
                        placeholder = { Text("Cth: SD Negeri Tunas Harapan") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Text("Pilih Cetak Biru Fasilitas:", fontWeight = FontWeight.Bold, fontSize = 13.sp)

                    // Blueprints selection
                    val blueprints = FoundationBlueprints.blueprints[foundation.type] ?: emptyList()
                    blueprints.forEach { blueprint ->
                        val isSel = selectedBlueprint == blueprint
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedBlueprint = blueprint }
                                .border(
                                    width = if (isSel) 2.dp else 1.dp,
                                    color = if (isSel) Color(0xFFD4AF37) else Color.Gray.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSel) Color(0xFF10223D) else Color.Transparent
                            )
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(text = blueprint.category, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                Text(text = blueprint.description, color = Color.Gray, fontSize = 11.sp, lineHeight = 13.sp)
                            }
                        }
                    }

                    Text("Pilih Kurikulum / Kualitas Layanan:", fontWeight = FontWeight.Bold, fontSize = 13.sp)

                    // Tiers selection
                    FoundationBlueprints.tiers.forEach { tier ->
                        val isSel = selectedTier == tier
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedTier = tier }
                                .border(
                                    width = if (isSel) 2.dp else 1.dp,
                                    color = if (isSel) Color(0xFFD4AF37) else Color.Gray.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSel) Color(0xFF10223D) else Color.Transparent
                            )
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(text = tier.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                Text(text = tier.description, color = Color.Gray, fontSize = 11.sp, lineHeight = 13.sp)
                            }
                        }
                    }

                    // Calculation Summary
                    selectedBlueprint?.let { bp ->
                        val cost = (bp.baseCost * selectedTier.costMultiplier).toLong()
                        val ops = (bp.baseMonthlyOps * selectedTier.opsMultiplier).toLong()
                        val prestige = (bp.basePrestige * selectedTier.prestigeMultiplier).toLong()

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("ESTIMASI BIAYA & MANFAAT", fontWeight = FontWeight.Bold, color = Color(0xFFD4AF37), fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Biaya Pembangunan:", fontSize = 11.sp, color = Color.LightGray)
                                    Text(com.example.ui.formatCurrency(cost), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Operasional Bulanan:", fontSize = 11.sp, color = Color.LightGray)
                                    Text("${com.example.ui.formatCurrency(ops)} / Bulan", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Legacy Bulanan:", fontSize = 11.sp, color = Color.LightGray)
                                    Text("+${prestige} Prestige Points", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD4AF37))
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Masa Konstruksi:", fontSize = 11.sp, color = Color.LightGray)
                                    Text("${bp.buildMonths} Bulan", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }

                    if (buildError != null) {
                        Text(text = buildError!!, color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val bp = selectedBlueprint
                        if (bp == null) {
                            buildError = "Silakan pilih cetak biru!"
                            return@Button
                        }
                        if (customFacilityName.trim().isEmpty()) {
                            buildError = "Nama fasilitas tidak boleh kosong!"
                            return@Button
                        }
                        val cost = (bp.baseCost * selectedTier.costMultiplier).toLong()
                        val ops = (bp.baseMonthlyOps * selectedTier.opsMultiplier).toLong()
                        val prestige = (bp.basePrestige * selectedTier.prestigeMultiplier).toLong()

                        if (foundation.endowmentFund < cost) {
                            buildError = "Dana Abadi Yayasan kurang! Butuh ${com.example.ui.formatCurrency(cost)}"
                            return@Button
                        }

                        val success = viewModel.buildFoundationFacility(
                            foundationId = foundation.id,
                            name = customFacilityName.trim(),
                            category = bp.category,
                            tier = selectedTier.name,
                            buildCost = cost,
                            buildMonths = bp.buildMonths,
                            monthlyOps = ops,
                            prestigeReward = prestige
                        )
                        if (success) {
                            showBuildDialog = false
                        } else {
                            buildError = "Gagal membangun fasilitas."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37))
                ) {
                    Text("Mulai Bangun", color = Color(0xFF0F1E36), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBuildDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    if (showCurriculumDialog && curriculumTargetInstitution != null) {
        val inst = curriculumTargetInstitution!!
        val options = when (inst.level) {
            "TK" -> listOf("Nasional", "Montessori", "Waldorf")
            "SD" -> listOf("Nasional", "Agama Terpadu")
            "SMA" -> listOf("Nasional", "Cambridge", "IB")
            "UNIV" -> listOf("Nasional", "Internasional")
            else -> listOf("Nasional")
        }

        AlertDialog(
            onDismissRequest = { showCurriculumDialog = false },
            title = { Text("Pilih Kurikulum - ${if (inst.level == "TK") "TK/TKA" else if (inst.level == "SD") "SD/MI" else if (inst.level == "SMA") "SMA/SMK" else "Universitas"}", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Memilih kurikulum khusus meningkatkan kualitas akreditasi, namun meningkatkan biaya operasional bulanan secara signifikan.",
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    options.forEach { option ->
                        val costMultiplier = when (option) {
                            "Montessori", "Waldorf" -> "+50% Ops"
                            "Agama Terpadu" -> "+75% Ops"
                            "Cambridge", "IB" -> "+200% Ops"
                            "Internasional" -> "+500% Ops"
                            else -> "Biaya Standar"
                        }
                        
                        val isSelected = inst.curriculumType == option
                        
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.changeEduCurriculum(foundation.id, inst.id, option)
                                    showCurriculumDialog = false
                                }
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) Color(0xFFD4AF37) else Color.White.copy(alpha = 0.05f),
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(0xFF1B2C3F) else Color(0xFF101B2B)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = option, fontWeight = FontWeight.Bold, color = if (isSelected) Color(0xFFD4AF37) else Color.White, fontSize = 13.sp)
                                    Text(text = costMultiplier, color = Color.LightGray, fontSize = 11.sp)
                                }
                                if (isSelected) {
                                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showCurriculumDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
fun FacilityItemCard(facility: FoundationFacility) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF101B2B)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(text = facility.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                    Text(text = "${facility.category} | ${facility.tier}", color = Color.LightGray, fontSize = 11.sp)
                }

                if (facility.buildMonthsLeft > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFE65100).copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("KONSTRUKSI", color = Color(0xFFFF9800), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    if (facility.isOperational) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF2E7D32).copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("AKTIF", color = Color(0xFF4CAF50), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFC62828).copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("MANGKRAK", color = Color(0xFFE53935), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
            Spacer(modifier = Modifier.height(12.dp))

            if (facility.buildMonthsLeft > 0) {
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Sisa Masa Konstruksi:", color = Color.Gray, fontSize = 11.sp)
                        Text("${facility.buildMonthsLeft} Bulan", color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Biaya Operasional", color = Color.Gray, fontSize = 11.sp)
                        Text(
                            text = "${com.example.ui.formatCurrency(facility.monthlyOperationalCost)} / bln",
                            color = Color.LightGray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Manfaat Legacy", color = Color.Gray, fontSize = 11.sp)
                        Text(
                            text = if (facility.isOperational) "+${facility.prestigeReward} Legacy" else "+0 Legacy",
                            color = if (facility.isOperational) Color(0xFFD4AF37) else Color.Gray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EducationInstitutionCard(
    inst: com.example.data.EducationInstitution,
    endowmentFund: Long,
    onUpgrade: () -> Unit,
    onChangeCurriculum: () -> Unit
) {
    val levelLabel = when (inst.level) {
        "TK" -> "TK/TKA"
        "SD" -> "SD/MI"
        "SMA" -> "SMA/SMK"
        "UNIV" -> "Universitas & Riset"
        else -> inst.level
    }

    val baseUpgradeCost = when (inst.level) {
        "TK" -> 150000L
        "SD" -> 400000L
        "SMA" -> 1200000L
        "UNIV" -> 4000000L
        else -> 150000L
    }
    val upgradeCost = baseUpgradeCost * inst.facilityLevel
    val canUpgrade = inst.facilityLevel < 5 && endowmentFund >= upgradeCost

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF101B2B)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Accreditation Points Progress
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Akreditasi: ${inst.accreditationPoints}/100",
                    color = if (inst.accreditationPoints >= 90) Color(0xFF4CAF50) else Color.LightGray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                if (inst.accreditationPoints >= 90) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF2E7D32).copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("UNGGUL", color = Color(0xFF4CAF50), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { inst.accreditationPoints / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (inst.accreditationPoints >= 90) Color(0xFF4CAF50) else Color(0xFFD4AF37),
                trackColor = Color.White.copy(alpha = 0.1f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Level & Name
            Text(
                text = levelLabel,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 14.sp
            )
            Text(
                text = "Level Fasilitas: ${inst.facilityLevel} / 5",
                color = Color.LightGray,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(6.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
            Spacer(modifier = Modifier.height(6.dp))

            // Info Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Kurikulum:", color = Color.Gray, fontSize = 11.sp)
                Text(inst.curriculumType, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Ops Bulanan:", color = Color.Gray, fontSize = 11.sp)
                Text(
                    text = com.example.ui.formatCurrency(inst.monthlyOperationalCost),
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Button Upgrade
                Button(
                    onClick = onUpgrade,
                    enabled = canUpgrade,
                    modifier = Modifier.fillMaxWidth().height(40.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD4AF37),
                        contentColor = Color(0xFF050C1A),
                        disabledContainerColor = Color.White.copy(alpha = 0.05f),
                        disabledContentColor = Color.Gray
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    val btnText = if (inst.facilityLevel >= 5) {
                        "Max Level (5)"
                    } else {
                        "Upgrade (${com.example.ui.formatCurrencyRingkas(upgradeCost, true)})"
                    }
                    Text(btnText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Button Curriculum Change
                OutlinedButton(
                    onClick = onChangeCurriculum,
                    modifier = Modifier.fillMaxWidth().height(40.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Ganti Kurikulum", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
