package com.example.ui.lifestyle

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PlayerState
import com.example.viewmodel.GameViewModel

data class SubscriptionItem(
    val name: String,
    val cost: Long,
    val description: String,
    val iconEmoji: String
)

@Composable
fun DigitalSubscriptionTab(
    playerState: PlayerState,
    viewModel: GameViewModel
) {
    val items = listOf(
        SubscriptionItem("Spotify Premium", 10L, "Dengarkan musik resolusi tinggi tanpa gangguan.", "🎵"),
        SubscriptionItem("YouTube Premium", 15L, "Nonton offline & bebas iklan untuk video tech startup.", "📺"),
        SubscriptionItem("Disney+ & Netflix Bundle", 30L, "Paket hiburan film akhir pekan 4K HDR.", "🍿"),
        SubscriptionItem("Adobe Creative Cloud & CapCut Pro", 60L, "Aset pengeditan video startup marketing.", "🎨"),
        SubscriptionItem("Google One 30TB", 150L, "Penyimpanan cloud raksasa untuk data & sasis kecerdasan buatan.", "☁️"),
        SubscriptionItem("Apple TV+", 15L, "Katalog serial orisinal kualitas sinematik terbaik.", "🍏")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 8.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFF00FF00).copy(alpha = 0.2f), RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1B12))
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
                        text = "Total Biaya Subscription",
                        color = Color(0xFF90A4AE),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "$${viewModel.playerState.value.monthlyLifestyleCost} / bln",
                        color = Color(0xFF00FF00),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                Text(
                    text = "Auto-billing bulanan",
                    color = Color(0xFF90A4AE),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Katalog Langganan Digital",
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(items) { item ->
                val isActive = playerState.activeSubscriptions.contains(item.name)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF151921)),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 250.dp)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF232B36)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(item.iconEmoji, fontSize = 22.sp)
                                }
                                
                                Switch(
                                    checked = isActive,
                                    onCheckedChange = { viewModel.toggleSubscription(item.name, item.cost) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color(0xFF00FF00),
                                        checkedTrackColor = Color(0xFF0C3A1A)
                                    )
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = Color(0xFF232B36), thickness = 1.dp)
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Text(
                                text = item.name,
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 18.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$${item.cost}/bln",
                                color = if (isActive) Color(0xFF00FF00) else Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = item.description,
                                color = Color(0xFF90A4AE),
                                fontSize = 11.sp,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
