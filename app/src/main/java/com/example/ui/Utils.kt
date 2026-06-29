package com.example.ui

import com.example.viewmodel.GameViewModel
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding

fun formatMarketCap(value: Double): String {
    val absVal = Math.abs(value)
    
    // Format full number using standard dot separators for thousands
    val numberFormat = java.text.NumberFormat.getNumberInstance(java.util.Locale.US).apply {
        maximumFractionDigits = 0
    }
    val fullFormatted = numberFormat.format(value)
    
    val (suffix, divisor) = when {
        absVal >= 1_000_000_000_000.0 -> "Trillion" to 1_000_000_000_000.0
        absVal >= 1_000_000_000.0 -> "Billion" to 1_000_000_000.0
        absVal >= 1_000_000.0 -> "Million" to 1_000_000.0
        else -> "" to 1.0
    }
    
    return if (suffix.isNotEmpty()) {
        val shortVal = value / divisor
        val shortFormatted = String.format(java.util.Locale.US, "%.2f", shortVal)
        "$$fullFormatted ($shortFormatted $suffix)"
    } else {
        "$$fullFormatted"
    }
}

fun formatGlobalDate(month: Int, year: Int): String {
    val baseYear = 2019
    val currentYear = baseYear + year
    val monthNames = arrayOf(
        "Januari", "Februari", "Maret", "April", "Mei", "Juni", 
        "Juli", "Agustus", "September", "Oktober", "November", "Desember"
    )
    val monthIndex = (month - 1).coerceIn(0, 11)
    val monthName = monthNames[monthIndex]
    
    return "$monthName $currentYear"
}

fun formatCurrencyRingkas(amount: Number, isShort: Boolean): String {
    val currencyFormat = java.text.NumberFormat.getCurrencyInstance(java.util.Locale.US).apply { 
        maximumFractionDigits = 0 
    }
    if (!isShort) return currencyFormat.format(amount)
    
    val doubleAmount = amount.toDouble()
    val absVal = Math.abs(doubleAmount)
    val (suffix, divisor) = when {
        absVal >= 1_000_000_000_000.0 -> "T" to 1_000_000_000_000.0
        absVal >= 1_000_000_000.0 -> "B" to 1_000_000_000.0
        absVal >= 1_000_000.0 -> "M" to 1_000_000.0
        absVal >= 1_000.0 -> "K" to 1_000.0
        else -> return currencyFormat.format(amount)
    }
    
    val shortVal = doubleAmount / divisor
    val shortStr = String.format(java.util.Locale.US, "%.1f", shortVal).replace(".0", "")
    return if (doubleAmount < 0) "-\$$shortStr$suffix" else "\$$shortStr$suffix"
}

@androidx.compose.runtime.Composable
fun ChecklistItem(label: String, current: Int, target: Int, isOk: Boolean) {
    androidx.compose.foundation.layout.Row(
        modifier = androidx.compose.ui.Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
    ) {
        androidx.compose.foundation.layout.Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            androidx.compose.material3.Text(
                text = if (isOk) "✅" else "⬜",
                fontSize = 14.sp
            )
            androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.width(8.dp))
            androidx.compose.material3.Text(
                text = label,
                color = if (isOk) androidx.compose.ui.graphics.Color.White else androidx.compose.ui.graphics.Color.LightGray,
                fontSize = 13.sp
            )
        }
        androidx.compose.material3.Text(
            text = "$current / $target",
            color = if (isOk) androidx.compose.ui.graphics.Color(0xFF81C784) else androidx.compose.ui.graphics.Color(0xFFE57373),
            fontSize = 13.sp,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
        )
    }
}
