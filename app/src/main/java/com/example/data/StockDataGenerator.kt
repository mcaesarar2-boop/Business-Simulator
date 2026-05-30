package com.example.data

import java.util.Locale

fun getDomainForTicker(ticker: String): String? {
    val base = ticker.substringBefore(".").uppercase(Locale.ROOT)
    return when (base) {
        "AAPL" -> "apple.com"
        "MSFT" -> "microsoft.com"
        "GOOGL", "GOOG" -> "google.com"
        "NVDA" -> "nvidia.com"
        "TSLA" -> "tesla.com"
        "META" -> "meta.com"
        "AMZN" -> "amazon.com"
        "JPM" -> "jpmorgan.com"
        "BAC" -> "bankofamerica.com"
        "WMT" -> "walmart.com"
        "JNJ" -> "jnj.com"
        "V" -> "visa.com"
        "PG" -> "pg.com"
        "MA" -> "mastercard.com"
        "HD" -> "homedepot.com"
        "CVX" -> "chevron.com"
        "LLY" -> "lilly.com"
        "ABBV" -> "abbvie.com"
        "MRK" -> "merck.com"
        "PEP" -> "pepsico.com"
        "KO" -> "coca-cola.com"
        "AVGO" -> "broadcom.com"
        "ASML" -> "asml.com"
        "TTE" -> "totalenergies.com"
        "NVO" -> "novonordisk.com"
        "NVS" -> "novartis.com"
        "AZN" -> "astrazeneca.com"
        "SHEL" -> "shell.com"
        "TMUS" -> "t-mobile.com"
        "CMCSA" -> "comcast.com"
        "DIS" -> "disney.com"
        "NFLX" -> "netflix.com"
        "ADBE" -> "adobe.com"
        "NKE" -> "nike.com"
        "NTDOY" -> "nintendo.com"
        "SONY" -> "sony.com"
        "AMD" -> "amd.com"
        "ORCL" -> "oracle.com"
        "CRM" -> "salesforce.com"
        "INTC" -> "intel.com"
        "QCOM" -> "qualcomm.com"
        "TXN" -> "ti.com"
        "COST" -> "costco.com"
        "SBUX" -> "starbucks.com"
        "MCD" -> "mcdonalds.com"
        
        "BBCA" -> "bca.co.id"
        "BBRI" -> "bri.co.id"
        "GOTO" -> "gotocompany.com"
        "TLKM" -> "telkom.co.id"
        "BMRI" -> "bankmandiri.co.id"
        "BBNI" -> "bni.co.id"
        "ASII" -> "astra.co.id"
        "UNVR" -> "unilever.co.id"
        "KLBF" -> "kalbe.co.id"
        "ADRO" -> "adaro.com"
        "PTBA" -> "bukitasam.co.id"
        "INDF" -> "indofood.com"
        "ICBP" -> "indofoodcbp.com"
        "AMRT" -> "alfamart.co.id"
        "BRIS" -> "bankbsi.co.id"
        "PGAS" -> "pgas.co.id"
        "ANTM" -> "antam.com"
        "TINS" -> "timah.com"
        "HRUM" -> "harumenergy.com"
        "UNTR" -> "unitedtractors.com"
        "JSMR" -> "jasamarga.com"
        "ADHI" -> "adhi.co.id"
        "WIKA" -> "wijayakarya.com"
        "PTPP" -> "ptpp.co.id"
        "SMGR" -> "sig.id"
        "INCO" -> "vale.com"
        "BDMN" -> "danamon.co.id"
        "BNGA" -> "cimbniaga.co.id"
        "CPIN" -> "cp.co.id"
        "EXCL" -> "xl.co.id"
        "ISAT" -> "indosatooredoo.com"
        "JPFA" -> "japfacomfeed.co.id"
        "MEDC" -> "medcoenergi.com"
        "MAPI" -> "map.co.id"
        "MYOR" -> "mayora.com"
        "SCMA" -> "suryacitramedia.com"
        "TOWR" -> "sarana-menara.com"
        "TBIG" -> "tbbtower.com"
        "HEAL" -> "medikalokahermina.com"
        "MIKA" -> "mitra-keluarga.com"
        else -> null
    }
}

