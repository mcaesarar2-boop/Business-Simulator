package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.viewmodel.GameViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventOrganizerScreen(navController: NavHostController, viewModel: GameViewModel, instanceId: String) {
    val playerState by viewModel.playerState.collectAsState()
    val ownedData = playerState.ownedBusinesses.find { it.instanceId == instanceId }
        ?: playerState.holdingCompanies.flatMap { it.subsidiaries }.find { it.instanceId == instanceId }
        
    if (ownedData == null) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text("Bisnis tidak ditemukan.", color = Color.White)
            Button(onClick = { navController.popBackStack() }) { Text("Kembali") }
        }
        return
    }

    var showRentalMenu by remember { mutableStateOf(false) }
    var showCustomEventMenu by remember { mutableStateOf(false) }
    var selectedClientRequest by remember { mutableStateOf<com.example.data.EventProject?>(null) }
    
    var showTransferDialog by remember { mutableStateOf(false) }
    var transferType by remember { mutableStateOf("DEPOSIT") } // "DEPOSIT" atau "WITHDRAW"
    var transferAmountInput by remember { mutableStateOf("") }
    
    val activeEvents = ownedData.activeEvents
    val clientRequests = ownedData.clientEventRequests

    // LaunchedEffect for Active Events timer
    LaunchedEffect(activeEvents) {
        while(true) {
            val now = System.currentTimeMillis()
            var changed = false
            for (ev in activeEvents) {
                if (now >= ev.executionEndTime) {
                    viewModel.completeEventProject(instanceId, ev.id)
                    changed = true
                }
            }
            if (changed) break
            delay(1000)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(ownedData.customName ?: "Event Organizer Dashboard", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) { 
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali") 
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background, titleContentColor = Color.White)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Kas Internal Perusahaan", fontSize = 14.sp, color = Color.Gray)
                        Text("$${String.format("%,.0f", ownedData.companyCash)}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981)) // Green
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { transferType = "DEPOSIT"; transferAmountInput = ""; showTransferDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                modifier = Modifier.weight(1f)
                            ) { Text("Suntik Dana") }
                            Button(
                                onClick = { transferType = "WITHDRAW"; transferAmountInput = ""; showTransferDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF43F5E)),
                                modifier = Modifier.weight(1f)
                            ) { Text("Tarik Dana") }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        if (ownedData.level >= 10 && !ownedData.hasRentalDivision) {
                            Button(
                                onClick = { showRentalMenu = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)) // Purple
                            ) {
                                Text("Bentuk Divisi Rental ($100,000)")
                            }
                        } else if (ownedData.hasRentalDivision) {
                            Text("✅ Memiliki Divisi Rental In-House", color = Color(0xFF10B981), fontSize = 14.sp)
                        }
                    }
                }
            }
            
            item {
                Button(
                    onClick = { showCustomEventMenu = true },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)) // Blue
                ) {
                    Text("+ Bikin Acara Custom", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
            
            item {
                Text("Tawaran Acara Klien", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            if (clientRequests.isEmpty()) {
                item { Text("Belum ada tawaran. Tunggu bulan depan.", color = Color.Gray) }
            } else {
                items(clientRequests) { req ->
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF334155)), modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(req.name, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("Kategori: ${req.category}", color = Color.LightGray, fontSize = 12.sp)
                                Text("Pax: ${req.pax} | Budget: $${String.format("%,.0f", req.totalBudget)}", color = Color(0xFF10B981), fontSize = 12.sp)
                            }
                            Button(onClick = { selectedClientRequest = req }) {
                                Text("Ambil")
                            }
                        }
                    }
                }
            }
            
            item {
                Text("Acara Berjalan", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            if (activeEvents.isEmpty()) {
                item { Text("Tidak ada acara yang sedang berjalan.", color = Color.Gray) }
            } else {
                items(activeEvents) { ev ->
                    var timeLeftMs by remember { mutableStateOf(Math.max(0L, ev.executionEndTime - System.currentTimeMillis())) }
                    LaunchedEffect(ev, ev.executionEndTime) {
                        while(timeLeftMs > 0) {
                            delay(1000)
                            timeLeftMs = Math.max(0L, ev.executionEndTime - System.currentTimeMillis())
                        }
                    }
                    
                    val secondsLeft = timeLeftMs / 1000
                    val progress = 1f - (timeLeftMs.toFloat() / (5 * 60000f)).coerceIn(0f, 1f)
                    
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF475569)), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("🔴 Sedang Eksekusi: ${ev.name}", fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(progress = progress, modifier = Modifier.fillMaxWidth().height(8.dp), color = Color(0xFF3B82F6), trackColor = Color.DarkGray)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Waktu tersisa: ${secondsLeft}s", color = Color.LightGray, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }

    if (showRentalMenu) {
        AlertDialog(
            onDismissRequest = { showRentalMenu = false },
            title = { Text("Bentuk Divisi Rental") },
            text = { Text("Membentuk divisi rental membutuhkan biaya $100,000 dari Kas Pribadi (Bukan Kas Perusahaan). Dengan ini, kamu bisa memangkas biaya teknis event luar dan menikmati profit berlipat. Lanjut?") },
            confirmButton = {
                TextButton(onClick = {
                    val err = viewModel.formRentalDivision(instanceId)
                    if (err != null) {
                        // Normally show Toast, handled implicitly here for simplicity
                    }
                    showRentalMenu = false
                }) { Text("Bayar $100,000") }
            },
            dismissButton = { TextButton(onClick = { showRentalMenu = false }) { Text("Batal") } }
        )
    }

    if (selectedClientRequest != null) {
        val req = selectedClientRequest!!
        var useInHouse by remember { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { selectedClientRequest = null },
            title = { Text("Eksekusi Acara: ${req.name}") },
            text = {
                Column {
                    Text("Total Budget yang harus ditanggung: $${String.format("%,.0f", req.totalBudget)}")
                    Text("EO Fee (Profit Dasar): $${String.format("%,.0f", req.eoFee)}")
                    Text("Biaya Teknis/Sewa: $${String.format("%,.0f", req.techFee)}")
                    Spacer(modifier = Modifier.height(16.dp))
                    if (ownedData.hasRentalDivision) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = useInHouse, onCheckedChange = { useInHouse = it })
                            Text("Gunakan Divisi Rental Sendiri (Biaya teknis akan kembali jadi profit!)", fontSize = 14.sp)
                        }
                    } else {
                        Text("Kamu akan menggunakan Vendor Luar. Biaya teknis hangus.", color = Color.Gray, fontSize = 14.sp)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val err = viewModel.acceptClientEvent(instanceId, req.id, useInHouse)
                    selectedClientRequest = null
                }) { Text("Jalankan", fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { selectedClientRequest = null }) { Text("Batal") } }
        )
    }

    if (showCustomEventMenu) {
        var catName by remember { mutableStateOf("Wedding") }
        var eventName by remember { mutableStateOf("") }
        var paxStr by remember { mutableStateOf("") }
        var tbStr by remember { mutableStateOf("") }
        var useInHouse by remember { mutableStateOf(false) }
        var showError by remember { mutableStateOf<String?>(null) }
        
        val minBudgets = mapOf("Pensi" to 1000.0, "Wedding" to 2000.0, "Corporate" to 5000.0, "Konser Grade C" to 6000.0, "Konser Grade B" to 7000.0, "Konser Grade A" to 8000.0, "Konser Grade S" to 10000.0)
        
        AlertDialog(
            onDismissRequest = { showCustomEventMenu = false },
            title = { Text("Bikin Acara Custom") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    if (showError != null) Text(showError!!, color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
                    OutlinedTextField(value = eventName, onValueChange = { eventName = it }, label = { Text("Nama Acara") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = paxStr, onValueChange = { paxStr = it }, label = { Text("Estimasi Pax") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text("Pilih Kategori:", fontSize = 14.sp, color = Color.White)
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        minBudgets.keys.forEach { cat ->
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { catName = cat }) {
                                RadioButton(selected = catName == cat, onClick = { catName = cat })
                                Text("$cat (Min $${minBudgets[cat]})", fontSize = 14.sp)
                            }
                        }
                    }
                    
                    OutlinedTextField(value = tbStr, onValueChange = { tbStr = it }, label = { Text("Total Budget Proposal") }, modifier = Modifier.fillMaxWidth())
                    
                    if (ownedData.hasRentalDivision) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = useInHouse, onCheckedChange = { useInHouse = it })
                            Text("Pakai Alat & Tim Internal (+Profit)", fontSize = 14.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val pax = paxStr.toIntOrNull() ?: 0
                    val tb = tbStr.toDoubleOrNull() ?: 0.0
                    val minb = minBudgets[catName] ?: 0.0
                    if (eventName.isBlank() || pax <= 0) {
                        showError = "Isi nama acara dan pax dengan benar!"
                    } else if (tb < minb) {
                        showError = "Budget minimal untuk $catName adalah $$minb !"
                    } else {
                        val eoFee = tb * 0.20
                        val techFee = tb * 0.40
                        val err = viewModel.startCustomEvent(instanceId, eventName, catName, pax, tb, eoFee, techFee, useInHouse)
                        if (err == null) {
                            showCustomEventMenu = false
                        } else {
                            showError = err
                        }
                    }
                }) { Text("Ajukan Proposal") }
            },
            dismissButton = { TextButton(onClick = { showCustomEventMenu = false }) { Text("Batal") } }
        )
    }

    if (showTransferDialog) {
        val parentHolding = playerState.holdingCompanies.find { h -> h.subsidiaries.any { it.instanceId == instanceId } }
        val parentName = parentHolding?.name
        val titleText = if (transferType == "DEPOSIT") "Suntik Dana Modal" else "Tarik Dana"

        AlertDialog(
            onDismissRequest = { showTransferDialog = false },
            title = { Text(titleText) },
            text = {
                Column {
                    val parentCashDesc = if (parentHolding != null) String.format("%,.0f", parentHolding.holdingCash) else String.format("%,d", playerState.cash)

                    if (parentName != null) {
                        Text("Sumber/Tujuan Dana: Kas Internal $parentName")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Kas Induk ($parentName): $$parentCashDesc")
                    } else {
                        Text("Sumber/Tujuan Dana: Global Balance")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Global Balance: $$parentCashDesc")
                    }
                    Text("Kas Internal Bisnis: $${String.format("%,.0f", ownedData.companyCash)}")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = transferAmountInput,
                        onValueChange = { transferAmountInput = it },
                        label = { Text("Jumlah ($)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val amountStr = transferAmountInput.replace("[^0-9]".toRegex(), "")
                    val amountLong = amountStr.toLongOrNull() ?: 0L
                    if (amountLong > 0) {
                        if (transferType == "DEPOSIT") {
                            viewModel.injectCapitalToBusiness(instanceId, amountLong)
                        } else {
                            viewModel.withdrawCapitalFromBusiness(instanceId, amountLong)
                        }
                    }
                    showTransferDialog = false
                }) { Text("Transfer") }
            },
            dismissButton = { TextButton(onClick = { showTransferDialog = false }) { Text("Batal") } }
        )
    }
}
