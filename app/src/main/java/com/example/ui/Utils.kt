package com.example.ui

fun formatMarketCap(value: Double, isIndo: Boolean): String {
    val absVal = Math.abs(value)
    
    // Format full number using standard dot separators for thousands
    val numberFormat = java.text.NumberFormat.getNumberInstance(java.util.Locale.GERMANY).apply {
        maximumFractionDigits = 0
    }
    val fullFormatted = numberFormat.format(value)
    
    val currencyPrefix = if (isIndo) "Rp " else "$ "
    
    val (suffix, divisor) = when {
        absVal >= 1_000_000_000_000.0 -> {
            val s = if (isIndo) "Triliun" else "Trillion"
            s to 1_000_000_000_000.0
        }
        absVal >= 1_000_000_000.0 -> {
            val s = if (isIndo) "Miliar" else "Billion"
            s to 1_000_000_000.0
        }
        absVal >= 1_000_000.0 -> {
            val s = if (isIndo) "Juta" else "Million"
            s to 1_000_000.0
        }
        else -> "" to 1.0
    }
    
    return if (suffix.isNotEmpty()) {
        val shortVal = value / divisor
        val shortFormatted = String.format(if (isIndo) java.util.Locale.GERMANY else java.util.Locale.US, "%.2f", shortVal)
        "$currencyPrefix$fullFormatted ($shortFormatted $suffix)"
    } else {
        "$currencyPrefix$fullFormatted"
    }
}