fun generateStockData(): List<StockItem> {
    return listOf(
        // === GLOBAL / US COMPANIES ===
        StockItem("1", "AAPL", "Apple Inc.", 185.50, 1.20, 0.65, "Technology"),
        StockItem("2", "MSFT", "Microsoft Corp.", 425.10, 2.50, 0.59, "Technology"),
        StockItem("3", "GOOGL", "Alphabet Inc.", 172.40, -1.10, -0.63, "Technology"),
        StockItem("4", "NVDA", "NVIDIA Corp.", 125.60, 5.30, 4.41, "Technology"),
        StockItem("5", "TSLA", "Tesla Inc.", 176.40, -5.20, -2.86, "Automotive"),
        StockItem("10", "TM", "Toyota Motor", 201.20, 1.20, 0.60, "Automotive"),
        StockItem("11", "RACE", "Ferrari N.V.", 405.50, 3.40, 0.85, "Automotive"),
        StockItem("12", "MC.PA", "LVMH Moët Hennessy", 760.00, 12.00, 1.60, "Luxury"),
        StockItem("13", "META", "Meta Platforms", 475.20, 5.00, 1.06, "Technology"),
        StockItem("14", "AMZN", "Amazon.com Inc.", 182.40, -0.50, -0.27, "Retail"),
        StockItem("15", "JPM", "JPMorgan Chase", 198.50, 1.10, 0.56, "Finance"),
        StockItem("16", "BAC", "Bank of America", 39.20, -0.20, -0.51, "Finance"),
        StockItem("17", "WMT", "Walmart Inc.", 67.40, 0.50, 0.75, "Retail"),
        StockItem("18", "JNJ", "Johnson & Johnson", 152.00, -1.00, -0.65, "Healthcare"),
        StockItem("19", "V", "Visa Inc.", 274.50, 2.00, 0.73, "Finance"),
        StockItem("20", "PG", "Procter & Gamble", 162.80, 0.80, 0.49, "Consumer"),
        StockItem("21", "MA", "Mastercard Inc.", 452.10, 3.50, 0.78, "Finance"),
        StockItem("22", "HD", "Home Depot Inc.", 342.30, -2.00, -0.58, "Retail"),
        StockItem("23", "CVX", "Chevron Corp.", 158.40, 1.50, 0.96, "Energy"),
        StockItem("24", "LLY", "Eli Lilly & Co.", 815.00, 4.00, 0.49, "Healthcare"),
        StockItem("25", "ABBV", "AbbVie Inc.", 164.50, -1.50, -0.90, "Healthcare"),
        StockItem("26", "MRK", "Merck & Co.", 122.30, 0.50, 0.41, "Healthcare"),
        StockItem("27", "PEP", "PepsiCo Inc.", 172.50, 1.00, 0.58, "Consumer"),
        StockItem("28", "KO", "Coca-Cola Co.", 63.10, 0.20, 0.32, "Consumer"),
        StockItem("29", "AVGO", "Broadcom Inc.", 145.20, 1.80, 1.26, "Technology"),
        StockItem("30", "ASML", "ASML Holding", 930.00, -5.00, -0.53, "Technology"),
        StockItem("31", "TTE", "TotalEnergies SE", 72.40, 0.50, 0.69, "Energy"),
        StockItem("32", "NVO", "Novo Nordisk", 134.50, 2.00, 1.51, "Healthcare"),
        StockItem("33", "NVS", "Novartis AG", 102.30, 1.00, 0.99, "Healthcare"),
        StockItem("34", "AZN", "AstraZeneca", 78.10, -0.50, -0.64, "Healthcare"),
        StockItem("35", "SHEL", "Shell plc", 71.50, 0.80, 1.13, "Energy"),
        StockItem("36", "TMUS", "T-Mobile US", 178.60, 1.50, 0.85, "Communication"),
        StockItem("37", "CMCSA", "Comcast Corp.", 41.20, -0.20, -0.48, "Communication"),
        StockItem("38", "DIS", "Walt Disney Co", 102.50, -1.00, -0.97, "Communication"),
        StockItem("39", "NFLX", "Netflix Inc.", 635.40, 5.00, 0.79, "Communication"),
        StockItem("40", "ADBE", "Adobe Inc.", 489.10, 6.00, 1.24, "Technology"),
        StockItem("86", "NKE", "Nike Inc.", 94.80, -1.20, -1.25, "Consumer"),
        StockItem("87", "NTDOY", "Nintendo Co. Ltd.", 13.80, 0.20, 1.47, "Technology"),
        StockItem("88", "SONY", "Sony Group", 86.20, 1.10, 1.29, "Technology"),
        StockItem("89", "AMD", "Advanced Micro Devices", 162.50, 4.20, 2.65, "Technology"),
        StockItem("90", "ORCL", "Oracle Corp.", 124.30, -0.50, -0.40, "Technology"),
        StockItem("91", "CRM", "Salesforce Inc.", 265.40, 2.40, 0.91, "Technology"),
        StockItem("92", "INTC", "Intel Corp.", 31.20, -0.90, -2.80, "Technology"),
        StockItem("93", "QCOM", "Qualcomm Inc.", 185.60, 1.80, 0.98, "Technology"),
        StockItem("94", "TXN", "Texas Instruments", 164.80, 0.70, 0.43, "Technology"),
        StockItem("95", "COST", "Costco Wholesale", 792.10, 6.50, 0.83, "Retail"),
        StockItem("96", "SBUX", "Starbucks Corp.", 81.50, -0.80, -0.97, "Retail"),
        StockItem("97", "MCD", "McDonald's Corp.", 264.30, 1.90, 0.72, "Retail"),

        // === INDONESIA COMPANIES ===
        StockItem("6", "BBCA.JK", "Bank Central Asia", 9500.0, 75.0, 0.80, "Finance"),
        StockItem("7", "BBRI.JK", "Bank Rakyat Indonesia", 4800.0, 20.0, 0.42, "Finance"),
        StockItem("8", "GOTO.JK", "GoTo Gojek Tokopedia", 62.0, -1.0, -1.59, "Technology"),
        StockItem("9", "TLKM.JK", "Telkom Indonesia", 3100.0, 30.0, 0.98, "Communication"),
        StockItem("41", "BMRI.JK", "Bank Mandiri (Persero)", 6200.0, 50.0, 0.81, "Finance"),
        StockItem("42", "BBNI.JK", "Bank Negara Indonesia", 4700.0, -40.0, -0.84, "Finance"),
        StockItem("43", "ASII.JK", "Astra International", 4500.0, 30.0, 0.67, "Automotive"),
        StockItem("44", "UNVR.JK", "Unilever Indonesia", 2500.0, 10.0, 0.40, "Consumer"),
        StockItem("45", "KLBF.JK", "Kalbe Farma", 1450.0, -15.0, -1.02, "Healthcare"),
        StockItem("46", "ADRO.JK", "Adaro Energy", 2800.0, 70.0, 2.56, "Energy"),
        StockItem("47", "PTBA.JK", "Bukit Asam", 2500.0, -20.0, -0.79, "Energy"),
        StockItem("48", "INDF.JK", "Indofood Sukses", 6100.0, 50.0, 0.83, "Consumer"),
        StockItem("49", "ICBP.JK", "Indofood CBP", 10500.0, 150.0, 1.45, "Consumer"),
        StockItem("50", "AMRT.JK", "Sumber Alfaria Trijaya", 2800.0, 20.0, 0.72, "Retail"),
        StockItem("60", "BRIS.JK", "Bank Syariah Indonesia", 2200.0, 35.0, 1.62, "Finance"),
        StockItem("61", "PGAS.JK", "Perusahaan Gas Negara", 1500.0, -20.0, -1.32, "Energy"),
        StockItem("62", "ANTM.JK", "Aneka Tambang", 1300.0, 35.0, 2.76, "Energy"),
        StockItem("63", "TINS.JK", "Timah Tbk", 950.0, -12.0, -1.25, "Energy"),
        StockItem("64", "HRUM.JK", "Harum Energy", 1250.0, 30.0, 2.46, "Energy"),
        StockItem("65", "UNTR.JK", "United Tractors", 21500.0, 350.0, 1.65, "Automotive"),
        StockItem("66", "JSMR.JK", "Jasa Marga Tbk", 4800.0, 50.0, 1.05, "Infrastructure"),
        StockItem("67", "ADHI.JK", "Adhi Karya (Persero)", 280.0, -3.0, -1.06, "Infrastructure"),
        StockItem("68", "WIKA.JK", "Wijaya Karya", 180.0, -5.0, -2.70, "Infrastructure"),
        StockItem("69", "PTPP.JK", "PP (Persero)", 380.0, 5.0, 1.33, "Infrastructure"),
        StockItem("70", "SMGR.JK", "Semen Indonesia", 3900.0, 50.0, 1.30, "Consumer"),
        StockItem("71", "INCO.JK", "Vale Indonesia Tbk", 3800.0, 60.0, 1.60, "Energy"),
        StockItem("72", "BDMN.JK", "Bank Danamon Tbk", 2500.0, -20.0, -0.79, "Finance"),
        StockItem("73", "BNGA.JK", "Bank CIMB Niaga", 1800.0, 30.0, 1.69, "Finance"),
        StockItem("74", "CPIN.JK", "Charoen Pokphand Indonesia", 4900.0, 50.0, 1.03, "Consumer"),
        StockItem("75", "EXCL.JK", "XL Axiata Tbk", 2200.0, -30.0, -1.35, "Communication"),
        StockItem("76", "ISAT.JK", "Indosat Ooredoo Hutchison", 8500.0, 150.0, 1.80, "Communication"),
        StockItem("77", "JPFA.JK", "Japfa Comfeed Indonesia", 1250.0, 15.0, 1.21, "Consumer"),
        StockItem("78", "MEDC.JK", "Medco Energi Internasional", 1150.0, 35.0, 3.14, "Energy"),
        StockItem("79", "MAPI.JK", "Mitra Adiperkasa", 1450.0, 20.0, 1.40, "Retail"),
        StockItem("80", "MYOR.JK", "Mayora Indah Tbk", 2600.0, 10.0, 0.39, "Consumer"),
        StockItem("81", "SCMA.JK", "Surya Citra Media", 120.0, -2.0, -1.64, "Communication"),
        StockItem("82", "TOWR.JK", "Sarana Menara Nusantara", 750.0, 5.0, 0.67, "Communication"),
        StockItem("83", "TBIG.JK", "Tower Bersama Infrastructure", 1700.0, -20.0, -1.16, "Communication"),
        StockItem("84", "HEAL.JK", "Medikaloka Hermina", 1350.0, 15.0, 1.12, "Healthcare"),
        StockItem("85", "MIKA.JK", "Mitra Keluarga Karyasehat", 2800.0, 40.0, 1.45, "Healthcare")
    )
}

