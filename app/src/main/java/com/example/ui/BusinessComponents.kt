package com.example.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BusinessCatalogItem
import com.example.data.BusinessCategory
import com.example.data.OwnedBusiness

data class SectorVisuals(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: Color
)

fun getSectorVisuals(sector: String): SectorVisuals {
    return when (sector.uppercase()) {
        "PROPERTY", "REAL_ESTATE" -> SectorVisuals(Icons.Default.Home, Color(0xFF0D47A1))
        "FINANCE", "BANKING" -> SectorVisuals(Icons.Default.AccountBalance, Color(0xFF1B5E20))
        "AVIATION", "TRANSPORT" -> SectorVisuals(Icons.Default.Send, Color(0xFF03A9F4))
        "CONSUMER", "RETAIL" -> SectorVisuals(Icons.Default.ShoppingCart, Color(0xFFFF9800))
        "MINING", "ENERGY", "BASIC_MATERIALS" -> SectorVisuals(Icons.Default.Build, Color(0xFF424242))
        else -> SectorVisuals(Icons.Default.BusinessCenter, Color(0xFF37474F))
    }
}

@Composable
fun getSectorIcon(sector: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (sector.uppercase(java.util.Locale.US)) {
        "CULINARY", "RESTAURANT" -> androidx.compose.material.icons.Icons.Default.Restaurant
        "ENTERTAINMENT", "MEDIA" -> androidx.compose.material.icons.Icons.Default.Movie
        "RETAIL", "E_COMMERCE" -> androidx.compose.material.icons.Icons.Default.ShoppingCart
        "PROPERTY", "CONSTRUCTION", "REAL_ESTATE" -> androidx.compose.material.icons.Icons.Default.LocationCity
        "HEALTHCARE", "HOSPITAL" -> androidx.compose.material.icons.Icons.Default.LocalHospital
        "AVIATION", "TRANSPORT" -> androidx.compose.material.icons.Icons.Default.Flight
        "EVENT", "EXHIBITION" -> androidx.compose.material.icons.Icons.Default.Event
        "FINANCE", "BANKING" -> androidx.compose.material.icons.Icons.Default.AccountBalance
        else -> androidx.compose.material.icons.Icons.Default.BusinessCenter
    }
}

val com.example.data.HoldingCompany.type: String
    get() {
        val typesList = listOf("Entertainment Holdings", "F&B Holdings", "Property Holdings", "Retail Holdings", "Tech Holdings", "Finance Holdings", "Daycare Holdings", "Transportation Holdings")
        typesList.forEach { if (name.contains(it, ignoreCase = true)) return it }
        return typesList[kotlin.math.abs(instanceId.hashCode()) % typesList.size]
    }

fun getHoldingBackgroundImage(type: String?): String {
    val safeType = type ?: ""
    return when (safeType) {
        "Entertainment Holdings" -> "https://images.unsplash.com/photo-1476242906366-d8eb64c2f661?q=80&w=1769&auto=format&fit=crop"
        "F&B Holdings" -> "https://images.unsplash.com/photo-1682142882978-c19f975d2407?q=80&w=1170&auto=format&fit=crop"
        "Property Holdings" -> "https://images.unsplash.com/photo-1560518883-ce09059eeffa?q=80&w=1073&auto=format&fit=crop"
        "Retail Holdings" -> "https://plus.unsplash.com/premium_photo-1683141052679-942eb9e77760?q=80&w=1170&auto=format&fit=crop"
        "Tech Holdings" -> "https://images.unsplash.com/photo-1488590528505-98d2b5aba04b?q=80&w=1170&auto=format&fit=crop"
        "Finance Holdings" -> "https://plus.unsplash.com/premium_photo-1681487769650-a0c3fbaed85a?q=80&w=1255&auto=format&fit=crop"
        "Daycare Holdings" -> "https://images.unsplash.com/photo-1511632765486-a01980e01a18?q=80&w=1170&auto=format&fit=crop"
        "Transportation Holdings" -> "https://images.unsplash.com/photo-1591768793355-74d04bb6608f?q=80&w=1172&auto=format&fit=crop"
        else -> "https://images.unsplash.com/photo-1486406146926-c627a92ad1ab"
    }
}

@Composable
fun HoldingItemCard(
    holding: com.example.data.HoldingCompany,
    rev: Long,
    useShortFormat: Boolean,
    onClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        Surface(
            modifier = Modifier.fillMaxWidth().clickable { onClick() },
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(16.dp),
            shadowElevation = 2.dp
        ) {
            Box(modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 140.dp)) {
                // Layer 1: Background Image
                val finalUrl = getHoldingBackgroundImage(holding.type)
                coil.compose.AsyncImage(
                    model = finalUrl,
                    contentDescription = null,
                    modifier = Modifier.matchParentSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )

                // Layer 2: Gradient Overlay (Hitam Transparan ke Hitam Pekat)
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(Color.Black.copy(alpha = 0.6f), Color.Black.copy(alpha = 0.95f))
                            )
                        )
                )

                // Layer 3: Text & Star Icon
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = Color(0xFFFFD700).copy(alpha = 0.2f),
                            shape = CircleShape,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = holding.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                            Text(
                                text = holding.type,
                                fontSize = 12.sp,
                                color = Color.LightGray
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${holding.subsidiaries.size} Anak Perusahaan",
                                fontSize = 12.sp,
                                color = Color(0xFFE0E0E0),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    Text(
                        text = "+$${com.example.ui.formatCurrencyRingkas(rev, useShortFormat)}/bln",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                }
            }
        }
    }
}

@Composable
fun getFilmStatusColor(status: String): Color {
    val s = status.lowercase()
    return when {
        s.contains("tayang") -> Color(0xFF4CAF50) // Hijau terang
        s.contains("pra-produksi") || s.contains("antrean") -> Color.Gray
        s.contains("syuting") || s.contains("produksi animasi") -> Color(0xFFFFC107) // Kuning Amber
        s.contains("pasca produksi") || s.contains("qc") || s.contains("poles") -> Color(0xFF64FFDA) // Biru/hijau muda
        s.contains("produksi") -> Color(0xFFFFC107)
        s.contains("menunggu") -> Color.LightGray // Abu-abu
        else -> Color.White
    }
}

