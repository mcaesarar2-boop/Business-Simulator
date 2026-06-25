package com.example.ui.lifestyle

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PlayerState
import com.example.viewmodel.GameViewModel

data class GadgetItem(
    val name: String,
    val price: Long,
    val description: String,
    val iconEmoji: String
)

@Composable
fun TechGadgetsTab(
    playerState: PlayerState,
    viewModel: GameViewModel
) {
    val items = listOf(
        GadgetItem("Smartphone Lipat", 2000L, "Layar ganda fleksibel terkini untuk mobilitas level eksekutif.", "📱"),
        GadgetItem("Smartwatch Titanium", 1000L, "Pelacak kebugaran berlapis titanium dengan sinkronisasi satelit.", "⌚"),
        GadgetItem("Laptop Pribadi", 5000L, "Grafis termutakhir dengan prosesor kustom ultra hemat daya.", "💻"),
        GadgetItem("Computer Gaming Super", 15000L, "Pendingin cairan dual-loop dengan sasis pencahayaan RGB kustom.", "🖥️"),
        GadgetItem("Computer AI & Server", 45000L, "Server cluster modular mandiri berisi 4 kartu akselerator AI.", "🗄️")
    )

    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 8.dp)
    ) {
        if (snackbarMessage != null) {
            Snackbar(
                action = {
                    TextButton(onClick = { snackbarMessage = null }) {
                        Text("OK", color = Color(0xFF00FF00))
                    }
                },
                modifier = Modifier.padding(bottom = 12.dp),
                containerColor = Color(0xFF1E293B)
            ) {
                Text(snackbarMessage!!, color = Color.White, fontSize = 12.sp)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Katalog Gadget Premium & AI",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${playerState.ownedGadgets.size} Dimiliki",
                color = Color(0xFF00FF00),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(items) { item ->
                val isOwned = playerState.ownedGadgets.contains(item.name)
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
                                Text(
                                    text = "$${String.format("%,d", item.price)}",
                                    color = if (isOwned) Color(0xFF90A4AE) else Color(0xFF00FF00),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = item.name,
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 18.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = item.description,
                                color = Color(0xFF90A4AE),
                                fontSize = 11.sp,
                                lineHeight = 14.sp
                            )
                        }

                        Column {
                            Spacer(modifier = Modifier.height(12.dp))
                            if (isOwned) {
                                Button(
                                    onClick = {},
                                    enabled = false,
                                    colors = ButtonDefaults.buttonColors(
                                        disabledContainerColor = Color(0xFF1E2530),
                                        disabledContentColor = Color(0xFF455A64)
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(36.dp)
                                ) {
                                    Text("Dimiliki", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Button(
                                    onClick = {
                                        val success = viewModel.purchaseTechGadget(item.name, item.price)
                                        snackbarMessage = if (success) {
                                            "Berhasil membeli ${item.name} seharga $${String.format("%,d", item.price)}!"
                                        } else {
                                            "Kas Pribadi Anda kurang untuk membeli ${item.name}!"
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF00FF00),
                                        contentColor = Color.Black
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(36.dp)
                                ) {
                                    Text("Beli", fontSize = 11.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
