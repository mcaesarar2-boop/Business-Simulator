package com.example.ui

import com.example.viewmodel.GameViewModel
import com.example.data.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Warning
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
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivateEquityScreen(navController: NavController, viewModel: GameViewModel) {
    val playerState by viewModel.playerState.collectAsState()
    
    // Theme Colors
    val bgDark = Color(0xFF0F172A) // Deep sea slate navy
    val cardDark = Color(0xFF1E293B) // Darker slate
    val accentCyan = Color(0xFF00E5FF) // Neon Cyan
    val textWhite = Color.White
    val textGray = Color(0xFF94A3B8) // Slate gray
    val neonGreen = Color(0xFF10B981) // Emerald Green
    val errorRed = Color(0xFFEF4444)
    val gold = Color(0xFFFFD700)

    // Calculate total business valuation
    val totalBusinessValuation = remember(playerState) {
        val totalBusinessValuation = playerState.ownedBusinesses.sumOf {
            val catalogItem = getCatalogItem(it.catalogId, playerState)
            if (catalogItem != null) getBusinessValuation(it, catalogItem) else 0L
        }
        val totalHoldingValuation = playerState.holdingCompanies.sumOf { holding ->
            holding.subsidiaries.sumOf { sub ->
                val catalogItem = getCatalogItem(sub.catalogId, playerState)
                if (catalogItem != null) getBusinessValuation(sub, catalogItem) else 0L
            }
        }
        (totalBusinessValuation + totalHoldingValuation).coerceAtLeast(100_000L)
    }

    // Dynamic max loan limit based on 20% of valuation, capped between 100k and 50M
    val maxLoanDynamic = remember(totalBusinessValuation) {
        (totalBusinessValuation * 0.20).toLong().coerceIn(100_000L, 50_000_000L)
    }

    // Sektor data list
    val sectors = remember {
        listOf(
            SectorOffer(
                name = "Infrastruktur & Energi",
                interestRate = 0.08,
                tenor = 48,
                dilutionMultiplier = 1.8,
                description = "Suku Bunga Rendah, Tenor Sangat Panjang, Dilusi Tinggi. Sempurna untuk proyek energi & utilitas skala masif."
            ),
            SectorOffer(
                name = "Finansial & Fintech",
                interestRate = 0.18,
                tenor = 24,
                dilutionMultiplier = 1.4,
                description = "Tenor sedang, suku bunga tinggi untuk penetrasi likuiditas instan industri keuangan digital modern."
            ),
            SectorOffer(
                name = "Konsumer & Retail",
                interestRate = 0.12,
                tenor = 12,
                dilutionMultiplier = 1.2,
                description = "Tenor pendek 12 bulan dengan dilusi paling minimal. Tepat untuk pendanaan ekspansi toko fisik retail Anda."
            ),
            SectorOffer(
                name = "Teknologi & AI",
                interestRate = 0.15,
                tenor = 36,
                dilutionMultiplier = 1.6,
                description = "High Risk, High Return. Melepas kendali sedang dengan tenor 36 bulan untuk mendominasi industri otomasi AI."
            )
        )
    }

    var selectedSectorIndex by remember { mutableStateOf(0) }
    val selectedSector = sectors[selectedSectorIndex]

    // Local state for interactive slider input
    var loanSliderValue by remember { mutableStateOf(100_000.0) }
    val loanAmount = loanSliderValue.toLong().coerceIn(10_000L, maxLoanDynamic)

    // Funding Type choice state
    var selectedFundingType by remember { mutableStateOf(FundingType.DEBT) }

    // Calculate interactive values
    val baseEquityGiven = remember(loanAmount, selectedSector, totalBusinessValuation) {
        (loanAmount.toDouble() / totalBusinessValuation.toDouble()) * selectedSector.dilutionMultiplier * 100.0
    }
    
    val totalBunga = (loanAmount * selectedSector.interestRate).toLong()
    val totalPayment = loanAmount + totalBunga
    val baseMonthlyPayment = totalPayment / selectedSector.tenor

    // 3 Options details
    val (monthlyPayment, equityGiven) = remember(selectedFundingType, baseMonthlyPayment, baseEquityGiven) {
        when (selectedFundingType) {
            FundingType.DEBT -> Pair(baseMonthlyPayment, 0.0)
            FundingType.HYBRID -> Pair(baseMonthlyPayment / 2, baseEquityGiven)
            FundingType.EQUITY -> Pair(0L, baseEquityGiven)
        }
    }

    val remainingEquityPostDilution = (playerState.playerEquityShare - equityGiven).coerceAtLeast(0.0)
    val isControlViolation = remainingEquityPostDilution < 51.0

    // Total liabilities of investor loans from global state
    val totalOutstandingDebt = playerState.totalOutstandingDebt
    val totalMonthlyPayment = playerState.totalMonthlyDebtObligation

    var showSuccessDialog by remember { mutableStateOf<String?>(null) }
    var showErrorDialog by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Private Equity & Investors", fontWeight = FontWeight.Bold, color = textWhite) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = textWhite)
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
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. STATS OVERVIEW PANEL
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = cardDark),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "PORTFOLIO PEMBIAYAAN & EKUITAS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentCyan,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Saham Tersisa", fontSize = 11.sp, color = textGray)
                                Text(
                                    text = String.format(java.util.Locale.US, "%.1f%%", playerState.playerEquityShare),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (playerState.playerEquityShare < 60.0) errorRed else textWhite
                                )
                            }
                            Column(modifier = Modifier.weight(1.2f)) {
                                Text("Total Valuasi Bisnis", fontSize = 11.sp, color = textGray)
                                Text(
                                    text = formatCurrencyRingkas(totalBusinessValuation, false),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textWhite
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Divider(color = Color.White.copy(alpha = 0.08f))
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Outstanding Debt PE", fontSize = 11.sp, color = textGray)
                                Text(
                                    text = formatCurrencyRingkas(totalOutstandingDebt, false),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (totalOutstandingDebt > 0) errorRed else textWhite
                                )
                            }
                            Column(modifier = Modifier.weight(1.2f)) {
                                Text("Beban Cicilan/Bulan", fontSize = 11.sp, color = textGray)
                                Text(
                                    text = formatCurrencyRingkas(totalMonthlyPayment, false),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (totalMonthlyPayment > 0) errorRed else textWhite
                                )
                            }
                        }
                    }
                }
            }

            // 2. SECTOR SELECTOR & LOAN APPLICATION
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = cardDark),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "AJUKAN PEMBIAYAAN INVESTOR",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentCyan,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        // Sektor horizontal tabs
                        ScrollableTabRow(
                            selectedTabIndex = selectedSectorIndex,
                            containerColor = Color.Transparent,
                            contentColor = accentCyan,
                            edgePadding = 0.dp,
                            divider = {}
                        ) {
                            sectors.forEachIndexed { idx, sector ->
                                Tab(
                                    selected = selectedSectorIndex == idx,
                                    onClick = { selectedSectorIndex = idx },
                                    text = { Text(sector.name, fontSize = 12.sp, color = if (selectedSectorIndex == idx) textWhite else textGray) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Description of selected sector
                        Text(
                            text = selectedSector.description,
                            fontSize = 12.sp,
                            color = textGray,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Loan parameters details
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Tenor Kontrak", fontSize = 11.sp, color = textGray)
                                Text("${selectedSector.tenor} Bulan", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textWhite)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Suku Bunga", fontSize = 11.sp, color = textGray)
                                Text("${(selectedSector.interestRate * 100).toInt()}% Flat", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textWhite)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Multiplier Resiko", fontSize = 11.sp, color = textGray)
                                Text("${selectedSector.dilutionMultiplier}x", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textWhite)
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Amount slider
                        Text(
                            text = "Jumlah Dana Diajukan: ${formatCurrencyRingkas(loanAmount, false)}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = textWhite
                        )
                        Slider(
                            value = loanSliderValue.toFloat(),
                            onValueChange = { loanSliderValue = it.toDouble() },
                            valueRange = 10_000f..maxLoanDynamic.toFloat(),
                            colors = SliderDefaults.colors(
                                thumbColor = accentCyan,
                                activeTrackColor = accentCyan,
                                inactiveTrackColor = textGray.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(formatCurrencyRingkas(10_000L, true), fontSize = 10.sp, color = textGray)
                            Text("Max: " + formatCurrencyRingkas(maxLoanDynamic, true), fontSize = 10.sp, color = textGray)
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Divider(color = Color.White.copy(alpha = 0.08f))
                        Spacer(modifier = Modifier.height(16.dp))

                        // Options Card Selector
                        Text(
                            text = "PILIH OPSI PENDANAAN",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = textGray,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            FundingType.values().forEach { type ->
                                val isSelected = selectedFundingType == type
                                val (title, desc, details) = when (type) {
                                    FundingType.DEBT -> Triple(
                                        "Debt Financing (Hutang Murni)",
                                        "Pembayaran cicilan bulanan penuh, tanpa dilusi saham (0.0%).",
                                        "Cicilan: ${formatCurrencyRingkas(baseMonthlyPayment, false)} | Saham: 0.0%"
                                    )
                                    FundingType.HYBRID -> Triple(
                                        "Mezzanine (Hybrid)",
                                        "Pembayaran cicilan 50%, dengan dilusi saham proporsional.",
                                        "Cicilan: ${formatCurrencyRingkas(baseMonthlyPayment / 2, false)} | Saham: ${String.format(java.util.Locale.US, "%.2f%%", baseEquityGiven)}"
                                    )
                                    FundingType.EQUITY -> Triple(
                                        "Venture Capital (Jual Saham)",
                                        "Tanpa cicilan bulanan, melepas kepemilikan saham penuh.",
                                        "Cicilan: $0 | Saham: ${String.format(java.util.Locale.US, "%.2f%%", baseEquityGiven)}"
                                    )
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) accentCyan.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.02f))
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) accentCyan else Color.White.copy(alpha = 0.08f),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable { selectedFundingType = type }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { selectedFundingType = type },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = accentCyan,
                                            unselectedColor = textGray
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = textWhite)
                                        Text(desc, fontSize = 11.sp, color = textGray)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(details, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = if (isSelected) accentCyan else gold)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        Divider(color = Color.White.copy(alpha = 0.08f))
                        Spacer(modifier = Modifier.height(16.dp))

                        // Live estimation calculation box
                        Text("ESTIMASI TRANSAKSI SEBELUM DIAJUKAN", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textGray)
                        Spacer(modifier = Modifier.height(8.dp))

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                                .background(Color.White.copy(alpha = 0.01f))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Estimasi Dilusi Saham", fontSize = 12.sp, color = textGray)
                                Text(
                                    text = String.format(java.util.Locale.US, "-%.2f%%", equityGiven),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (equityGiven > 0) errorRed else textGray
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Estimasi Sisa Saham Anda", fontSize = 12.sp, color = textGray)
                                Text(
                                    text = String.format(java.util.Locale.US, "%.2f%%", remainingEquityPostDilution),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (remainingEquityPostDilution < 51.0) errorRed else neonGreen
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Estimasi Cicilan per Bulan", fontSize = 12.sp, color = textGray)
                                Text(
                                    text = formatCurrencyRingkas(monthlyPayment, false),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textWhite
                                )
                            }
                        }

                        if (isControlViolation) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Batas kontrol mayoritas tercapai! Anda tidak bisa melepas saham lagi.",
                                color = errorRed,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Submit Button
                        Button(
                            onClick = {
                                val err = viewModel.applyForInvestorsLoan(
                                    sectorName = selectedSector.name,
                                    loanAmount = loanAmount,
                                    tenorMonths = selectedSector.tenor,
                                    interestRate = selectedSector.interestRate,
                                    dilutionMultiplier = selectedSector.dilutionMultiplier,
                                    fundingType = selectedFundingType
                                )
                                if (err != null) {
                                    showErrorDialog = err
                                } else {
                                    showSuccessDialog = "Pengajuan dana sebesar ${formatCurrencyRingkas(loanAmount, false)} disetujui! Dana langsung cair ke Kas Pribadi (Family Office) Anda."
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isControlViolation) Color.Gray else accentCyan,
                                contentColor = bgDark
                            ),
                            shape = RoundedCornerShape(10.dp),
                            enabled = !isControlViolation,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (isControlViolation) "DITOLAK (SAHAM < 51%)" else "Ajukan Pembiayaan",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            // 3. OUTSTANDING DEBTS PANEL
            item {
                Text(
                    text = "Daftar Hutang & Pembiayaan Aktif (${playerState.activeInvestorsLoans.size})",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = textWhite,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (playerState.activeInvestorsLoans.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.01f))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Tidak ada hutang atau pembiayaan aktif kepada investor saat ini.",
                            color = textGray,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(playerState.activeInvestorsLoans) { loan ->
                    val outstandingVal = loan.monthlyPayment * loan.remainingMonths
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = cardDark.copy(alpha = 0.8f)),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = loan.sectorName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = accentCyan
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                when (loan.fundingType) {
                                                    FundingType.DEBT -> gold.copy(alpha = 0.15f)
                                                    FundingType.HYBRID -> accentCyan.copy(alpha = 0.15f)
                                                    FundingType.EQUITY -> errorRed.copy(alpha = 0.15f)
                                                },
                                                RoundedCornerShape(4.dp)
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = loan.fundingType.name,
                                            fontSize = 10.sp,
                                            color = when (loan.fundingType) {
                                                FundingType.DEBT -> gold
                                                FundingType.HYBRID -> accentCyan
                                                FundingType.EQUITY -> errorRed
                                            },
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    
                                    Box(
                                        modifier = Modifier
                                            .background(accentCyan.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "${loan.remainingMonths} bln tersisa",
                                            fontSize = 11.sp,
                                            color = accentCyan,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Divider(color = Color.White.copy(alpha = 0.05f))
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Cicilan Bulanan", fontSize = 11.sp, color = textGray)
                                    Text(formatCurrencyRingkas(loan.monthlyPayment, false), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textWhite)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Saham Digadaikan", fontSize = 11.sp, color = textGray)
                                    Text(String.format(java.util.Locale.US, "%.1f%%", loan.equityGiven), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = errorRed)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Sisa Hutang", fontSize = 11.sp, color = textGray)
                                    Text(formatCurrencyRingkas(outstandingVal, false), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = gold)
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // Alerts and Dialogs
    showSuccessDialog?.let { msg ->
        AlertDialog(
            onDismissRequest = { showSuccessDialog = null },
            title = { Text("Transaksi Berhasil", fontWeight = FontWeight.Bold, color = neonGreen) },
            text = { Text(msg, color = textWhite) },
            confirmButton = {
                TextButton(onClick = { showSuccessDialog = null }) {
                    Text("Ok", color = accentCyan, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = cardDark
        )
    }

    showErrorDialog?.let { msg ->
        AlertDialog(
            onDismissRequest = { showErrorDialog = null },
            title = { Text("Transaksi Ditolak", fontWeight = FontWeight.Bold, color = errorRed) },
            text = { Text(msg, color = textWhite) },
            confirmButton = {
                TextButton(onClick = { showErrorDialog = null }) {
                    Text("Tutup", color = accentCyan, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = cardDark
        )
    }
}

data class SectorOffer(
    val name: String,
    val interestRate: Double,
    val tenor: Int,
    val dilutionMultiplier: Double,
    val description: String
)
