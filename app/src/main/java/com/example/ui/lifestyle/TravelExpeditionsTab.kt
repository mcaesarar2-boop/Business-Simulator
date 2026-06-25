package com.example.ui.lifestyle

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PlayerState
import com.example.viewmodel.GameViewModel

data class TravelDestination(
    val name: String,
    val price: Long,
    val description: String,
    val iconEmoji: String,
    val statusText: String
)

@Composable
fun TravelExpeditionsTab(
    playerState: PlayerState,
    viewModel: GameViewModel
) {
    val items = listOf(
        TravelDestination("Couples Private Getaway", 50000L, "Resor pulau tropis terpencil ultra mewah dengan pelayan pribadi 24 jam.", "🏝️", "Sempurna untuk meredakan ketegangan board meeting."),
        TravelDestination("First-Class Europe Trip", 120000L, "Terbang first-class ke 5 ibu kota monarki Eropa & menginap di istana kastel orisinal.", "🏰", "Destinasi sosial selebritas elit global."),
        TravelDestination("Multi-Country Overland Expedition", 300000L, "Perjalanan konvoi helikopter kustom menyusuri dataran tinggi bersalju & gurun murni.", "🚁", "Simbol utama kepuasan petualangan petinggi holding.")
    )

    var lastTripResult by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 8.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFF2196F3).copy(alpha = 0.2f), RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF111E2E))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Total Perjalanan Liburan",
                        color = Color(0xFF90A4AE),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${playerState.travelHistory} Perjalanan",
                        color = Color(0xFF2196F3),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                Text(
                    text = "Aktivitas Healing CEO",
                    color = Color(0xFF90A4AE),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (lastTripResult != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2315))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "✈️ Catatan Perjalanan Terakhir",
                        color = Color(0xFFFFD700),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = lastTripResult!!,
                        color = Color.White,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        Text(
            text = "Eksplorasi Perjalanan Eksklusif",
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(items) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF151921)),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Grand hero picture placeholder
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = when (item.name) {
                                            "Couples Private Getaway" -> listOf(Color(0xFF004D40), Color(0xFF00796B))
                                            "First-Class Europe Trip" -> listOf(Color(0xFF311B92), Color(0xFF512DA8))
                                            else -> listOf(Color(0xFFE65100), Color(0xFFF57C00))
                                        }
                                    )
                                )
                        ) {
                            // Dark gradient overlay for text readability
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                                        )
                                    )
                            )

                            // Tag overlay top-left
                            Text(
                                text = "👑 DESTINASI IMPIAN",
                                color = Color(0xFFFFD700),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(14.dp)
                                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )

                            // Emoji container top-right
                            Box(
                                modifier = Modifier
                                    .padding(14.dp)
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.15f))
                                    .align(Alignment.TopEnd),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(item.iconEmoji, fontSize = 22.sp)
                            }

                            // Floating titles
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = item.name,
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "$${String.format("%,d", item.price)}",
                                    color = Color(0xFF00FF00),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }

                        // Bottom info area
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = item.description,
                                color = Color(0xFFECEFF1),
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "💡 ${item.statusText}",
                                color = Color(0xFFFFD700).copy(alpha = 0.9f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Button(
                                onClick = {
                                    val success = viewModel.goOnTravelExpedition(item.name, item.price)
                                    if (success) {
                                        lastTripResult = "Anda baru saja menyelesaikan ekspedisi '${item.name}'! Perjalanan mewah ini memulihkan kesehatan eksekutif Anda secara total. Pelayanan kelas dunia yang mengesankan!"
                                    } else {
                                        lastTripResult = "Gagal memproses perjalanan: Kas Pribadi Anda kurang sebesar $${String.format("%,d", item.price - playerState.privateBalance)} untuk pergi ke ${item.name}."
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF2196F3),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                ) {
                                    Text("Pergi Liburan", fontSize = 13.sp, fontWeight = FontWeight.Black)
                                }
                        }
                    }
                }
            }
        }
    }
}