fun getMarketStats(ticker: String, currentPrice: Double): StockStats {
    val shares = when (ticker.uppercase().replace(".JK", "")) {
        "AAPL" -> 15600000000L
        "MSFT" -> 7430000000L
        "GOOGL" -> 12500000000L
        "NVDA" -> 24600000000L
        "TSLA" -> 3180000000L
        "TM" -> 1350000000L
        "RACE" -> 180000000L
        "MC" -> 500000000L
        "META" -> 2540000000L
        "AMZN" -> 10300000000L
        "JPM" -> 2880000000L
        "BAC" -> 7850000000L
        "WMT" -> 2690000000L
        "JNJ" -> 2600000000L
        "V" -> 2050000000L
        "PG" -> 2400000000L
        "MA" -> 930000000L
        "HD" -> 1000000000L
        "CVX" -> 1850000000L
        "LLY" -> 950000000L
        "BBCA" -> 123200000000L
        "BBRI" -> 151500000000L
        "GOTO" -> 1200000000000L
        "TLKM" -> 99060000000L
        "BMRI" -> 93330000000L
        "BBNI" -> 37290000000L
        "ASII" -> 40480000000L
        "UNVR" -> 38150000000L
        "KLBF" -> 46870000000L
        "ADRO" -> 31980000000L
        "PTBA" -> 11520000000L
        "INDF" -> 8780000000L
        "ICBP" -> 11660000000L
        "AMRT" -> 41520000000L
        "BRIS" -> 46130000000L
        "PGAS" -> 24240000000L
        "ANTM" -> 24030000000L
        "TINS" -> 7440000000L
        "HRUM" -> 13520000000L
        "UNTR" -> 3730000000L
        "JSMR" -> 7250000000L
        "ADHI" -> 840000000L
        "WIKA" -> 897000000L
        "PTPP" -> 620000000L
        "SMGR" -> 675000000L
        "INCO" -> 9930000000L
        "BDMN" -> 9770000000L
        "BNGA" -> 25130000000L
        "CPIN" -> 16398000000L
        "EXCL" -> 13110000000L
        "ISAT" -> 8060000000L
        "JPFA" -> 11720000000L
        "MEDC" -> 25160000000L
        "MAPI" -> 1660000000L
        "MYOR" -> 22350000000L
        "SCMA" -> 14780000000L
        "TOWR" -> 51010000000L
        "TBIG" -> 22650000000L
        "HEAL" -> 14960000000L
        "MIKA" -> 14240000000L
        else -> 4800000000L
    }

    val hash = Math.abs(ticker.hashCode()).coerceAtLeast(1)
    
    val divYield = when {
        ticker.contains(".JK") -> 1.5 + (hash % 45) / 10.0
        else -> 0.1 + (hash % 35) / 10.0
    }
    
    val peRatio = when {
        ticker.contains("AAPL") || ticker.contains("MSFT") || ticker.contains("NVDA") -> 28.0 + (hash % 30)
        ticker.contains(".JK") -> 8.0 + (hash % 15)
        else -> 12.0 + (hash % 20)
    }

    val highDaily = currentPrice * (1.0 + (hash % 15) / 400.0)
    val lowDaily = currentPrice * (1.0 - (hash % 12) / 400.0)

    return StockStats(
        sharesOutstanding = shares,
        dividendYield = divYield,
        peRatio = peRatio,
        highToday = highDaily,
        lowToday = lowDaily
    )
}