@Composable
fun BusinessItemCard(
    owned: OwnedBusiness,
    catalogItem: BusinessCatalogItem,
    rev: Long,
    useShortFormat: Boolean,
    stockSector: String? = null,
    onClick: () -> Unit
) {
    var isPreviewExpanded by remember { mutableStateOf(false) }
    
    // Safe Variables (Null-Safety)
    val safeChildCount = owned.subsidiaries?.size ?: 0
    val safeFilms = owned.projectHistory ?: emptyList()
    val safeCustomName = owned.customName ?: catalogItem.name
    val safeStudioType = owned.studioType ?: "LIVE_ACTION"
    
    val isAcquired = owned.acquiredStockTicker != null

    val cardIcon: androidx.compose.ui.graphics.vector.ImageVector
    val sectorText: String

    if (isAcquired && stockSector != null) {
        cardIcon = getSectorIcon(stockSector)
        sectorText = "Sektor: ${stockSector ?: "General"}"
    } else {
        cardIcon = getSectorIcon(catalogItem.category.name)
        sectorText = if (catalogItem.id == "media_production") {
            if (safeStudioType == "ANIMATION") "Sektor: ${catalogItem.category.name} • Animation Studio" else "Sektor: ${catalogItem.category.name} • Live-Action Studio"
        } else "Sektor: ${catalogItem.category.name}"
    }

    Box(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = !owned.isUpgrading) { onClick() },
            color = if (owned.isUpgrading) Color.LightGray.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(16.dp),
            shadowElevation = 2.dp
        ) {
            Box(modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 140.dp)) {
                // Layer 1: Background Image
                val fallbackImage = "https://images.unsplash.com/photo-1486406146926-c627a92ad1ab?q=80&w=1470&auto=format&fit=crop"
                val finalUrl = catalogItem.imageUrl ?: fallbackImage
                coil.compose.AsyncImage(
                    model = finalUrl,
                    contentDescription = null,
                    modifier = Modifier.matchParentSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )

                // Layer 2: Gradient Overlay
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(Color.Black.copy(alpha = 0.65f), Color.Black.copy(alpha = 0.9f), Color.Black)
                            )
                        )
                )

                // Layer 3: Konten Teks & Icon
                Column(modifier = Modifier.fillMaxWidth().align(Alignment.BottomStart)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = if (catalogItem.id == "media_production") 0.dp else 16.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                    // Left: Icon
                    Surface(
                        color = if (owned.isUpgrading) Color.Gray else MaterialTheme.colorScheme.primary,
                        shape = CircleShape,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                cardIcon,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    // Middle: Content
                    Column(
                        modifier = Modifier.weight(1f).padding(vertical = 4.dp),
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Text(
                            text = safeCustomName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        
                        Text(
                            text = sectorText,
                            fontSize = 12.sp,
                            color = Color.LightGray
                        )
                        
                        if (catalogItem.id == "fine_dining") {
                            Text(
                                text = "Total Cabang: $safeChildCount",
                                fontSize = 12.sp,
                                color = Color(0xFFFFD700),
                                fontWeight = FontWeight.Medium
                            )
                        }
                        
                        if (owned.isUpgrading) {
                            Text(
                                text = "🚧 Dikonstruksi (${owned.upgradeDelayMonths} bln)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFD700)
                            )
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Lvl ${owned.level}",
                                    fontSize = 12.sp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${owned.purchasedUpgrades.size}/${catalogItem.upgrades.size} Upg",
                                    fontSize = 12.sp,
                                    color = Color.White
                                )
                            }
                            
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = if (rev == 0L) "$0" else formatCurrencyRingkas(rev, useShortFormat),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFFFD700)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (rev == 0L) "Pending" else "/bln",
                                    fontSize = 10.sp,
                                    color = Color.LightGray,
                                    modifier = Modifier.padding(bottom = 2.dp)
                                )
                            }
                        }
                        
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Right (Arrow)
                    Icon(
                        Icons.Default.KeyboardArrowRight,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }
                
                // Live Preview for Movie Studio
                if (catalogItem.id == "media_production") {
                    val films = safeFilms.filter { it.status == "IN_PRODUCTION" || it.status == "IN_THEATERS" }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.animateContentSize(
                                animationSpec = tween(
                                    durationMillis = 400,
                                    easing = FastOutSlowInEasing
                                )
                            )
                        ) {
                            if (films.isEmpty()) {
                                Text("Tidak ada proyek film yang sedang diproduksi.", color = Color.Gray, fontSize = 11.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                            } else {
                                val displayFilms = if (isPreviewExpanded) films else films.take(2)
                                displayFilms.forEach { film ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(text = film.title, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                                            val statusText = if (film.status == "IN_PRODUCTION") {
                                                if (film.isAwaitingRelease) "Menunggu Rilis" else if (film.isQcPhase) "Fase QC Internal" else if (film.productionPhase == "ANTREAN") "Menunggu Jadwal" else film.productionPhase
                                            } else "Tayang"
                                            Text(text = statusText, color = getFilmStatusColor(statusText), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                        }
                                        val rightInfo = if (film.status == "IN_THEATERS") if (film.currentRevenue == 0L) "$0" else formatCurrencyRingkas(film.currentRevenue, useShortFormat) else "Sisa: ${film.productionDelayMonths} bln"
                                        Text(text = rightInfo, color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                }
                                if (films.size > 2) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth().clickable { isPreviewExpanded = !isPreviewExpanded },
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = if (isPreviewExpanded) "Tutup Detail" else "Lihat Semua (${films.size})", color = Color.Yellow.copy(alpha = 0.8f), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                        Icon(
                                            imageVector = if (isPreviewExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                            contentDescription = null,
                                            tint = Color.Yellow.copy(alpha = 0.8f),
                                            modifier = Modifier.size(16.dp).padding(start = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            }
        }

        
        // Notification Badge (Red Circle) - Always on Finance as requested dummy
        if (catalogItem.category == BusinessCategory.FINANCE) {
            Surface(
                color = MaterialTheme.colorScheme.error, // Red
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 8.dp, y = (-8).dp)
            ) {
                Text(
                    text = "!", // Required dummy
                    color = MaterialTheme.colorScheme.onError,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
fun FilmProductionForm(
    owned: OwnedBusiness,
    playerCash: Long,
    useShortFormat: Boolean,
    currentMonth: Int,
    currentYear: Int,
    onProduce: (title: String, budget: Long, promoBudget: Long, genres: List<String>, isGlobal: Boolean, schedMonth: Int?, schedYear: Int?, filmFormat: String, productionFocus: String) -> Boolean,
    onPolish: (title: String, budgetCost: Long, extraMonths: Int) -> Unit = { _,_,_ -> },
    onSchedule: (title: String, schedStr: String) -> Unit = { _,_ -> },
    onCancel: (title: String, refundAmount: Long) -> Unit = { _,_ -> },
    onOpenHistory: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var title by remember { mutableStateOf("") }
    var budgetInput by remember { mutableStateOf("") }
    var promoBudgetInput by remember { mutableStateOf("") }
    val type = owned.studioType ?: "LIVE_ACTION"
    var selectedGenres by remember { mutableStateOf(if (type == "ANIMATION") setOf("Animation") else setOf<String>()) }
    var isGlobal by remember { mutableStateOf(false) }
    var selectedFormat by remember { mutableStateOf("Feature Film") }
    var selectedFocus by remember { mutableStateOf("REGULER") }
    
    var selectedSchedMonth by remember { mutableStateOf<Int?>(null) }
    var selectedSchedYear by remember { mutableStateOf<Int?>(null) }
    var showCalendarPicker by remember { mutableStateOf(false) }
    var activePolishProject by remember { mutableStateOf<com.example.data.MovieProject?>(null) }
    var activeScheduleProject by remember { mutableStateOf<com.example.data.MovieProject?>(null) }
    var filmToCancel by remember { mutableStateOf<com.example.data.MovieProject?>(null) }
    var showCancelDialog by remember { mutableStateOf(false) }
    
    val allGenres = listOf("Action", "Romance", "Sci-Fi", "Horror", "Comedy", "Drama", "Fantasy", "Animation", "Thriller", "Mystery")
    val canGlobal = owned.level >= 20
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(20.dp))
            .border(androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)), RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            val type = owned.studioType ?: "LIVE_ACTION"
            Text("🎬 Studio Mgt: Box Office Simulator", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Judul Film") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                    focusedContainerColor = Color.White.copy(alpha = 0.05f),
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color.Yellow,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color.Yellow
                )
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = budgetInput,
                onValueChange = { newValue ->
                    // Remove non-digits
                    val digits = newValue.filter { it.isDigit() }
                    if (digits.isEmpty()) {
                        budgetInput = ""
                    } else {
                        val parsed = digits.toLongOrNull()
                        if (parsed != null) {
                            // Format with commas
                            val formatter = java.text.NumberFormat.getNumberInstance(java.util.Locale.US)
                            budgetInput = formatter.format(parsed)
                        }
                    }
                },
                label = { Text("Production Budget (USD)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                singleLine = true,
                leadingIcon = { Text("$", modifier = Modifier.padding(start = 12.dp)) },
                shape = RoundedCornerShape(16.dp),
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                    focusedContainerColor = Color.White.copy(alpha = 0.05f),
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color.Yellow,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color.Yellow
                )
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = promoBudgetInput,
                onValueChange = { newValue ->
                    val digits = newValue.filter { it.isDigit() }
                    if (digits.isEmpty()) {
                        promoBudgetInput = ""
                    } else {
                        val parsed = digits.toLongOrNull()
                        if (parsed != null) {
                            val formatter = java.text.NumberFormat.getNumberInstance(java.util.Locale.US)
                            promoBudgetInput = formatter.format(parsed)
                        }
                    }
                },
                label = { Text("Promotion Budget (USD)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                singleLine = true,
                leadingIcon = { Text("$", modifier = Modifier.padding(start = 12.dp)) },
                shape = RoundedCornerShape(16.dp),
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                    focusedContainerColor = Color.White.copy(alpha = 0.05f),
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color.Yellow,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color.Yellow
                )
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            Text("Distribution Scale", fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = !isGlobal, onClick = { isGlobal = false })
                Text("Local Release")
                Spacer(modifier = Modifier.width(16.dp))
                RadioButton(selected = isGlobal, onClick = { if (canGlobal) isGlobal = true }, enabled = canGlobal)
                Text("Global (Hollywood)" + if(!canGlobal) " [Lvl 20+]" else "")
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            Text("Format Film", fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = selectedFormat == "Short Film", onClick = { selectedFormat = "Short Film" })
                Text("Short Film")
                Spacer(modifier = Modifier.width(16.dp))
                RadioButton(selected = selectedFormat == "Feature Film", onClick = { selectedFormat = "Feature Film" })
                Text("Feature Film")
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            Text("Demographics & Genres (Select 1-6)", fontWeight = FontWeight.Bold)
            @OptIn(ExperimentalLayoutApi::class)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                allGenres.forEach { genre ->
                    if (type == "LIVE_ACTION" && genre == "Animation") return@forEach
                    
                    val isAnimTag = type == "ANIMATION" && genre == "Animation"
                    FilterChip(
                        selected = selectedGenres.contains(genre),
                        onClick = {
                            if (isAnimTag) return@FilterChip // Locked
                            if (selectedGenres.contains(genre)) selectedGenres = selectedGenres - genre
                            else if (selectedGenres.size < 6) selectedGenres = selectedGenres + genre
                        },
                        label = { Text(genre) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            Text("Jadwalkan Produksi (Opsional)", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedButton(
                onClick = { showCalendarPicker = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (selectedSchedMonth != null && selectedSchedYear != null) {
                    val monthNames = listOf("Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Ags", "Sep", "Okt", "Nov", "Des")
                    val mIndex = (selectedSchedMonth!! - 1).coerceIn(0, 11)
                    val vYear = selectedSchedYear!! + 2019
                    Text("📅 Jadwal: ${monthNames[mIndex]} $vYear")
                } else {
                    Text("📅 Jadwal: Mulai Sekarang")
                }
            }
            
            if (showCalendarPicker) {
                val bookedSchedules = owned.projectHistory
                    .filter { it.productionPhase == "ANTREAN" && it.scheduledMonth != null && it.scheduledYear != null }
                    .map { Pair(it.scheduledMonth!!, it.scheduledYear!!) }
                    
                CalendarPickerDialog(
                    currentMonth = currentMonth,
                    currentYear = currentYear,
                    initialMonth = selectedSchedMonth,
                    initialYear = selectedSchedYear,
                    bookedSchedules = bookedSchedules,
                    onDismiss = { showCalendarPicker = false },
                    onConfirm = { m, y -> 
                        selectedSchedMonth = m
                        selectedSchedYear = y
                        showCalendarPicker = false
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text("Pendekatan Produksi (Production Focus)", fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    Triple("REGULER", "Reguler", "Standar"),
                    Triple("KUALITAS", "Fokus Kualitas", "Budget +30%, Durasi +6Bln"),
                    Triple("MAHAKARYA", "Ambisi Mahakarya", "Budget +80%, Durasi +12Bln")
                ).forEach { (id, t, desc) ->
                    val isSelected = selectedFocus == id
                    val focusContainerColor = if (isSelected) Color(0xFFDAA520).copy(alpha = 0.2f) else Color.White.copy(alpha=0.05f)
                    val focusBorderColor = if (isSelected) Color(0xFFDAA520) else Color.White.copy(alpha=0.1f)
                    Card(
                        onClick = { selectedFocus = id },
                        colors = CardDefaults.cardColors(containerColor = focusContainerColor),
                        border = BorderStroke(1.dp, focusBorderColor),
                        modifier = Modifier.width(160.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(t, fontWeight = FontWeight.Bold, color = if(isSelected) Color(0xFFDAA520) else Color.White)
                            Text(desc, fontSize = 10.sp, color = Color.LightGray)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            val bLongRaw = budgetInput.filter { it.isDigit() }.toLongOrNull() ?: 0L
            val bLong = when(selectedFocus) {
                "KUALITAS" -> (bLongRaw * 1.3).toLong()
                "MAHAKARYA" -> (bLongRaw * 1.8).toLong()
                else -> bLongRaw
            }
            val pLong = promoBudgetInput.filter { it.isDigit() }.toLongOrNull() ?: 0L
            val totalInvestment = bLong + pLong
            val distMult = if (isGlobal) 2.5 else 1.0
            val lvlBonus = 1.0 + (owned.level * 0.05)
            
            val promoRatio = if (bLong > 0) pLong.toDouble() / bLong.toDouble() else 0.0
            val promoMult = 1.0 + (promoRatio * 0.5).coerceAtMost(2.0)
            
            val minProj = (totalInvestment * (0.1 + (promoRatio * 0.1).coerceAtMost(0.4))).toLong()
            val maxProj = (totalInvestment * distMult * lvlBonus * 3.0 * promoMult * 1.5).toLong() // 1.5 is max viral multiplier
            
            val riskLevel = when {
                selectedFocus == "MAHAKARYA" -> "Low" // Stabilized Risk
                selectedFocus == "KUALITAS" -> "Medium"
                selectedGenres.size >= 4 -> "EXTREME"
                selectedGenres.size == 3 -> "High"
                selectedGenres.size == 2 -> "Medium"
                else -> "Low"
            }
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFF2C2C2C), Color(0xFF1A1A1A))), RoundedCornerShape(16.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("📊 Pitch Deck / Estimasi Analis", fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Total Cost: ${formatCurrencyRingkas(totalInvestment, useShortFormat)} (Prod: ${formatCurrencyRingkas(bLong, useShortFormat)} | Promo: ${formatCurrencyRingkas(pLong, useShortFormat)})", color = Color.White.copy(alpha=0.7f))
                    Text("Volatility Risk: $riskLevel", color = if (riskLevel == "EXTREME" || riskLevel == "High") Color.Red else Color.White.copy(alpha=0.7f))
                    Text("Est. Box Office: ${formatCurrencyRingkas(minProj, useShortFormat)} - ${formatCurrencyRingkas(maxProj, useShortFormat)}", fontWeight = FontWeight.SemiBold, color = Color.White)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { 
                    val success = onProduce(title.ifBlank { "Untitled Project" }, bLong, pLong, selectedGenres.toList(), isGlobal, selectedSchedMonth, selectedSchedYear, selectedFormat, selectedFocus)
                    if (success) {
                        title = ""
                        budgetInput = ""
                        promoBudgetInput = ""
                        selectedSchedMonth = null
                        selectedSchedYear = null
                    } else {
                        android.widget.Toast.makeText(context, "Gagal memulai produksi! (Judul sudah digunakan atau error)", android.widget.Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = totalInvestment in 10000..playerCash && selectedGenres.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("🎬 MULAI PRODUKSI (Potong Saldo)", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
            }
            
            if (bLong > playerCash) {
                Text("Saldo tidak cukup!", color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
            } else if (bLong < 10000 && bLong > 0) {
                Text("Minimal budget $10,000", color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
    
    val activeMovies = owned.projectHistory.filter { it.status != "FINISHED" }

    if (activeMovies.isNotEmpty()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .shadow(elevation = 8.dp, shape = RoundedCornerShape(20.dp))
                .border(androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)), RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("🎬 Sedang Berjalan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                activeMovies.forEach { proj ->
                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(proj.title, fontWeight = FontWeight.Bold)
                                if (proj.status == "IN_PRODUCTION") {
                                    if (proj.isAwaitingRelease) {
                                        Text("⏳ Menunggu Rilis: ${proj.scheduledReleaseDate ?: "?"}", fontSize = 12.sp, color = Color(0xFFFFA000))
                                    } else if (proj.isQcPhase) {
                                        Text("⚠️ Fase QC Internal: Skor ${proj.internalScore}/100", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFA000))
                                    } else if (proj.productionPhase == "ANTREAN" && proj.scheduledMonth != null && proj.scheduledYear != null) {
                                        val monthNames = listOf("Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Ags", "Sep", "Okt", "Nov", "Des")
                                        val mIndex = (proj.scheduledMonth - 1).coerceIn(0, 11)
                                        val vYear = proj.scheduledYear + 2019
                                        Text("⏳ Menunggu Jadwal: ${monthNames[mIndex]} $vYear", fontSize = 12.sp, color = Color(0xFFFFA000))
                                    } else {
                                        Text("🎥 ${proj.productionPhase} | Sisa: ${proj.productionDelayMonths} bln", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                    }
                                } else {
                                    Text("Score: ${proj.reviewScore}/100 | Tayang: ${proj.remainingMonths} bln", fontSize = 12.sp, color = Color.Gray)
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(horizontalAlignment = Alignment.End) {
                                    if (proj.status == "IN_PRODUCTION") {
                                        Text("Budget Produksi", fontSize = 12.sp, color = Color.Gray)
                                        Text(formatCurrencyRingkas(proj.budget, useShortFormat), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                                    } else {
                                        Text("Pendapatan Sementara", fontSize = 12.sp, color = Color.Gray)
                                        Text(formatCurrencyRingkas(proj.currentRevenue, useShortFormat), fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                                    }
                                }
                                IconButton(
                                    onClick = { filmToCancel = proj; showCancelDialog = true },
                                    modifier = Modifier.background(Color.White.copy(alpha = 0.05f), CircleShape)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Batalkan", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                        
                        if (proj.isQcPhase) {
                            Spacer(Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = { activePolishProject = proj },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Text("Poles Film", fontSize = 11.sp, textAlign = TextAlign.Center)
                                }
                                Button(
                                    onClick = { activeScheduleProject = proj },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Jadwalkan", fontSize = 11.sp, textAlign = TextAlign.Center)
                                }
                            }
                        }
                    }
                    HorizontalDivider(color = Color.LightGray.copy(alpha=0.3f), modifier = Modifier.padding(top=4.dp))
                }
            }
        }
    }
    
    Button(
        onClick = onOpenHistory,
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
    ) {
        Text("🎬 Buka Katalog IP & Histori Film")
    }

    if (showCancelDialog && filmToCancel != null) {
        val totalInvested = (filmToCancel!!.budget + filmToCancel!!.promoBudget)
        val isScreening = filmToCancel?.status == "IN_THEATERS"
        
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text(if (isScreening) "Tarik Film dari Bioskop?" else "Batalkan Proyek Film?") },
            text = { Text(if (isScreening) "Apakah Anda yakin ingin menarik paksa film '${filmToCancel!!.title}' dari peredaran? Pendapatan akan langsung dihentikan dan TIDAK ADA pengembalian dana (Hangus/Rugi Total)." else "Apakah Anda yakin ingin membatalkan proyek '${filmToCancel!!.title}' secara permanen? Karena proses sudah berjalan, dana produksi dan promosi hanya bisa ditarik kembali sebagian (30% - 50%).") },
            confirmButton = {
                Button(
                    onClick = {
                        if (isScreening) {
                            onCancel(filmToCancel!!.title, 0L)
                            android.widget.Toast.makeText(context, "Film ditarik dari bioskop. Tidak ada pengembalian dana.", android.widget.Toast.LENGTH_LONG).show()
                        } else {
                            val refundPercentage = (30..50).random() / 100.0
                            val refundAmount = (totalInvested * refundPercentage).toLong()
                            onCancel(filmToCancel!!.title, refundAmount)
                            android.widget.Toast.makeText(context, "Proyek dibatalkan. Dana sebesar ${formatCurrencyRingkas(refundAmount, false)} berhasil diselamatkan.", android.widget.Toast.LENGTH_LONG).show()
                        }
                        showCancelDialog = false
                        filmToCancel = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                ) { Text(if (isScreening) "Tarik Film" else "Hapus Proyek") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showCancelDialog = false }) { Text("Batal") }
            }
        )
    }

    var pCost by remember { mutableStateOf(0L) }
    var eMonths by remember { mutableStateOf(0) }
    
    LaunchedEffect(activePolishProject) {
        if (activePolishProject != null) {
            val p = activePolishProject!!
            pCost = (p.budget * (10..20).random() / 100).toLong()
            eMonths = (2..12).random()
        }
    }

    if (activePolishProject != null) {
        AlertDialog(
            onDismissRequest = { activePolishProject = null },
            title = { Text("Poles Film: ${activePolishProject!!.title}") },
            text = { Text("Suntik dana tambahan ${formatCurrencyRingkas(pCost, useShortFormat)} untuk memoles film dan menambah waktu produksi $eMonths bulan?") },
            confirmButton = {
                Button(onClick = {
                    onPolish(activePolishProject!!.title, pCost, eMonths)
                    activePolishProject = null
                }) { Text("Konfirmasi") }
            },
            dismissButton = {
                OutlinedButton(onClick = { activePolishProject = null }) { Text("Batal") }
            }
        )
    }

    if (activeScheduleProject != null) {
        val bookedSchedules = owned.projectHistory
            .filter { it.productionPhase == "ANTREAN" && it.scheduledMonth != null && it.scheduledYear != null }
            .map { Pair(it.scheduledMonth!!, it.scheduledYear!!) }

        CalendarPickerDialog(
            currentMonth = currentMonth,
            currentYear = currentYear,
            initialMonth = null,
            initialYear = null,
            bookedSchedules = bookedSchedules,
            onDismiss = { activeScheduleProject = null },
            onConfirm = { m, y ->
                val str = "$m/$y"
                onSchedule(activeScheduleProject!!.title, str)
                activeScheduleProject = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarPickerDialog(
    currentMonth: Int,
    currentYear: Int,
    initialMonth: Int?,
    initialYear: Int?,
    bookedSchedules: List<Pair<Int, Int>>,
    onDismiss: () -> Unit,
    onConfirm: (month: Int?, year: Int?) -> Unit
) {
    var viewYear by remember { mutableStateOf(initialYear ?: currentYear) }
    var selectedM by remember { mutableStateOf(initialMonth) }
    var selectedY by remember { mutableStateOf(initialYear) }
    
    val visualViewYear = viewYear + 2019
    
    val monthNames = listOf("Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Ags", "Sep", "Okt", "Nov", "Des")
    
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Pilih Jadwal Rilis", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                
                // Header (Tahun)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.material3.IconButton(
                        onClick = { viewYear-- },
                        enabled = viewYear > currentYear
                    ) {
                        Text("<", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                    
                    Text(
                        text = visualViewYear.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    androidx.compose.material3.IconButton(
                        onClick = { viewYear++ }
                    ) {
                        Text(">", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Grid 12 Bulan (4 Baris x 3 Kolom)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    monthNames.chunked(3).forEachIndexed { rowIndex, rowMonths ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            rowMonths.forEachIndexed { colIndex, monthName ->
                                val monthNum = rowIndex * 3 + colIndex + 1
                                val isPastDisabled = viewYear == currentYear && monthNum < currentMonth
                                val isAlreadyBooked = bookedSchedules.contains(Pair(monthNum, viewYear))
                                val isDisabled = isPastDisabled || isAlreadyBooked
                                val isSelected = selectedY == viewYear && selectedM == monthNum
                                
                                val containerColor = when {
                                    isSelected -> MaterialTheme.colorScheme.primary
                                    isDisabled -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }
                                val contentColor = when {
                                    isSelected -> MaterialTheme.colorScheme.onPrimary
                                    isDisabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                }
                                
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .clickable(enabled = !isDisabled) {
                                            selectedM = monthNum
                                            selectedY = viewYear
                                        },
                                    colors = CardDefaults.cardColors(containerColor = containerColor)
                                ) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        if (isAlreadyBooked) {
                                            Text("Penuh", color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        } else {
                                            Text(monthName, color = contentColor, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Action Buttons
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    androidx.compose.material3.TextButton(
                        onClick = { onConfirm(null, null) },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Langsung Produksi", color = MaterialTheme.colorScheme.error)
                    }
                    Button(
                        onClick = { onConfirm(selectedM, selectedY) },
                        enabled = selectedM != null && selectedY != null
                    ) {
                        Text("Simpan Jadwal")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TvStationDashboard(
    activePrograms: List<com.example.data.TvProgram>,
    playerCash: Long,
    useShortFormat: Boolean,
    inGameYear: Int,
    businessLevel: Int,
    bookedTimeSlots: List<String>,
    onAddProgram: (String, String, Double, Boolean, Long, Int, List<String>) -> Boolean,
    onCancelProgram: (String) -> Unit,
    onEditSchedule: (String, List<String>) -> Unit
) {
    var showAddSheet by remember { mutableStateOf(false) }
    var editScheduleProgramId by remember { mutableStateOf<String?>(null) }

    // Bidding War State
    var showBiddingDialog by remember { mutableStateOf(false) }
    var bidItemTitle by remember { mutableStateOf("") }
    var bidItemDuration by remember { mutableStateOf(-1) }
    var bidBasePrice by remember { mutableStateOf(0.0) }
    var currentRivalBid by remember { mutableStateOf(0.0) }
    var playerBidStr by remember { mutableStateOf("") }
    var bidMessage by remember { mutableStateOf("Jaringan TV Pesaing (Rival Network) ikut masuk dalam lelang. Masukkan penawaran Anda.") }
    val context = androidx.compose.ui.platform.LocalContext.current

    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Program Management", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { showAddSheet = true }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
                Text("Tambah Program / Hak Siar")
            }
            Spacer(modifier = Modifier.height(16.dp))
            if (activePrograms.isEmpty()) {
                Text("Belum ada program mengudara.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                activePrograms.sortedBy { it.timeSlots.firstOrNull() ?: "24:00" }.forEach { prog ->
                    val opsCost = prog.currentOperationalCost
                    val isProfit = prog.monthlyAdRevenue >= opsCost
                    val net = prog.monthlyAdRevenue - opsCost
                    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Column(modifier = Modifier.padding(12.dp).fillMaxWidth()) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(prog.title, fontWeight = FontWeight.Bold)
                                    val isPremium = prog.remainingMonths > 0
                                    
                                    val ratingDiff = prog.rating - prog.previousRating
                                    val ratingInd = if (ratingDiff > 0) " (↑)" else if (ratingDiff < 0) " (↓)" else ""
                                    Text("${prog.type} • Rating: ${java.lang.String.format("%.1f", prog.rating)}%$ratingInd", style = MaterialTheme.typography.bodySmall)
                                    
                                    if (prog.timeSlots.isNotEmpty()) {
                                        val sortedSlots = prog.timeSlots.sorted()
                                        val firstSlot = sortedSlots.first()
                                        val lastSlot = sortedSlots.last()
                                        val lastHour = lastSlot.substringBefore(":").toInt()
                                        val lastMin = lastSlot.substringAfter(":").toInt()
                                        var endMin = lastMin + 30
                                        var endHour = lastHour
                                        if (endMin >= 60) {
                                            endMin -= 60
                                            endHour += 1
                                        }
                                        if (endHour >= 24) endHour -= 24
                                        val endTimeStr = java.lang.String.format("%02d:%02d", endHour, endMin)
                                        Text("Jam: $firstSlot - $endTimeStr", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                    }

                                    if (isPremium) {
                                        Text("Sisa Kontrak: ${prog.remainingMonths} Bulan", style = MaterialTheme.typography.bodySmall, color = if (prog.remainingMonths in 1..2) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Ad Rev: +${formatCurrencyRingkas(prog.monthlyAdRevenue.toLong(), useShortFormat)}", color = Color(0xFF00C853), style = MaterialTheme.typography.bodySmall)
                                    Text("Ops: -${formatCurrencyRingkas(opsCost.toLong(), useShortFormat)}", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                                    Text(
                                        "Net: ${if (net >= 0) "+" else "-"}${formatCurrencyRingkas(kotlin.math.abs(net).toLong(), useShortFormat)}/bln", 
                                        color = if (isProfit) Color(0xFF00C853) else MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                Button(onClick = { editScheduleProgramId = prog.id }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary), modifier = Modifier.padding(end = 8.dp)) {
                                    Text("EDIT JADWAL")
                                }
                                Button(onClick = { onCancelProgram(prog.id) }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                                    Text("BUNGKUS")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (editScheduleProgramId != null) {
        val progToEdit = activePrograms.find { it.id == editScheduleProgramId }
        var selectedSlots by remember { mutableStateOf(progToEdit?.timeSlots?.toSet() ?: emptySet()) }
        val allSlots = (0..23).flatMap { h -> listOf(String.format("%02d:00", h), String.format("%02d:30", h)) }

        AlertDialog(
            onDismissRequest = { editScheduleProgramId = null },
            title = { Text("Edit Jadwal: ${progToEdit?.title}") },
            text = {
                Column {
                    Text("Pilih Jam Tayang (Maks 2 Jam / 4 Slot)", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                        columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(4),
                        modifier = Modifier.height(300.dp)
                    ) {
                        items(allSlots.size) { idx ->
                            val slot = allSlots[idx]
                            val isOtherBooked = bookedTimeSlots.contains(slot) && !(progToEdit?.timeSlots?.contains(slot) ?: false)
                            val isSelected = selectedSlots.contains(slot)
                            androidx.compose.material3.FilterChip(
                                selected = isSelected,
                                onClick = {
                                    if (isSelected) {
                                        selectedSlots = selectedSlots - slot
                                    } else {
                                        if (selectedSlots.size < 4) {
                                            selectedSlots = selectedSlots + slot
                                        }
                                    }
                                },
                                label = { Text(slot, fontSize = 10.sp) },
                                enabled = !isOtherBooked,
                                modifier = Modifier.padding(2.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onEditSchedule(editScheduleProgramId!!, selectedSlots.toList().sorted())
                        editScheduleProgramId = null
                    },
                    enabled = selectedSlots.isNotEmpty()
                ) {
                    Text("Simpan Jadwal")
                }
            },
            dismissButton = {
                TextButton(onClick = { editScheduleProgramId = null }) { Text("Batal") }
            }
        )
    }

    if (showBiddingDialog) {
        AlertDialog(
            onDismissRequest = { showBiddingDialog = false },
            title = { Text("Bidding War: $bidItemTitle") },
            text = {
                Column {
                    Text(bidMessage, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Tawaran Tertinggi Saat Ini (Rival): ${formatCurrencyRingkas(currentRivalBid.toLong(), useShortFormat)}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = playerBidStr,
                        onValueChange = { playerBidStr = it.filter { c -> c.isDigit() } },
                        label = { Text("Tawaran Anda ($)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val playerBid = playerBidStr.toDoubleOrNull() ?: 0.0
                        if (playerBid <= currentRivalBid) {
                            bidMessage = "Tawaran Anda harus lebih tinggi dari ${formatCurrencyRingkas(currentRivalBid.toLong(), useShortFormat)}!"
                        } else if (playerBid > playerCash) {
                            bidMessage = "Kas perusahaan Anda tidak mencukupi untuk tawaran ini!"
                        } else {
                            val isMajor = bidItemTitle.contains("World Cup") || bidItemTitle.contains("Euro") || bidItemTitle.contains("Champions League")
                            val rivalAggression = if (isMajor) 2.0 else 1.3
                            val maxRivalTol = currentRivalBid * rivalAggression
                            
                            val rivalGiveUpChance = if (playerBid > maxRivalTol) 0.9 else (playerBid - currentRivalBid) / (maxRivalTol - currentRivalBid + 1)
                            
                            if (Math.random() < rivalGiveUpChance) {
                                val success = onAddProgram(bidItemTitle, "Sports/Event", bidBasePrice, true, playerBid.toLong(), bidItemDuration, emptyList())
                                if (success) {
                                    showBiddingDialog = false
                                } else {
                                    android.widget.Toast.makeText(context, "Judul sudah digunakan!", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                val bumpFactor = 1.05 + (Math.random() * 0.1)
                                currentRivalBid = playerBid * bumpFactor
                                bidMessage = "RIVAL COUNTER-BID! Rival langsung menaikkan tawaran. Apakah Anda bersedia menaikkannya lagi?"
                            }
                        }
                    }
                ) {
                    Text("Ajukan Bid")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBiddingDialog = false }) { Text("Mundur") }
            }
        )
    }

    if (showAddSheet) {
        ModalBottomSheet(onDismissRequest = { showAddSheet = false }) {
            var selectedTab by remember { mutableStateOf(0) }
            Column(modifier = Modifier.padding(16.dp).fillMaxWidth().padding(bottom = 32.dp)) {
                Text("Tambah Program TV", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Siaran Internal") })
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Siaran Premium") })
                }
                Spacer(modifier = Modifier.height(16.dp))
                
                if (selectedTab == 0) {
                    var title by remember { mutableStateOf("") }
                    var type by remember { mutableStateOf("Sinetron") }
                    val types = listOf("Sinetron", "Sitkom", "Berita", "Talkshow", "Reality Show", "Pencarian Bakat (Talent Show)", "Dokumenter", "Animasi Anak", "FTV", "Kuis Interaktif (Game Show)", "Variety Show", "Late Night Show", "Investigasi Kriminal")
                    var typeExpanded by remember { mutableStateOf(false) }
                    var budgetStr by remember { mutableStateOf("") }
                    var selectedSlots by remember { mutableStateOf(setOf<String>()) }
                    val allSlots = (0..23).flatMap { h -> listOf(String.format("%02d:00", h), String.format("%02d:30", h)) }
                    
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Judul Program") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    ExposedDropdownMenuBox(expanded = typeExpanded, onExpandedChange = { typeExpanded = it }) {
                        OutlinedTextField(
                            value = type, onValueChange = {}, readOnly = true,
                            label = { Text("Tipe Program") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                            types.forEach { t ->
                                DropdownMenuItem(text = { Text(t) }, onClick = { type = t; typeExpanded = false })
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = budgetStr, onValueChange = { budgetStr = it.filter { c -> c.isDigit() } }, label = { Text("Budget Produksi Awal ($)") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Pilih Jam Tayang (Maks 2 Jam / 4 Slot)", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                        columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(4),
                        modifier = Modifier.height(200.dp)
                    ) {
                        items(allSlots.size) { idx ->
                            val slot = allSlots[idx]
                            val isBooked = bookedTimeSlots.contains(slot)
                            val isSelected = selectedSlots.contains(slot)
                            androidx.compose.material3.FilterChip(
                                selected = isSelected,
                                onClick = {
                                    if (isSelected) {
                                        selectedSlots = selectedSlots - slot
                                    } else {
                                        if (selectedSlots.size < 4) {
                                            selectedSlots = selectedSlots + slot
                                        }
                                    }
                                },
                                label = { Text(slot, fontSize = 10.sp) },
                                enabled = !isBooked,
                                modifier = Modifier.padding(2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    val budg = budgetStr.toDoubleOrNull() ?: 0.0
                    Button(
                        onClick = {
                            if (title.isNotEmpty() && budg > 0 && selectedSlots.isNotEmpty()) {
                                val success = onAddProgram(title, type, budg, false, budg.toLong(), -1, selectedSlots.toList().sorted())
                                if (success) {
                                    showAddSheet = false
                                } else {
                                    android.widget.Toast.makeText(context, "Judul sudah digunakan!", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        enabled = title.isNotEmpty() && budg > 0 && playerCash >= budg.toLong() && selectedSlots.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (playerCash >= budg.toLong()) "Produksi Sekarang" else "Dana Tidak Cukup")
                    }
                } else {
                    val isWorldCupYear = inGameYear % 4 == 0
                    
                    data class PremiumRight(val title: String, val basePrice: Double, val requiredLevel: Int, val durationMonths: Int)
                    
                    val premiumOptions = mutableListOf(
                        PremiumRight("Liga 1 Indonesia", 50000000.0, 20, 10),
                        PremiumRight("AFC Cup", 60000000.0, 20, 10),
                        PremiumRight("Liga Africa", 70000000.0, 30, 10),
                        PremiumRight("Liga Arab Saudi", 80000000.0, 30, 10),
                        PremiumRight("DFB Pokal", 110000000.0, 40, 10),
                        PremiumRight("FA Cup", 120000000.0, 40, 10),
                        PremiumRight("Super Copa", 90000000.0, 40, 10),
                        PremiumRight("Serie A", 180000000.0, 40, 10),
                        PremiumRight("La Liga", 250000000.0, 40, 10),
                        PremiumRight("Bundesliga", 150000000.0, 50, 10),
                        PremiumRight("F1", 200000000.0, 50, 10),
                        PremiumRight("Premier League", 300000000.0, 60, 10),
                        PremiumRight("UEFA Champions League", 400000000.0, 60, 10)
                    )
                    
                    if (isWorldCupYear) {
                        premiumOptions.add(0, PremiumRight("FIFA World Cup (Major Event)", 800000000.0, 70, 2))
                        premiumOptions.add(1, PremiumRight("UEFA Euro (Major Event)", 600000000.0, 70, 2))
                        premiumOptions.add(2, PremiumRight("FIFA Club World Cup", 300000000.0, 70, 2))
                    }
                    
                    LazyColumn {
                        items(count = premiumOptions.size) { idx ->
                            val opt = premiumOptions[idx]
                            val isLocked = businessLevel < opt.requiredLevel
                            Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                                Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(opt.title, fontWeight = FontWeight.Bold)
                                        Text("Base/Start Price: ${formatCurrencyRingkas(opt.basePrice.toLong(), useShortFormat)}", style = MaterialTheme.typography.bodySmall)
                                        Text("Kontrak: ${opt.durationMonths} Bulan", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = { 
                                            bidItemTitle = opt.title
                                            bidItemDuration = opt.durationMonths
                                            bidBasePrice = opt.basePrice
                                            currentRivalBid = opt.basePrice * (1.0 + Math.random() * 0.1) // Rival starts slightly above base price
                                            playerBidStr = ""
                                            bidMessage = "Jaringan TV Pesaing (Rival Network) ikut masuk dalam lelang. Masukkan penawaran Anda."
                                            showAddSheet = false 
                                            showBiddingDialog = true
                                        },
                                        enabled = !isLocked,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isLocked) Color.Gray else MaterialTheme.colorScheme.primary
                                        )
                                    ) {
                                        Text(if (isLocked) "Terkunci (Lvl ${opt.requiredLevel})" else "Ikut Lelang")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun SoftwareHouseDashboard(
    appProjects: List<com.example.data.AppProject>,
    businessLevel: Int,
    ownedBusinesses: List<com.example.data.OwnedBusiness>,
    playerCash: Long,
    useShortFormat: Boolean,
    onStartProject: (String, com.example.data.ProjectType, Double, Double, Int, String?) -> Unit,
    onSellSaaS: (String) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("B2B Market", "SaaS Portfolio", "Synergy Hub")

    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Software House Kanban", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            androidx.compose.material3.TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    androidx.compose.material3.Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when(selectedTab) {
                0 -> {
                    val activeB2B = appProjects.filter { it.type == com.example.data.ProjectType.CLIENT_B2B && it.status == com.example.data.ProjectStatus.DEVELOPMENT }
                    if (activeB2B.isNotEmpty()) {
                        Text("Active B2B Projects:", fontWeight = FontWeight.Bold)
                        activeB2B.forEach { proj ->
                             val prog = proj.currentMonth.toFloat() / proj.devTimeMonths.coerceAtLeast(1)
                             Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                                 Column(modifier = Modifier.padding(12.dp)) {
                                      Text(proj.title, fontWeight = FontWeight.Bold)
                                      Text("Month: ${proj.currentMonth} / ${proj.devTimeMonths}", style = MaterialTheme.typography.bodySmall)
                                      androidx.compose.material3.LinearProgressIndicator(progress = { prog }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp))
                                      Text("Budget/mo: -${formatCurrencyRingkas((proj.budgetCost / proj.devTimeMonths).toLong(), useShortFormat)} | Payout: +${formatCurrencyRingkas(proj.targetRevenue.toLong(), useShortFormat)}", style = MaterialTheme.typography.labelSmall)
                                 }
                             }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    Text("Available B2B Contracts:", fontWeight = FontWeight.Bold)
                    val available = remember(businessLevel) {
                         val list = mutableListOf<Triple<String, Double, Double>>() // title, budget, revenue
                         if (businessLevel < 10) {
                              list.add(Triple("Sistem Kasir Toko", 10000.0, 30000.0))
                              list.add(Triple("Website Company Profile", 5000.0, 15000.0))
                              list.add(Triple("Aplikasi Antrian Faskes", 15000.0, 40000.0))
                         } else {
                              list.add(Triple("Integrasi Big Data", 150000.0, 450000.0))
                              list.add(Triple("Super App Ekosistem", 500000.0, 2000000.0))
                              list.add(Triple("AI Customer Service", 200000.0, 800000.0))
                         }
                         list
                    }
                    val devTime = if (businessLevel < 10) 3 else 7
                    available.forEach { opt ->
                         Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                             Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                  Column(modifier = Modifier.weight(1f)) {
                                      Text(opt.first, fontWeight = FontWeight.Bold)
                                      Text("Dev: $devTime bulan", style = MaterialTheme.typography.labelSmall)
                                      Text("Budget: ${formatCurrencyRingkas(opt.second.toLong(), useShortFormat)} | Payout: ${formatCurrencyRingkas(opt.third.toLong(), useShortFormat)}", style = MaterialTheme.typography.labelSmall)
                                  }
                                  Button(
                                      onClick = { onStartProject(opt.first, com.example.data.ProjectType.CLIENT_B2B, opt.second, opt.third, devTime, null) },
                                      enabled = playerCash >= opt.second.toLong()
                                  ) {
                                      Text("Ambil")
                                  }
                             }
                         }
                    }
                }
                1 -> {
                    val activeDev = appProjects.filter { it.type == com.example.data.ProjectType.INDEPENDENT_SAAS && it.status == com.example.data.ProjectStatus.DEVELOPMENT }
                    if (activeDev.isNotEmpty()) {
                        Text("In Development:", fontWeight = FontWeight.Bold)
                        activeDev.forEach { proj ->
                             val prog = proj.currentMonth.toFloat() / proj.devTimeMonths.coerceAtLeast(1)
                             Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                                 Column(modifier = Modifier.padding(12.dp)) {
                                      Text(proj.title, fontWeight = FontWeight.Bold)
                                      Text("Month: ${proj.currentMonth} / ${proj.devTimeMonths}", style = MaterialTheme.typography.bodySmall)
                                      androidx.compose.material3.LinearProgressIndicator(progress = { prog }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp))
                                 }
                             }
                        }
                    }

                    val released = appProjects.filter { it.type == com.example.data.ProjectType.INDEPENDENT_SAAS && it.status == com.example.data.ProjectStatus.MAINTENANCE }
                    if (released.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Live SaaS Apps:", fontWeight = FontWeight.Bold)
                        released.forEach { proj ->
                             Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                                 Column(modifier = Modifier.padding(12.dp)) {
                                      Text(proj.title, fontWeight = FontWeight.Bold)
                                      Text("MRR: ${formatCurrencyRingkas(proj.targetRevenue.toLong(), useShortFormat)}/bln", color = Color(0xFF00C853))
                                      Button(onClick = { onSellSaaS(proj.id) }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                                          Text("Acquisition/Jual (+${formatCurrencyRingkas((proj.targetRevenue * 50).toLong(), useShortFormat)})")
                                      }
                                 }
                             }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = {
                         onStartProject("Custom SaaS App", com.example.data.ProjectType.INDEPENDENT_SAAS, 50000.0, 15000.0, 5, null)
                    }, modifier = Modifier.fillMaxWidth(), enabled = playerCash >= 50000) {
                         Text("Bangun SaaS Baru (Cost: 50k, 5bln)")
                    }
                }
                2 -> {
                    Text("In-House Synergy", fontWeight = FontWeight.Bold)
                    val synergyOptions = mutableListOf<Triple<String, String, String>>()
                    synergyOptions.add(Triple("media_tv", "Bangun Platform Streaming", "Sinergi TV Station (+25% Rev)"))
                    synergyOptions.add(Triple("retail_supermarket", "Bangun Aplikasi E-Commerce", "Sinergi Supermarket (+25% Rev)"))

                    synergyOptions.forEach { opt ->
                        val targetBiz = ownedBusinesses.find { it.catalogId == opt.first }
                        if (targetBiz != null) {
                            val activeProj = appProjects.find { it.targetBusinessId == targetBiz.instanceId && it.status == com.example.data.ProjectStatus.DEVELOPMENT }
                            val completedProj = appProjects.find { it.targetBusinessId == targetBiz.instanceId && it.status == com.example.data.ProjectStatus.COMPLETED }
                            
                            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                                Column(modifier = Modifier.padding(12.dp).fillMaxWidth()) {
                                    Text(opt.third, fontWeight = FontWeight.Bold)
                                    if (completedProj != null) {
                                        Text("Selesai & Aktif", color = Color(0xFF00C853))
                                    } else if (activeProj != null) {
                                        val prog = activeProj.currentMonth.toFloat() / activeProj.devTimeMonths.coerceAtLeast(1)
                                        Text("In Development: ${activeProj.currentMonth}/${activeProj.devTimeMonths} bln")
                                        androidx.compose.material3.LinearProgressIndicator(progress = { prog }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp))
                                    } else {
                                        Button(
                                            onClick = { onStartProject(opt.second, com.example.data.ProjectType.ECOSYSTEM_SYNERGY, 200000.0, 0.0, 6, targetBiz.instanceId) },
                                            enabled = playerCash >= 200000
                                        ) {
                                            Text("Mulai Bangun (Cost 200k, 6 bln)")
                                        }
                                    }
                                }
                            }
                        } else {
                            Text("Belum punya bisnis untuk: ${opt.third}", color = Color.Gray, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(4.dp))
                        }
                    }
                }
            }
        }
    }
}
