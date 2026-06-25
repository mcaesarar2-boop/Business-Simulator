package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ConstructionProject

@Composable
fun ConstructionDashboard(
    availableClientProjects: List<ConstructionProject>,
    activeTenders: List<ConstructionProject>,
    playerCash: Long,
    companyCash: Long,
    useShortFormat: Boolean,
    onStartTender: (String, Long, Int, Long, Boolean) -> Unit,
    onTakeClientProject: (String) -> Unit
) {
    var showTenderDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("🏗️ Mega Construction Firm", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            Text("Kas Internal: ${formatCurrencyRingkas(companyCash, useShortFormat)}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (activeTenders.isEmpty()) {
                Text("Tidak ada proyek berjalan.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            } else {
                Text("Proyek Berjalan:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                activeTenders.forEach { tender ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).background(MaterialTheme.colorScheme.background, RoundedCornerShape(8.dp)).padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            val parts = tender.name.split("|")
                            val displayName = if (parts.size >= 3) parts[0] else tender.name
                            Text(displayName, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                            Text("Nilai: ${formatCurrencyRingkas(tender.totalContractValue.toLong(), useShortFormat)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        }
                        if (tender.isFinished) {
                            Text("SELESAI", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFF00C853))
                        } else {
                            Text("${tender.remainingMonths} Bln", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color.LightGray)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Tawaran Klien Bulan Ini (B2B Market):", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            if (availableClientProjects.isEmpty()) {
                Text("Belum ada tawaran klien baru.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            } else {
                availableClientProjects.forEach { proj ->
                    val parts = proj.name.split("|")
                    val displayName = if (parts.size >= 3) parts[0] else proj.name
                    val baseBudget = if (parts.size >= 3) parts[1].toLongOrNull() ?: 0L else 0L
                    val marginStr = if (parts.size >= 3) {
                        val m = parts[2].toDoubleOrNull() ?: 0.0
                        "%.0f".format(m * 100) + "%"
                    } else ""
                    
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(displayName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            if (baseBudget > 0) {
                                Text("Total Budget: ${formatCurrencyRingkas(baseBudget, useShortFormat)}", fontSize = 12.sp, color = Color.Gray)
                                Text("Estimasi Laba Bersih (Margin $marginStr): ${formatCurrencyRingkas(proj.totalContractValue.toLong(), useShortFormat)}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            } else {
                                Text("Nilai: ${formatCurrencyRingkas(proj.totalContractValue.toLong(), useShortFormat)}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            }
                            Text("Durasi: ${proj.durationMonths} Bulan", fontSize = 12.sp, color = Color.Gray)
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { onTakeClientProject(proj.id) },
                                modifier = Modifier.fillMaxWidth().height(36.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("Ambil Proyek", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = { showTenderDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Ikuti Tender Megaproyek")
            }
        }
    }

    if (showTenderDialog) {
        TenderSelectionDialog(
            companyCash = companyCash,
            playerCash = playerCash,
            useShortFormat = useShortFormat,
            onDismiss = { showTenderDialog = false },
            onStart = { name, cv, dur, cost, useCompany ->
                onStartTender(name, cv, dur, cost, useCompany)
                showTenderDialog = false
            }
        )
    }
}

@Composable
fun TenderSelectionDialog(
    companyCash: Long,
    playerCash: Long,
    useShortFormat: Boolean,
    onDismiss: () -> Unit,
    onStart: (String, Long, Int, Long, Boolean) -> Unit
) {
    val options = listOf(
        Triple("Apartemen Premium", 10_000_000L, Triple(5_000_000L, 6, "Proyek menengah dengan ROI cepat.")),
        Triple("Jalan Tol Antar Kota", 50_000_000L, Triple(25_000_000L, 12, "Infrastruktur vital, butuh modal besar.")),
        Triple("Bandara Internasional", 250_000_000L, Triple(100_000_000L, 24, "Megaproyek raksasa dengan keuntungan masif."))
    )
    
    var selectedTender by remember { mutableStateOf<Int?>(null) }
    var useCompanyCash by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Lelang Tender Megaproyek", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Pilih megaproyek untuk dikerjakan. Anda harus menyetor Uang Jaminan sebesar modal awal. Keuntungan akan cair utuh setelah proyek selesai.")
                Spacer(modifier = Modifier.height(16.dp))
                
                Box(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(options.indices.toList()) { index ->
                            val opt = options[index]
                            val isSelected = selectedTender == index
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedTender = index }
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = isSelected, onClick = { selectedTender = index })
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(opt.first, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                    Text("Nilai: ${formatCurrencyRingkas(opt.second, useShortFormat)} | ${opt.third.second} Bulan", style = MaterialTheme.typography.labelSmall)
                                    Text("Modal Awal: ${formatCurrencyRingkas(opt.third.first, useShortFormat)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = useCompanyCash, onCheckedChange = { useCompanyCash = it })
                    Text("Gunakan Kas Perusahaan (${formatCurrencyRingkas(companyCash, useShortFormat)})", style = MaterialTheme.typography.bodySmall)
                }
                if (!useCompanyCash) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Spacer(modifier = Modifier.width(48.dp)) // Aligned to checkbox text
                        Text("Kas Pribadi: ${formatCurrencyRingkas(playerCash, useShortFormat)}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val sel = selectedTender
                    if (sel != null) {
                        val opt = options[sel]
                        onStart(opt.first, opt.second, opt.third.second, opt.third.first, useCompanyCash)
                    }
                },
                enabled = selectedTender != null
            ) {
                Text("Ikuti Tender")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
