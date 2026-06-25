package com.example.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.HealthcareUnit
import com.example.data.OwnedBusiness

@Composable
fun HealthcareDashboard(
    units: List<HealthcareUnit>,
    playerCash: Long,
    useShortFormat: Boolean,
    constructionVendors: List<OwnedBusiness>,
    onBuildUnit: (String, String, String?, Int) -> Unit
) {
    var showBuildDialog by remember { mutableStateOf(false) }
    var selectedUnit by remember { mutableStateOf<HealthcareUnit?>(null) }

    val totalPatients = units.filter { it.type == "HOSPITAL" || it.type == "CLINIC" }.sumOf { it.members }
    val totalInsured = units.filter { it.type == "INSURANCE" }.sumOf { it.members }
    val combinedCash = units.sumOf { it.unitCash }
    val activeUnitsCount = units.size

    if (selectedUnit != null) {
        HealthcareUnitDetailScreen(
            unit = selectedUnit!!,
            useShortFormat = useShortFormat,
            onClose = { selectedUnit = null },
            onUpgrade = { /* Will be handled by viewmodel later */ }
        )
        return
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Healthcare & Protection Group", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        // TRANSPARENCY HEADER
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Dashboard Transparansi Agregat", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Unit Aktif", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text("$activeUnitsCount Unit Terdaftar", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Total Kas Gabungan", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text(formatCurrencyRingkas(combinedCash.toLong(), useShortFormat), fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color(0xFF00796B))
                    }
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Card(modifier = Modifier.weight(1f).padding(end = 8.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Favorite, contentDescription = "Patients", tint = MaterialTheme.colorScheme.onTertiaryContainer)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Total Pasien", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onTertiaryContainer)
                    }
                    Text(formatCurrencyRingkas(totalPatients, useShortFormat).replace("$", ""), fontSize = 20.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onTertiaryContainer)
                }
            }
            Card(modifier = Modifier.weight(1f).padding(start = 8.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = "Insurance", tint = MaterialTheme.colorScheme.onSecondaryContainer)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Member Asuransi", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                    Text(formatCurrencyRingkas(totalInsured, useShortFormat).replace("$", ""), fontSize = 20.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSecondaryContainer)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Daftar Unit Medis", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        if (units.isEmpty()) {
            Text("Belum ada unit medis yang dibangun.", color = Color.Gray, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
        } else {
            units.forEach { unit ->
                val icon = when (unit.type) {
                    "HOSPITAL" -> Icons.Default.LocalHospital
                    "INSURANCE" -> Icons.Default.Security
                    else -> Icons.Default.MedicalServices
                }
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { selectedUnit = unit },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)) // Glassmorphism-ish
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp)) {
                            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.padding(12.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(unit.name, fontWeight = FontWeight.Bold)
                            val statusColor = if (unit.isUpgrading) Color(0xFFA66400) else Color.Gray
                            if (unit.isUpgrading) {
                                Text("🚧 Sedang Dibangun (Sisa ${unit.upgradeDelayMonths} bln)", fontSize = 12.sp, color = statusColor)
                            } else {
                                if (unit.type == "INSURANCE") {
                                    Text("Tier: ${unit.tierCategory} | Member: ${formatCurrencyRingkas(unit.members, useShortFormat).replace("$", "")}", fontSize = 12.sp, color = statusColor)
                                } else {
                                    Text("Level ${unit.level} | Kapasitas: ${formatCurrencyRingkas(unit.members, useShortFormat).replace("$", "")}", fontSize = 12.sp, color = statusColor)
                                }
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Kas Internal", fontSize = 10.sp, color = Color.Gray)
                            Text(formatCurrencyRingkas(unit.unitCash.toLong(), useShortFormat), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00796B))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { showBuildDialog = true }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Bangun Unit Baru")
        }

        if (showBuildDialog) {
            var selectedType by remember { mutableStateOf("HOSPITAL") }
            var customName by remember { mutableStateOf("") }
            var selectedVendor by remember { mutableStateOf<String?>(null) }
            var hospitalLevel by remember { mutableStateOf(1f) }

            AlertDialog(
                onDismissRequest = { showBuildDialog = false },
                title = { Text("Bangun Unit Kesehatan") },
                text = {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        Text("Tipe Unit:")
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = selectedType == "HOSPITAL", onClick = { selectedType = "HOSPITAL" })
                            Text("Rumah Sakit")
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = selectedType == "INSURANCE", onClick = { selectedType = "INSURANCE" })
                            Text("Asuransi Kesehatan ($2.00M)")
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = selectedType == "CLINIC", onClick = { selectedType = "CLINIC" })
                            Text("Klinik Pratama ($150k)")
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = customName,
                            onValueChange = { customName = it },
                            label = { Text("Nama Unit") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (selectedType == "HOSPITAL") {
                            Spacer(modifier = Modifier.height(16.dp))
                            val cost = 500_000L + (hospitalLevel.toInt() * 100_000L)
                            Text("Level RS: ${hospitalLevel.toInt()} (Biaya: ${formatCurrencyRingkas(cost, useShortFormat)})")
                            Slider(value = hospitalLevel, onValueChange = { hospitalLevel = it }, valueRange = 1f..50f)
                        }
                        
                        if (selectedType == "HOSPITAL" || selectedType == "CLINIC") {
                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Pilih Kontraktor Pembangunan:")
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = selectedVendor == null, onClick = { selectedVendor = null })
                                Text("Vendor Eksternal (Instan)")
                            }
                            if (constructionVendors.isNotEmpty()) {
                                constructionVendors.forEach { vendor ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        RadioButton(selected = selectedVendor == vendor.instanceId, onClick = { selectedVendor = vendor.instanceId })
                                        Column {
                                            Text("In-House: ${vendor.customName ?: "PT Konstruksi"}", fontSize = 14.sp)
                                            Text("Profit 40%, Waktu 3 Bulan", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    val cost = when (selectedType) {
                        "HOSPITAL" -> 500_000L + (hospitalLevel.toInt() * 100_000L)
                        "INSURANCE" -> 2_000_000L
                        "CLINIC" -> 150_000L
                        else -> 0L
                    }
                    Button(
                        onClick = {
                            val finalName = customName.ifBlank {
                                when (selectedType) {
                                    "HOSPITAL" -> "RS Umum"
                                    "INSURANCE" -> "Asuransi Jiwa"
                                    "CLINIC" -> "Klinik Sehat"
                                    else -> "Unit Medis"
                                }
                            }
                            onBuildUnit(finalName, selectedType, selectedVendor, hospitalLevel.toInt())
                            showBuildDialog = false
                        },
                        enabled = playerCash >= cost
                    ) {
                        Text("Bangun")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showBuildDialog = false }) { Text("Batal") }
                }
            )
        }
    }
}

