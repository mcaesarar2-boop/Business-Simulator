package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.GameViewModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun RealEstateScreen(viewModel: GameViewModel) {
    val playerState by viewModel.playerState.collectAsState()
    val cash = playerState.cash
    val ownedProperties = playerState.ownedProperties
    val market by viewModel.realEstateMarket.collectAsState()
    
    val bgDark = Color(0xFF121212)
    val cardDark = Color(0xFF1E1E1E)
    val gold = Color(0xFFFFD700)
    val textGray = Color(0xFFA0A0A0)
    val neonGreen = Color(0xFF00FF00)
    
    var showBuyDialog by remember { mutableStateOf(false) }
    var selectedProperty by remember { mutableStateOf<com.example.data.PropertyItem?>(null) }
    
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale.US).apply { maximumFractionDigits = 0 } }
    
    val totalRentalIncome = ownedProperties.sumOf { owned ->
        market.find { it.id == owned.propertyId }?.baseRentalIncome ?: 0L
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
    ) {
        Column {
            Text(currencyFormat.format(totalRentalIncome), color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Pendapatan sewa per bulan", color = textGray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(24.dp))
        }
        
        Text("Properti Saya", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        if (ownedProperties.isEmpty()) {
            Text("Anda belum memiliki properti.", color = textGray)
        }
        Spacer(modifier = Modifier.height(16.dp))
        
        ownedProperties.forEach { owned ->
            val prop = market.find { it.id == owned.propertyId }
            if (prop != null) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = cardDark)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = RoundedCornerShape(8.dp), color = Color.DarkGray, modifier = Modifier.size(50.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.HomeWork, contentDescription = null, tint = Color.White)
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(prop.name, color = Color.White, fontWeight = FontWeight.Bold)
                            Text(prop.location, color = textGray, fontSize = 12.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("${currencyFormat.format(prop.baseRentalIncome)}/bln", color = neonGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Text("Pasar Properti", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        
        market.forEach { prop ->
            val isOwned = ownedProperties.any { it.propertyId == prop.id }
            if (!isOwned) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = cardDark)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = RoundedCornerShape(8.dp), color = Color.DarkGray, modifier = Modifier.size(50.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.RealEstateAgent, contentDescription = null, tint = gold)
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(prop.name, color = Color.White, fontWeight = FontWeight.Bold)
                                Text(prop.location, color = textGray, fontSize = 12.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text("Harga", color = textGray, fontSize = 12.sp)
                                Text(currencyFormat.format(prop.basePrice), color = Color.White, fontWeight = FontWeight.Medium)
                            }
                            Column {
                                Text("Sewa/bln", color = textGray, fontSize = 12.sp)
                                Text(currencyFormat.format(prop.baseRentalIncome), color = neonGreen, fontWeight = FontWeight.Medium)
                            }
                            Button(
                                onClick = { selectedProperty = prop; showBuyDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = gold, contentColor = Color.Black),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Beli", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
    
    if (showBuyDialog && selectedProperty != null) {
        val prop = selectedProperty!!
        AlertDialog(
            onDismissRequest = { showBuyDialog = false },
            containerColor = cardDark,
            title = { Text("Beli Properti", color = Color.White) },
            text = {
                Column {
                    Text("${prop.name} di ${prop.location}", color = textGray)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Harga: ${currencyFormat.format(prop.basePrice)}", color = Color.White)
                    Text("Saldo Anda: ${currencyFormat.format(cash)}", color = if (cash >= prop.basePrice) neonGreen else Color.Red)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.buyProperty(prop.id)
                        showBuyDialog = false
                    },
                    enabled = cash >= prop.basePrice,
                    colors = ButtonDefaults.buttonColors(containerColor = neonGreen, contentColor = bgDark)
                ) {
                    Text("Konfirmasi Beli")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBuyDialog = false }) {
                    Text("Batal", color = textGray)
                }
            }
        )
    }
}