fun getSimulatedHistory(ticker: String, currentPrice: Double, interval: String, pointsCount: Int = 40): List<Double> {
    val intervalSeed = when (interval) {
        "1D" -> 101L
        "1W" -> 202L
        "1M" -> 303L
        "3M" -> 404L
        "1Y" -> 505L
        else -> 606L
    }
    val seed = Math.abs(ticker.hashCode()).toLong() * 31 + intervalSeed
    val random = java.util.Random(seed)
    val history = mutableListOf<Double>()
    
    val startFactor = when (interval) {
        "1D" -> 0.985 + (random.nextDouble() * 0.03)
        "1W" -> 0.96 + (random.nextDouble() * 0.06)
        "1M" -> 0.92 + (random.nextDouble() * 0.12)
        "3M" -> 0.85 + (random.nextDouble() * 0.25)
        "1Y" -> 0.70 + (random.nextDouble() * 0.50)
        else -> 0.95
    }
    
    var tempVal = currentPrice * startFactor
    
    for (i in 0 until pointsCount) {
        val fraction = i.toDouble() / (pointsCount - 1)
        val volt = when (interval) {
            "1D" -> 0.005
            "1W" -> 0.012
            "1M" -> 0.02
            "3M" -> 0.03
            "1Y" -> 0.05
            else -> 0.01
        }
        val fluctuation = (random.nextDouble() - 0.495) * volt
        tempVal = tempVal * (1.0 + fluctuation)
        history.add(tempVal)
    }
    
    if (history.isNotEmpty()) {
        history[history.lastIndex] = currentPrice
    }
    return history
}

