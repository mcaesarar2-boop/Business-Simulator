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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.data.*
import com.example.viewmodel.GameViewModel
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToInt

// Terminal FinTech Theme Colors
private val NavyDarkBg = Color(0xFF070D18)
private val NavyCardBg = Color(0xFF0D1B2A)
private val NavyCardSecondary = Color(0xFF132338)
private val GoldAccent = Color(0xFFFFD700)
private val GoldDim = Color(0xFFC5A028)
private val EmeraldAccent = Color(0xFF10B981)
private val EmeraldDark = Color(0xFF065F46)
private val CrimsonRed = Color(0xFFEF4444)
private val AmberWarn = Color(0xFFF59E0B)
private val BlueTerminal = Color(0xFF38BDF8)
private val SlateText = Color(0xFF94A3B8)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BankingDashboardScreen(
    navController: NavController,
    viewModel: GameViewModel,
    instanceId: String
) {
    val playerState by viewModel.playerState.collectAsState()
    val bankBusiness = viewModel.getBankingBusiness(instanceId)
    val useShortFormat by viewModel.useShortNumberFormat.collectAsState()

    var activeTab by remember { mutableStateOf(0) } // 0: Credit Desk, 1: Active Loan Book, 2: Interest Policy, 3: Tier Expansion
    var showCapitalModal by remember { mutableStateOf(false) }
    var capitalModalMode by remember { mutableStateOf("INJECT") } // "INJECT" or "WITHDRAW"
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    if (bankBusiness == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(NavyDarkBg),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.AccountBalance, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("Unit Bisnis Bank Tidak Ditemukan", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { navController.popBackStack() }, colors = ButtonDefaults.buttonColors(containerColor = GoldAccent)) {
                    Text("Kembali", color = Color.Black)
                }
            }
        }
        return
    }

    val bankingData = bankBusiness.bankingData
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale.US).apply { maximumFractionDigits = 0 } }

    Scaffold(
        containerColor = NavyDarkBg,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = bankBusiness.customName ?: bankBusiness.name,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = when (bankingData.currentTier) {
                                    BankTier.TIER_1_MICRO -> BlueTerminal.copy(alpha = 0.2f)
                                    BankTier.TIER_2_RETAIL -> EmeraldAccent.copy(alpha = 0.2f)
                                    BankTier.TIER_3_CORPORATE -> GoldAccent.copy(alpha = 0.2f)
                                },
                                border = BorderStroke(
                                    1.dp,
                                    when (bankingData.currentTier) {
                                        BankTier.TIER_1_MICRO -> BlueTerminal
                                        BankTier.TIER_2_RETAIL -> EmeraldAccent
                                        BankTier.TIER_3_CORPORATE -> GoldAccent
                                    }
                                )
                            ) {
                                Text(
                                    text = bankingData.currentTier.label,
                                    color = when (bankingData.currentTier) {
                                        BankTier.TIER_1_MICRO -> BlueTerminal
                                        BankTier.TIER_2_RETAIL -> EmeraldAccent
                                        BankTier.TIER_3_CORPORATE -> GoldAccent
                                    },
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Bloomberg Financial Core • Reserve Ratio Engine",
                            color = SlateText,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.processBankMonthlyTick(instanceId)
                        snackbarMessage = "Simulasi siklus bulanan bank berhasil diproses!"
                    }) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Simulate Tick", tint = EmeraldAccent)
                    }
                    IconButton(onClick = {
                        capitalModalMode = "INJECT"
                        showCapitalModal = true
                    }) {
                        Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Kas Modal", tint = GoldAccent)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyCardBg)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. BANNER WARNING JIKA GWM / LIKUIDITAS RENDAH
            if (bankingData.liquidityRatioPercent < 10.0) {
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = CrimsonRed.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, CrimsonRed)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = CrimsonRed, modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "PERINGATAN KRISIS LIKUIDITAS (GWM < 10%)",
                                    color = CrimsonRed,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "Rasio uang tunai di brankas terlalu tipis! Bank terancam denda OJK / Bank Sentral. Segera suntik modal internal atau stop pencairan pinjaman baru.",
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            // 2. FINANCIAL OVERVIEW METRICS GRID (DPK, Loan Book, Liquidity, NPL)
            item {
                BankingCoreMetricsSection(
                    bankingData = bankingData,
                    useShortFormat = useShortFormat,
                    currencyFormat = currencyFormat,
                    onInjectCashClick = {
                        capitalModalMode = "INJECT"
                        showCapitalModal = true
                    }
                )
            }

            // 3. NET INTEREST INCOME & SPREAD SUMMARY CARD
            item {
                MonthlyFinancialSummaryCard(
                    bankingData = bankingData,
                    currencyFormat = currencyFormat
                )
            }

            // 4. NAVIGATION TABS (Credit Desk, Active Book, Suku Bunga, Tiering)
            item {
                TabRow(
                    selectedTabIndex = activeTab,
                    containerColor = NavyCardSecondary,
                    contentColor = GoldAccent,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                            color = GoldAccent
                        )
                    }
                ) {
                    Tab(
                        selected = activeTab == 0,
                        onClick = { activeTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Credit Desk", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                if (bankingData.incomingApplications.isNotEmpty()) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Badge(containerColor = GoldAccent, contentColor = Color.Black) {
                                        Text("${bankingData.incomingApplications.size}", fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    )
                    Tab(
                        selected = activeTab == 1,
                        onClick = { activeTab = 1 },
                        text = {
                            val activeCount = bankingData.activeLoans.count { it.healthStatus != LoanHealthStatus.SETTLED }
                            Text("Portofolio ($activeCount)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    )
                    Tab(
                        selected = activeTab == 2,
                        onClick = { activeTab = 2 },
                        text = { Text("Suku Bunga", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                    )
                    Tab(
                        selected = activeTab == 3,
                        onClick = { activeTab = 3 },
                        text = { Text("Tier Ekspansi", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                    )
                }
            }

            // 5. TAB CONTENT
            when (activeTab) {
                0 -> {
                    // CREDIT DESK: INCOMING LOAN PROPOSALS
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Antrean Pengajuan Kredit",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "Analisis profil risiko sebelum menyetujui pencairan dana DPK",
                                    color = SlateText,
                                    fontSize = 11.sp
                                )
                            }
                            OutlinedButton(
                                onClick = {
                                    viewModel.refreshBankLoanApplications(instanceId)
                                    snackbarMessage = "Pipa pengajuan kredit diperbarui!"
                                },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = BlueTerminal),
                                border = BorderStroke(1.dp, BlueTerminal.copy(alpha = 0.5f)),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Cari Debitur", fontSize = 11.sp)
                            }
                        }
                    }

                    if (bankingData.incomingApplications.isEmpty()) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = NavyCardBg,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(Icons.Default.CheckCircleOutline, contentDescription = null, tint = EmeraldAccent, modifier = Modifier.size(48.dp))
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text("Semua Pengajuan Telah Ditinjau", color = Color.White, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Tekan tombol 'Cari Debitur' untuk memunculkan aplikasi pinjaman baru dari pasar.", color = SlateText, fontSize = 12.sp, textAlign = TextAlign.Center)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(
                                        onClick = { viewModel.refreshBankLoanApplications(instanceId) },
                                        colors = ButtonDefaults.buttonColors(containerColor = BlueTerminal)
                                    ) {
                                        Text("Generate Aplikasi Pinjaman", color = Color.Black, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    } else {
                        items(bankingData.incomingApplications) { app ->
                            LoanApplicationReviewCard(
                                application = app,
                                vaultCash = bankingData.vaultCash,
                                currencyFormat = currencyFormat,
                                onApprove = {
                                    val success = viewModel.approveBankLoan(instanceId, app)
                                    if (success) {
                                        snackbarMessage = if (app.isInternalCorporateSynergy) {
                                            "Kredit B2B Sindikasi ${app.applicantName} disetujui! Dana langsung disuntikkan ke anak perusahaan."
                                        } else {
                                            "Kredit ${app.applicantName} disetujui & dicairkan!"
                                        }
                                    } else {
                                        snackbarMessage = "Gagal mencairkan! Uang kas di brankas (Vault Cash) tidak mencukupi."
                                    }
                                },
                                onReject = {
                                    viewModel.rejectBankLoan(instanceId, app.id)
                                    snackbarMessage = "Pengajuan pinjaman ${app.applicantName} ditolak."
                                }
                            )
                        }
                    }
                }

                1 -> {
                    // ACTIVE DISBURSED LOANS
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Buku Portofolio Kredit Berjalan",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "Monitoring angsuran pokok, bunga, dan kredit macet (NPL)",
                                    color = SlateText,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    val activeList = bankingData.activeLoans.filter { it.healthStatus != LoanHealthStatus.SETTLED }
                    if (activeList.isEmpty()) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = NavyCardBg,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(Icons.Default.HourglassEmpty, contentDescription = null, tint = SlateText, modifier = Modifier.size(48.dp))
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text("Belum Ada Kredit yang Sedang Berjalan", color = Color.White, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Buka tab 'Credit Desk' dan setujui aplikasi pinjaman untuk mulai memutarkan dana DPK.", color = SlateText, fontSize = 12.sp, textAlign = TextAlign.Center)
                                }
                            }
                        }
                    } else {
                        items(activeList) { loan ->
                            ActiveLoanBookItemCard(
                                loan = loan,
                                currencyFormat = currencyFormat,
                                onWriteOffNpl = {
                                    viewModel.writeOffBankNplLoan(instanceId, loan.id)
                                    snackbarMessage = "Kredit macet ${loan.borrowerName} telah dihapusbukukan (Write-Off)."
                                }
                            )
                        }
                    }
                }

                2 -> {
                    // KEBIJAKAN SUKU BUNGA & NIM
                    item {
                        InterestRatePolicyControlSection(
                            bankingData = bankingData,
                            onRatesChanged = { dep, lend ->
                                viewModel.setBankInterestRates(instanceId, dep, lend)
                            }
                        )
                    }
                }

                3 -> {
                    // TIER EXPANSION & LICENSE UNLOCKS
                    item {
                        TierExpansionSection(
                            bankingData = bankingData,
                            currencyFormat = currencyFormat,
                            onUnlockTier = { nextTier ->
                                val success = viewModel.unlockBankTier(instanceId, nextTier)
                                if (success) {
                                    snackbarMessage = "Selamat! Bank berhasil naik kelas ke ${nextTier.label}."
                                } else {
                                    snackbarMessage = "Syarat DPK atau Kas Modal internal belum mencukupi untuk naik tier."
                                }
                            }
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    // Capital Injection / Withdrawal Modal
    if (showCapitalModal) {
        CapitalManagementDialog(
            mode = capitalModalMode,
            playerCash = playerState.cash,
            bankInternalCash = bankingData.internalCash,
            currencyFormat = currencyFormat,
            onDismiss = { showCapitalModal = false },
            onConfirm = { amount ->
                if (capitalModalMode == "INJECT") {
                    viewModel.injectCapitalToBank(instanceId, amount)
                    snackbarMessage = "Berhasil menyuntikkan ${currencyFormat.format(amount)} ke kas internal bank."
                } else {
                    val ok = viewModel.withdrawCapitalFromBank(instanceId, amount)
                    if (ok) {
                        snackbarMessage = "Berhasil menarik dividen ${currencyFormat.format(amount)} dari kas bank ke dompet pribadi."
                    } else {
                        snackbarMessage = "Kas internal bank tidak mencukupi untuk ditarik."
                    }
                }
                showCapitalModal = false
            }
        )
    }

    // Snackbar alert
    if (snackbarMessage != null) {
        LaunchedEffect(snackbarMessage) {
            kotlinx.coroutines.delay(3500)
            snackbarMessage = null
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = NavyCardSecondary,
                border = BorderStroke(1.dp, GoldAccent),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = snackbarMessage ?: "", color = Color.White, fontSize = 13.sp)
                }
            }
        }
    }
}

/**
 * 4 Key Metrics Card: DPK, Loan Book, Liquidity Ratio, NPL
 */
@Composable
private fun BankingCoreMetricsSection(
    bankingData: BankingCompanyData,
    useShortFormat: Boolean,
    currencyFormat: NumberFormat,
    onInjectCashClick: () -> Unit
) {
    val totalDpk = bankingData.totalCustomerDepositsDpk
    val totalLoanBook = bankingData.totalOutstandingLoansPrincipal
    val vaultCash = bankingData.vaultCash
    val liquidityRatio = bankingData.liquidityRatioPercent
    val nplRatio = bankingData.nplRatioPercent

    fun formatAmt(amount: Long): String {
        return if (useShortFormat) {
            when {
                amount >= 1_000_000_000L -> String.format(Locale.US, "$%.2fB", amount / 1_000_000_000.0)
                amount >= 1_000_000L -> String.format(Locale.US, "$%.1fM", amount / 1_000_000.0)
                amount >= 1_000L -> String.format(Locale.US, "$%.0fK", amount / 1_000.0)
                else -> "$$amount"
            }
        } else {
            currencyFormat.format(amount)
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. DPK (Dana Pihak Ketiga)
            MetricCard(
                modifier = Modifier.weight(1f),
                title = "Dana Nasabah (DPK)",
                value = formatAmt(totalDpk),
                subtitle = "Bunga Simpanan: ${String.format(Locale.US, "%.1f%%", bankingData.depositInterestRate * 100)}",
                icon = Icons.Default.Savings,
                accentColor = BlueTerminal
            )

            // 2. Loan Book (Portofolio Kredit)
            MetricCard(
                modifier = Modifier.weight(1f),
                title = "Portofolio Kredit",
                value = formatAmt(totalLoanBook),
                subtitle = "Lending Rate: ${String.format(Locale.US, "%.1f%%", bankingData.lendingInterestRate * 100)}",
                icon = Icons.Default.Payments,
                accentColor = GoldAccent
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 3. Liquidity Ratio (GWM Engine)
            val liqColor = when {
                liquidityRatio >= 20.0 -> EmeraldAccent
                liquidityRatio >= 10.0 -> AmberWarn
                else -> CrimsonRed
            }
            val liqStatus = when {
                liquidityRatio >= 20.0 -> "Sangat Aman (Safe)"
                liquidityRatio >= 10.0 -> "Waspada (GWM Ketat)"
                else -> "KRISIS LIKUIDITAS!"
            }
            MetricCard(
                modifier = Modifier.weight(1f),
                title = "Rasio Likuiditas",
                value = String.format(Locale.US, "%.1f%%", liquidityRatio),
                subtitle = "Kas Brankas: ${formatAmt(vaultCash)} ($liqStatus)",
                icon = Icons.Default.WaterDrop,
                accentColor = liqColor
            )

            // 4. NPL Ratio
            val nplColor = when {
                nplRatio <= 2.0 -> EmeraldAccent
                nplRatio <= 5.0 -> AmberWarn
                else -> CrimsonRed
            }
            val nplStatus = when {
                nplRatio <= 2.0 -> "Sehat (<2%)"
                nplRatio <= 5.0 -> "Toleransi (2-5%)"
                else -> "Berbahaya (>5%)"
            }
            MetricCard(
                modifier = Modifier.weight(1f),
                title = "Rasio NPL (Macet)",
                value = String.format(Locale.US, "%.2f%%", nplRatio),
                subtitle = "Status Buku: $nplStatus",
                icon = Icons.Default.Shield,
                accentColor = nplColor
            )
        }

        // Vault Cash & Internal Equity Bar
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = NavyCardBg,
            border = BorderStroke(1.dp, NavyCardSecondary),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Kas Modal Internal (Owner Equity)", color = SlateText, fontSize = 11.sp)
                        Text(formatAmt(bankingData.internalCash), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
                TextButton(
                    onClick = onInjectCashClick,
                    colors = ButtonDefaults.textButtonColors(contentColor = GoldAccent)
                ) {
                    Text("+ Kelola Modal", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun MetricCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = NavyCardBg,
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.25f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, color = SlateText, fontSize = 11.sp, maxLines = 1)
                Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                color = accentColor,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Monthly Financial Summary Card
 */
@Composable
private fun MonthlyFinancialSummaryCard(
    bankingData: BankingCompanyData,
    currencyFormat: NumberFormat
) {
    val activeLoans = bankingData.activeLoans.filter { it.healthStatus != LoanHealthStatus.SETTLED && it.healthStatus != LoanHealthStatus.NON_PERFORMING }
    val monthlyInterestRevenue = activeLoans.sumOf { it.monthlyInterestPayment }
    val monthlyDepositExpense = (bankingData.totalCustomerDepositsDpk * (bankingData.depositInterestRate / 12.0)).toLong()
    val monthlyOverhead = (bankingData.totalCustomerDepositsDpk * 0.0005).toLong() + 25_000L
    val netMonthlyProfit = monthlyInterestRevenue - monthlyDepositExpense - monthlyOverhead

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = NavyCardBg,
        border = BorderStroke(1.dp, NavyCardSecondary),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Icon(Icons.Default.Analytics, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Kinerja Laba / Rugi (NII)",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (netMonthlyProfit >= 0) EmeraldDark.copy(alpha = 0.5f) else CrimsonRed.copy(alpha = 0.3f),
                    modifier = Modifier.wrapContentWidth()
                ) {
                    Text(
                        text = if (netMonthlyProfit >= 0) "+${currencyFormat.format(netMonthlyProfit)}/bln" else "${currencyFormat.format(netMonthlyProfit)}/bln",
                        color = if (netMonthlyProfit >= 0) EmeraldAccent else CrimsonRed,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = NavyCardSecondary)
            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Pendapatan Bunga Kredit (+)", color = SlateText, fontSize = 12.sp)
                Text(currencyFormat.format(monthlyInterestRevenue), color = EmeraldAccent, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Beban Bunga Simpanan Nasabah (-)", color = SlateText, fontSize = 12.sp)
                Text(currencyFormat.format(monthlyDepositExpense), color = CrimsonRed, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Biaya Operasional & IT (-)", color = SlateText, fontSize = 12.sp)
                Text(currencyFormat.format(monthlyOverhead), color = SlateText, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
            }
        }
    }
}

/**
 * Loan Application Review Card (Credit Underwriting Desk)
 */
@Composable
private fun LoanApplicationReviewCard(
    application: LoanApplication,
    vaultCash: Long,
    currencyFormat: NumberFormat,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    val canDisburse = vaultCash >= application.principalAmount
    val monthlyInterest = (application.principalAmount * (application.annualInterestRate / 12.0)).toLong()
    val monthlyPrincipal = application.principalAmount / application.tenorMonths
    val totalMonthlyInstallment = monthlyPrincipal + monthlyInterest
    val totalInterestEarned = monthlyInterest * application.tenorMonths

    val gradeColor = when (application.creditGrade) {
        CreditGrade.GRADE_A -> EmeraldAccent
        CreditGrade.GRADE_B -> BlueTerminal
        CreditGrade.GRADE_C -> AmberWarn
    }

    val cardBorderColor = if (application.isInternalCorporateSynergy) GoldAccent else NavyCardSecondary

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = NavyCardBg,
        border = BorderStroke(1.dp, cardBorderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Applicant Name + B2B Synergy Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = CircleShape,
                        color = gradeColor.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, gradeColor),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = application.creditGrade.name.takeLast(1),
                                color = gradeColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = application.applicantName,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${application.sector.label} • Tenor: ${application.tenorMonths} Bulan",
                            color = SlateText,
                            fontSize = 11.sp
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = gradeColor.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, gradeColor.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "${application.creditGrade.ratingLabel} (Resiko ${(application.creditGrade.baseDefaultRisk * 100).toInt()}%)",
                        color = gradeColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            // Internal Corporate Synergy Banner
            if (application.isInternalCorporateSynergy) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = GoldAccent.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Hub, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "SINDIKASI INTERNAL B2B: Dana pokok kredit dicairkan langsung ke kas ${application.linkedBusinessName ?: "Anak Perusahaan"}!",
                            color = GoldAccent,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(text = application.proposalNote, color = SlateText, fontSize = 11.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = NavyCardSecondary)
            Spacer(modifier = Modifier.height(10.dp))

            // Financial Summary of the Loan
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Plafon Pinjaman", color = SlateText, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        currencyFormat.format(application.principalAmount),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        softWrap = false
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Suku Bunga Kredit", color = SlateText, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        String.format(Locale.US, "%.1f%% p.a.", application.annualInterestRate * 100),
                        color = GoldAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Angsuran per Bulan:", color = SlateText, fontSize = 11.sp)
                Text(
                    "${currencyFormat.format(totalMonthlyInstallment)}/bln",
                    color = EmeraldAccent,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    softWrap = false
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Total Bunga yang Dihasilkan:", color = SlateText, fontSize = 11.sp)
                Text(
                    "+${currencyFormat.format(totalInterestEarned)}",
                    color = GoldAccent,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    softWrap = false
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons: Reject / Disburse
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onReject,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CrimsonRed),
                    border = BorderStroke(1.dp, CrimsonRed.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Tolak", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Button(
                    onClick = onApprove,
                    enabled = canDisburse,
                    modifier = Modifier.weight(1.5f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EmeraldAccent,
                        disabledContainerColor = SlateText.copy(alpha = 0.2f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = if (canDisburse) Color.Black else SlateText, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (canDisburse) "Setujui & Cairkan" else "Kas Brankas Kurang",
                        color = if (canDisburse) Color.Black else SlateText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

/**
 * Active Disbursed Loan Item Card
 */
@Composable
private fun ActiveLoanBookItemCard(
    loan: ActiveDisbursedLoan,
    currencyFormat: NumberFormat,
    onWriteOffNpl: () -> Unit
) {
    val isNpl = loan.healthStatus == LoanHealthStatus.NON_PERFORMING
    val statusColor = when (loan.healthStatus) {
        LoanHealthStatus.PERFORMING -> EmeraldAccent
        LoanHealthStatus.SPECIAL_MENTION -> AmberWarn
        LoanHealthStatus.NON_PERFORMING -> CrimsonRed
        LoanHealthStatus.SETTLED -> SlateText
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = NavyCardBg,
        border = BorderStroke(1.dp, if (isNpl) CrimsonRed else NavyCardSecondary),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = loan.borrowerName,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (loan.isInternalCorporateSynergy) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = GoldAccent.copy(alpha = 0.2f)
                            ) {
                                Text("B2B", color = GoldAccent, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                            }
                        }
                    }
                    Text(
                        text = "${loan.sector.label} • Sisa Tenor: ${loan.remainingMonths} bln",
                        color = SlateText,
                        fontSize = 11.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = statusColor.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, statusColor.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = loan.healthStatus.label,
                        color = statusColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = NavyCardSecondary)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Sisa Pokok", color = SlateText, fontSize = 10.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        currencyFormat.format(loan.remainingPrincipal),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        softWrap = false
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Angsuran/Bln", color = SlateText, fontSize = 10.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        if (isNpl) "$0 (Macet)" else "${currencyFormat.format(loan.monthlyPrincipalPayment + loan.monthlyInterestPayment)}",
                        color = if (isNpl) CrimsonRed else EmeraldAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        softWrap = false
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Total Bunga Diterima", color = SlateText, fontSize = 10.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        currencyFormat.format(loan.totalInterestCollected),
                        color = GoldAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            if (isNpl) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Debitur gagal bayar. Hapus buku untuk bersihkan neraca.", color = CrimsonRed, fontSize = 10.sp, modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(
                        onClick = onWriteOffNpl,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CrimsonRed),
                        border = BorderStroke(1.dp, CrimsonRed),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("Write-Off", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Interest Rate Policy (Spread & NIM Engine)
 */
@Composable
private fun InterestRatePolicyControlSection(
    bankingData: BankingCompanyData,
    onRatesChanged: (Double, Double) -> Unit
) {
    var depositRate by remember(bankingData.depositInterestRate) { mutableStateOf(bankingData.depositInterestRate) }
    var lendingRate by remember(bankingData.lendingInterestRate) { mutableStateOf(bankingData.lendingInterestRate) }

    val nimSpread = (lendingRate - depositRate) * 100.0

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = NavyCardBg,
        border = BorderStroke(1.dp, NavyCardSecondary),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Kebijakan Moneter & Suku Bunga", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("Atur spread untuk menarik DPK atau menggenjot margin keuntungan", color = SlateText, fontSize = 11.sp)
                }
                Icon(Icons.Default.Tune, contentDescription = null, tint = GoldAccent)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // NIM Gauge Card
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = NavyCardSecondary,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Net Interest Margin (NIM)", color = SlateText, fontSize = 11.sp)
                        Text(
                            text = String.format(Locale.US, "%.2f%%", nimSpread),
                            color = if (nimSpread >= 4.0) EmeraldAccent else if (nimSpread >= 1.0) GoldAccent else CrimsonRed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Text(
                        text = when {
                            nimSpread >= 8.0 -> "Margin Agresif"
                            nimSpread >= 4.0 -> "Margin Sehat"
                            nimSpread >= 1.0 -> "Margin Tipis"
                            else -> "Margin Negatif (Rugi!)"
                        },
                        color = SlateText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 1. Slider Suku Bunga Simpanan (Deposit Rate)
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Suku Bunga Simpanan (DPK)", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Text(String.format(Locale.US, "%.1f%%", depositRate * 100), color = BlueTerminal, fontWeight = FontWeight.Bold, fontSize = 14.sp, fontFamily = FontFamily.Monospace)
                }
                Text(
                    text = if (depositRate >= 0.06) "Tinggi: DPK akan tumbuh pesat, namun beban bunga membengkak."
                    else if (depositRate >= 0.03) "Normal: DPK stabil bertumbuh sesuai pasar."
                    else "Rendah: Nasabah cenderung memindahkan dananya ke bank lain.",
                    color = SlateText,
                    fontSize = 10.sp
                )
                Slider(
                    value = (depositRate * 100).toFloat(),
                    onValueChange = {
                        depositRate = it.toDouble() / 100.0
                        onRatesChanged(depositRate, lendingRate)
                    },
                    valueRange = 1f..15f,
                    steps = 27,
                    colors = SliderDefaults.colors(
                        thumbColor = BlueTerminal,
                        activeTrackColor = BlueTerminal,
                        inactiveTrackColor = SlateText.copy(alpha = 0.3f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Slider Suku Bunga Kredit (Lending Rate)
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Suku Bunga Kredit Dasar (Lending Rate)", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Text(String.format(Locale.US, "%.1f%%", lendingRate * 100), color = GoldAccent, fontWeight = FontWeight.Bold, fontSize = 14.sp, fontFamily = FontFamily.Monospace)
                }
                Text(
                    text = if (lendingRate >= 0.20) "Sangat Tinggi: Profit bunga besar, namun risiko gagal bayar (NPL) naik drastis!"
                    else if (lendingRate >= 0.10) "Kompetitif: Menarik bagi debitur berkualitas dengan rasio NPL terjaga."
                    else "Konservatif: Risiko kredit sangat rendah.",
                    color = SlateText,
                    fontSize = 10.sp
                )
                Slider(
                    value = (lendingRate * 100).toFloat(),
                    onValueChange = {
                        lendingRate = it.toDouble() / 100.0
                        onRatesChanged(depositRate, lendingRate)
                    },
                    valueRange = 4f..35f,
                    steps = 30,
                    colors = SliderDefaults.colors(
                        thumbColor = GoldAccent,
                        activeTrackColor = GoldAccent,
                        inactiveTrackColor = SlateText.copy(alpha = 0.3f)
                    )
                )
            }
        }
    }
}

/**
 * Tier Expansion & License Section
 */
@Composable
private fun TierExpansionSection(
    bankingData: BankingCompanyData,
    currencyFormat: NumberFormat,
    onUnlockTier: (BankTier) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Lisensi Perbankan & Kategori Layanan", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)

        BankTierCard(
            tier = BankTier.TIER_1_MICRO,
            currentTier = bankingData.currentTier,
            dpk = bankingData.totalCustomerDepositsDpk,
            internalCash = bankingData.internalCash,
            currencyFormat = currencyFormat,
            onUnlock = { onUnlockTier(BankTier.TIER_1_MICRO) }
        )

        BankTierCard(
            tier = BankTier.TIER_2_RETAIL,
            currentTier = bankingData.currentTier,
            dpk = bankingData.totalCustomerDepositsDpk,
            internalCash = bankingData.internalCash,
            currencyFormat = currencyFormat,
            onUnlock = { onUnlockTier(BankTier.TIER_2_RETAIL) }
        )

        BankTierCard(
            tier = BankTier.TIER_3_CORPORATE,
            currentTier = bankingData.currentTier,
            dpk = bankingData.totalCustomerDepositsDpk,
            internalCash = bankingData.internalCash,
            currencyFormat = currencyFormat,
            onUnlock = { onUnlockTier(BankTier.TIER_3_CORPORATE) }
        )
    }
}

@Composable
private fun BankTierCard(
    tier: BankTier,
    currentTier: BankTier,
    dpk: Long,
    internalCash: Long,
    currencyFormat: NumberFormat,
    onUnlock: () -> Unit
) {
    val isCurrent = tier == currentTier
    val isUnlocked = tier.level <= currentTier.level
    val dpkProgress = (dpk.toFloat() / tier.requiredDpkToUnlock.toFloat()).coerceIn(0f, 1f)
    val hasDpk = dpk >= tier.requiredDpkToUnlock
    val hasCapital = internalCash >= tier.upgradeCost

    val canUnlockNow = !isUnlocked && hasDpk && hasCapital

    val accentColor = when (tier) {
        BankTier.TIER_1_MICRO -> BlueTerminal
        BankTier.TIER_2_RETAIL -> EmeraldAccent
        BankTier.TIER_3_CORPORATE -> GoldAccent
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = NavyCardBg,
        border = BorderStroke(1.dp, if (isCurrent) accentColor else NavyCardSecondary),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        when (tier) {
                            BankTier.TIER_1_MICRO -> Icons.Default.Storefront
                            BankTier.TIER_2_RETAIL -> Icons.Default.Domain
                            BankTier.TIER_3_CORPORATE -> Icons.Default.LocationCity
                        },
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(tier.label, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(tier.description, color = SlateText, fontSize = 11.sp)
                    }
                }

                if (isCurrent) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = accentColor.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, accentColor)
                    ) {
                        Text("AKTIF", color = accentColor, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                } else if (isUnlocked) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldAccent, modifier = Modifier.size(20.dp))
                }
            }

            if (!isUnlocked) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = NavyCardSecondary)
                Spacer(modifier = Modifier.height(10.dp))

                Text("Syarat Lisensi & Kelayakan:", color = SlateText, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))

                // DPK Requirement Bar
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Minimal DPK: ${currencyFormat.format(tier.requiredDpkToUnlock)}", color = if (hasDpk) EmeraldAccent else SlateText, fontSize = 11.sp)
                    Text("${(dpkProgress * 100).toInt()}%", color = if (hasDpk) EmeraldAccent else SlateText, fontSize = 11.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { dpkProgress },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                    color = if (hasDpk) EmeraldAccent else GoldAccent,
                    trackColor = NavyCardSecondary
                )

                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Biaya Lisensi (Kas Internal):", color = SlateText, fontSize = 11.sp)
                    Text(currencyFormat.format(tier.upgradeCost), color = if (hasCapital) EmeraldAccent else CrimsonRed, fontWeight = FontWeight.Bold, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }

                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onUnlock,
                    enabled = canUnlockNow,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accentColor,
                        disabledContainerColor = SlateText.copy(alpha = 0.2f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (canUnlockNow) "Upgrade ke ${tier.label}" else "Syarat Belum Terpenuhi",
                        color = if (canUnlockNow) Color.Black else SlateText,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Capital Injection / Dividend Withdrawal Modal
 */
@Composable
private fun CapitalManagementDialog(
    mode: String,
    playerCash: Long,
    bankInternalCash: Long,
    currencyFormat: NumberFormat,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    var selectedAmount by remember { mutableStateOf(500_000L) }
    val maxAvailable = if (mode == "INJECT") playerCash else bankInternalCash

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NavyCardBg,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = GoldAccent)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (mode == "INJECT") "Suntik Modal Internal Bank" else "Tarik Dividen Kas Bank",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        },
        text = {
            Column {
                Text(
                    text = if (mode == "INJECT")
                        "Modal pemilik digunakan untuk cadangan penyerap rugi (loan loss reserve), infrastruktur IT, dan syarat ekspansi lisensi (TIDAK dicampur dengan DPK nasabah)."
                    else
                        "Tarik kas internal yang mengendap di bank ke dompet kas pribadi pemain.",
                    color = SlateText,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(if (mode == "INJECT") "Saldo Kas Pribadi:" else "Kas Internal Bank:", color = SlateText, fontSize = 11.sp)
                    Text(currencyFormat.format(maxAvailable), color = GoldAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text("Pilih Jumlah:", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))

                val options = listOf(100_000L, 500_000L, 1_000_000L, 5_000_000L, 10_000_000L)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    options.take(3).forEach { amt ->
                        FilterChip(
                            selected = selectedAmount == amt,
                            onClick = { selectedAmount = amt },
                            label = { Text("${amt / 1_000}K", fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GoldAccent,
                                selectedLabelColor = Color.Black
                            )
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    options.drop(3).forEach { amt ->
                        FilterChip(
                            selected = selectedAmount == amt,
                            onClick = { selectedAmount = amt },
                            label = { Text("${amt / 1_000_000}M", fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GoldAccent,
                                selectedLabelColor = Color.Black
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedAmount) },
                enabled = selectedAmount <= maxAvailable && selectedAmount > 0,
                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent)
            ) {
                Text(
                    text = if (mode == "INJECT") "Suntikkan Dana" else "Tarik Dana",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = SlateText)
            }
        }
    )
}
