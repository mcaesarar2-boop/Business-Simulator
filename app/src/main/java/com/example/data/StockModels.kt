package com.example.data

data class StockItem(
    val id: String,
    val ticker: String,
    val name: String,
    val currentPrice: Double,
    val changeAbsolute: Double,
    val changePercentage: Double,
    val sector: String
) {
    val logoDomain: String?
        get() = getDomainForTicker(ticker)
}

data class StockStats(
    val sharesOutstanding: Long,
    val dividendYield: Double,
    val peRatio: Double,
    val highToday: Double,
    val lowToday: Double
)

data class MarketNews(
    val id: String,
    val text: String,
    val type: String,
    val timestamp: String = "Now"
)
