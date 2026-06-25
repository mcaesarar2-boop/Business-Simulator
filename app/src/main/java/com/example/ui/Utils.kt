package com.example.ui

import com.example.viewmodel.GameViewModel

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