@Composable
fun HealthcareUnitDetailScreen(
    unit: HealthcareUnit,
    useShortFormat: Boolean,
    onClose: () -> Unit,
    onUpgrade: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text("Detail: ${unit.name}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Status Keuangan Unit", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("Kas Internal (Unit Cash)")
                    Text(formatCurrencyRingkas(unit.unitCash.toLong(), useShortFormat), fontWeight = FontWeight.Black, color = Color(0xFF00796B))
                }
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("Pendapatan Bulan Lalu")
                    Text(formatCurrencyRingkas(unit.monthlyRevenue.toLong(), useShortFormat))
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (unit.type == "HOSPITAL") {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Operasional Rumah Sakit", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Level Fasilitas: ${unit.level}")
                    Text("Kapasitas Pasien Aktif: ${formatCurrencyRingkas(unit.members, useShortFormat).replace("$", "")}")
                    Text("Estimasi Valuasi Aset: ${formatCurrencyRingkas(unit.level * 2_000_000L, useShortFormat)}")
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val upgradeCost = 100_000.0 * unit.level
                    Button(
                        onClick = onUpgrade,
                        enabled = !unit.isUpgrading && unit.unitCash >= upgradeCost,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Upgrade Fasilitas (Biaya: ${formatCurrencyRingkas(upgradeCost.toLong(), useShortFormat)})")
                    }
                    if (unit.unitCash < upgradeCost) {
                        Text("Kas internal unit tidak cukup untuk upgrade.", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }
                }
            }
        } else if (unit.type == "INSURANCE") {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Manajemen Risiko Asuransi", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Tier Asuransi: ${unit.tierCategory}")
                    Text("Total Member Aktif: ${formatCurrencyRingkas(unit.members, useShortFormat).replace("$", "")}")
                    
                    val riskLevel = when (unit.tierCategory) {
                        "PREMIUM" -> "Tinggi (8%)"
                        "ELITE" -> "Sangat Tinggi (12%)"
                        else -> "Sedang (5%)"
                    }
                    Text("Probabilitas Klaim Masif: $riskLevel", color = MaterialTheme.colorScheme.error)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val upgradeCost = when (unit.tierCategory) {
                        "BASIC" -> 1_500_000.0
                        "PREMIUM" -> 5_000_000.0
                        else -> 0.0
                    }
                    
                    if (unit.tierCategory != "ELITE") {
                        Button(
                            onClick = onUpgrade,
                            enabled = !unit.isUpgrading && unit.unitCash >= upgradeCost,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Upgrade Tier (Biaya: ${formatCurrencyRingkas(upgradeCost.toLong(), useShortFormat)})")
                        }
                        if (unit.unitCash < upgradeCost) {
                            Text("Kas internal unit tidak cukup untuk upgrade.", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                        }
                    } else {
                        Text("Asuransi telah mencapai Tier tertinggi (ELITE).", fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, color = Color.Gray)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(onClick = { /* Suntik dana dari parent, handled later if provided */ }, modifier = Modifier.fillMaxWidth()) {
            Text("Suntik Dana dari Global Balance (Kas Induk) ->")
        }
        Text("Pemain dapat menyuntik dana jika unit membutuhkan modal darurat untuk klaim/upgrade.", fontSize = 11.sp, color = Color.Gray)
    }
}
