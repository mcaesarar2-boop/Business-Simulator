package com.example.ui

import com.example.viewmodel.GameViewModel

import com.example.data.*

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaxLegalScreen(navController: NavController, viewModel: GameViewModel) {
    val playerState by viewModel.playerState.collectAsState()
    val report = playerState.taxLegalReport
    
    val bgDark = Color(0xFF121212)
    val cardDark = Color(0xFF1E1E1E)
    val gold = Color(0xFFFFD700)
    val neonGreen = Color(0xFF39FF14)
    val textGray = Color(0xFFA0A0A0)
    val errorColor = MaterialTheme.colorScheme.error
    
    val format = remember { NumberFormat.getCurrencyInstance(Locale.US).apply { maximumFractionDigits = 0 } }
    
    val totalBusiness = playerState.ownedBusinesses.sumOf {
        val cat = com.example.data.getCatalogItem(it.catalogId, playerState)
        if (cat != null) com.example.data.getBusinessValuation(it, cat) else 0L
    }
    
    // Monthly Corporate Net Profit before taxes
    var megaHoldingMonthlyProfit = playerState.ownedBusinesses.sumOf {
        val ct = com.example.data.getCatalogItem(it.catalogId, playerState)
        if (ct != null) getBusinessStats(it, ct, playerState).let { (rev, mnt) -> rev - mnt } else 0L
    } + playerState.holdingCompanies.sumOf { h ->
        h.subsidiaries.sumOf { sub ->
            val ct = com.example.data.getCatalogItem(sub.catalogId, playerState)
            if (ct != null) getBusinessStats(sub, ct, playerState).let { (rev, mnt) -> rev - mnt } else 0L
        }
    }
    if (megaHoldingMonthlyProfit < 0) megaHoldingMonthlyProfit = 0L
    
    val activeCorpTaxRate = if (report.isTaxHavenActive) 0.05 else 0.20
    val estMonthlyCorpTax = (megaHoldingMonthlyProfit * activeCorpTaxRate).toLong()
    
    Scaffold(
        containerColor = bgDark,
        topBar = {
            TopAppBar(
                title = { Text("Holding Tax & Legal", color = gold, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = gold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bgDark)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }
            
            // Tax Section
            item {
                Text("Kepatuhan Pajak Badan (Corporate Tax)", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Mengelola perpajakan atas nama Entitas Korporas Mega Holding. Terpisah sepenuhnya dari keuangan saku pribadi CEO.", color = textGray, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(12.dp))
                
                Card(
                    colors = CardDefaults.cardColors(containerColor = cardDark),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Pajak PPh Badan Dibayar", color = textGray)
                            Text(format.format(playerState.corporateTaxPaid), color = neonGreen, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Estimasi Pendapatan Sebelum Pajak (Bulanan)", color = textGray)
                            Text(format.format(megaHoldingMonthlyProfit), color = Color.White, fontWeight = FontWeight.Medium)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Tarif PPh Badan Aktif", color = textGray)
                            Text("${(activeCorpTaxRate * 100).toInt()}%", color = if (report.isTaxHavenActive) gold else neonGreen, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Estimasi Pajak Badan Bulanan", color = textGray)
                            Text(format.format(estMonthlyCorpTax), color = errorColor, fontWeight = FontWeight.Bold)
                        }
                        
                        if (report.unpaidTaxes > 0) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Tunggakan/Denda Terhutang", color = textGray)
                                Text(format.format(report.unpaidTaxes), color = errorColor, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.payTaxesManually(report.unpaidTaxes) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = gold, contentColor = Color.Black),
                                enabled = playerState.cash >= report.unpaidTaxes
                            ) {
                                Text("Bayar Denda Terhutang")
                            }
                        }
                        if (report.frozenBusinessId != null) {
                            Spacer(Modifier.height(8.dp))
                            Surface(color = Color(0xFF550000), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    "🚨 OPERASIONAL DIBEKUKAN! Sebagian unit anak perusahaan disita oleh Direktur Jenderal Pajak akibat tunggakan terhutang yang melewati batas aman. Selesaikan tunggakan untuk memulihkan arus kas anak perusahaan.",
                                    color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }
                }
            }
            
            // Tax Haven Section
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = cardDark.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.AccountBalance, contentDescription = null, tint = gold, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Alihkan Dana ke Offshore (Tax Haven)", color = Color.White, fontWeight = FontWeight.Bold)
                            Text("Diskon pajak bulanan 60%, tapi ada risiko 5% tiap bulan terkena Audit Pajak Besar dengan denda 5x lipat dari taksiran pajak. Risiko tanggung sendiri!", color = textGray, fontSize = 12.sp)
                        }
                        Switch(
                            checked = report.isTaxHavenActive,
                            onCheckedChange = { viewModel.toggleTaxHaven() },
                            colors = SwitchDefaults.colors(checkedThumbColor = gold, checkedTrackColor = gold.copy(alpha = 0.5f))
                        )
                    }
                }
            }
            
            // Notary Section
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = cardDark.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = gold, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Jasa Notaris Otomatis", color = Color.White, fontWeight = FontWeight.Bold)
                            Text("Notaris mengurus pembayaran pajak tahunan otomatis dengan biaya ${format.format(12000)}/tahun.", color = textGray, fontSize = 12.sp)
                        }
                        Switch(
                            checked = report.hasNotary,
                            onCheckedChange = { viewModel.toggleNotary(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = gold, checkedTrackColor = gold.copy(alpha = 0.5f))
                        )
                    }
                }
            }
            
            // Legal Issues
            item {
                Text("Permasalahan Hukum (Legal)", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Daftar gugatan dan masalah hukum yang harus segera Anda selesaikan. Jika diabaikan, dapat berdampak buruk.", color = textGray, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            if (report.activeLawsuits.isEmpty()) {
                item {
                    Surface(
                        color = cardDark.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Tidak ada masalah hukum saat ini. Bisnis Anda aman.",
                            color = textGray,
                            modifier = Modifier.padding(20.dp),
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                items(report.activeLawsuits) { lawsuit ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = cardDark),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = errorColor)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(lawsuit.title, color = errorColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(lawsuit.description, color = textGray, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            val scale = lawsuit.scaleFactor
                            val topLayerCost = (scale * 0.4).toLong()
                            val internCost = (scale * 0.1).toLong()
                            val settlementCost = (scale * 1.5).toLong()
                            
                            // Hire Premium Lawyer Button
                            Button(
                                onClick = { viewModel.resolveLawsuit(lawsuit.id, 2) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = gold, contentColor = Color.Black),
                                enabled = playerState.cash >= topLayerCost
                            ) {
                                Text("Sewa Firma Premium ($${format.format(topLayerCost)} | 95% Menang)")
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Hire Intern Lawyer Button
                            Button(
                                onClick = { viewModel.resolveLawsuit(lawsuit.id, 1) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = neonGreen, contentColor = Color.Black),
                                enabled = playerState.cash >= internCost
                            ) {
                                Text("Sewa Pengacara Magang ($${format.format(internCost)} | 40% Menang)")
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Pay damages only
                            OutlinedButton(
                                onClick = { viewModel.resolveLawsuit(lawsuit.id, 0) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                border = androidx.compose.foundation.BorderStroke(1.dp, textGray),
                                enabled = playerState.cash >= settlementCost
                            ) {
                                Text("Settle/Bayar Penuh Denda ($${format.format(settlementCost)})")
                            }
                        }
                    }
                }
            }
            
            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }
}
