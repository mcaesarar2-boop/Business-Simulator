package com.example.data

val businessCatalog: List<BusinessCatalogItem> = listOf(
        BusinessCatalogItem(
            id = "content_creator", name = "Content Creator", category = BusinessCategory.ENTERTAINMENT,
            imageUrl = "https://images.unsplash.com/photo-1611162617474-5b21e879e113?auto=format&fit=crop&w=400&q=80",
            costToBuy = 500, monthlyRevenue = 0, monthlyMaintenanceCost = 0,
            upgrades = emptyList() // Diurus mandiri di ContentCreatorScreen
        ),
        // UMKM & ENTERPRISE LEVEL
        BusinessCatalogItem(
            id = "fine_dining", name = "F&B Restaurant Chain", category = BusinessCategory.CULINARY,
            imageUrl = "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?auto=format&fit=crop&w=400&q=80",
            costToBuy = 50000, monthlyRevenue = 5000, monthlyMaintenanceCost = 2000,
            upgrades = emptyList()
        ),
        BusinessCatalogItem(
            id = "construction", name = "Construction Firm", category = BusinessCategory.PROPERTY,
            imageUrl = "https://images.unsplash.com/photo-1503387762-592deb58ef4e?auto=format&fit=crop&w=400&q=80",
            costToBuy = 1000000, monthlyRevenue = 0, monthlyMaintenanceCost = 50000,
            upgrades = emptyList()
        ),
        
        // RETAIL & SHOPS
        BusinessCatalogItem(
            id = "shop_local", name = "Toko Kelontong Lokal", category = BusinessCategory.RETAIL,
            imageUrl = "https://images.unsplash.com/photo-1534723452862-4c874018d66d?auto=format&fit=crop&w=400&q=80",
            costToBuy = 2000, monthlyRevenue = 1500, monthlyMaintenanceCost = 500,
            upgrades = listOf(
                BusinessUpgrade("1", "Tingkatkan Kapasitas Toko", "Perbanyak rak dan variasi barang (+500 Rev/Level)", baseCost = 1000, costMultiplier = 1.15f, revenueFlatBoost = 500, maxLevel = 20)
            )
        ),
        BusinessCatalogItem(
            id = "shop_small_chain", name = "Minimarket (Small Chain)", category = BusinessCategory.RETAIL,
            imageUrl = "https://images.unsplash.com/photo-1542838132-92c53300491e?auto=format&fit=crop&w=400&q=80",
            costToBuy = 15000, monthlyRevenue = 8000, monthlyMaintenanceCost = 3000,
            upgrades = listOf(
                BusinessUpgrade("1", "Buka Gerai Baru", "Buka cabang minimarket baru di sekitar kota (+2500 Rev/Level)", baseCost = 5000, costMultiplier = 1.2f, revenueFlatBoost = 2500, maxLevel = 40)
            )
        ),
        BusinessCatalogItem(
            id = "shop_large_chain", name = "Supermarket (Large Chain)", category = BusinessCategory.RETAIL,
            imageUrl = "https://images.unsplash.com/photo-1578916171728-46686eac8d58?auto=format&fit=crop&w=400&q=80",
            costToBuy = 100000, monthlyRevenue = 60000, monthlyMaintenanceCost = 25000,
            upgrades = listOf(
                BusinessUpgrade("1", "Ekspansi Nasional", "Targetkan kota-kota besar untuk cabang baru (+15000 Rev/Level)", baseCost = 45000, costMultiplier = 1.25f, revenueFlatBoost = 15000, maxLevel = 60)
            )
        ),
        BusinessCatalogItem(
            id = "shop_department_store", name = "Mega Department Store", category = BusinessCategory.RETAIL,
            imageUrl = "https://images.unsplash.com/photo-1567401893414-76b7b1e5a7a5?auto=format&fit=crop&w=400&q=80",
            costToBuy = 1000000, monthlyRevenue = 400000, monthlyMaintenanceCost = 150000,
            upgrades = listOf(
                BusinessUpgrade("1", "Tambah Lantai & Zona Belanja", "Kembangkan skala gedung menjadi pusat perbelanjaan raksasa (+80000 Rev/Level)", baseCost = 300000, costMultiplier = 1.3f, revenueFlatBoost = 80000, maxLevel = 100)
            )
        ),

        // MENENGAH LEVEL
        BusinessCatalogItem(
            id = "mid_cafe", name = "Indie Coffee Shop", category = BusinessCategory.CULINARY,
            imageUrl = "https://images.unsplash.com/photo-1554118811-1e0d58224f24?auto=format&fit=crop&w=400&q=80",
            costToBuy = 15000, monthlyRevenue = 8000, monthlyMaintenanceCost = 3000,
            upgrades = listOf(
                BusinessUpgrade("1", "Mesin Espresso Italy", "Ekstraksi maksimal (1.3x Rev)", baseCost = 10000, revenueMultiplier = 1.3f),
                BusinessUpgrade("2", "Acoustic Live Music", "Rame di malam hari (+4000 Rev)", baseCost = 5000, revenueFlatBoost = 4000)
            )
        ),
        BusinessCatalogItem(
            id = "mid_logistics", name = "Kurir Ekspres Lokal", category = BusinessCategory.LOGISTICS,
            imageUrl = "https://images.unsplash.com/photo-1566576912321-d58ddd7a6088?auto=format&fit=crop&w=400&q=80",
            costToBuy = 25000, monthlyRevenue = 15000, monthlyMaintenanceCost = 8000,
            upgrades = listOf(
                BusinessUpgrade("1", "Armada Mobil Box", "Kapasitas kirim ++ (1.5x Rev)", baseCost = 20000, revenueMultiplier = 1.5f),
                BusinessUpgrade("2", "Optimasi Rute AI", "Hemat bensin (-30% Biaya)", baseCost = 10000, maintenanceMultiplier = 0.7f)
            )
        ),

        // ATAS LEVEL
        BusinessCatalogItem(
            id = "upper_tech", name = "App Dev Agency", category = BusinessCategory.TECHNOLOGY,
            imageUrl = "https://images.unsplash.com/photo-1519389950473-47ba0277781c?auto=format&fit=crop&w=400&q=80",
            costToBuy = 150000, monthlyRevenue = 85000, monthlyMaintenanceCost = 40000, isFluctuating = true,
            upgrades = listOf(
                BusinessUpgrade("1", "Sewa Hacker Silicon Valley", "Produk A-Class (+50,000 Rev)", baseCost = 100000, revenueFlatBoost = 50000),
                BusinessUpgrade("2", "Marketing Global", "Klien mancanegara (2x Rev)", baseCost = 150000, revenueMultiplier = 2.0f)
            )
        ),
        BusinessCatalogItem(
            id = "upper_realestate", name = "Kompleks Apartemen", category = BusinessCategory.PROPERTY,
            imageUrl = "https://images.unsplash.com/photo-1545324418-cc1a3fa10c00?auto=format&fit=crop&w=400&q=80",
            costToBuy = 500000, monthlyRevenue = 120000, monthlyMaintenanceCost = 20000,
            upgrades = listOf(
                BusinessUpgrade("1", "Kolam Renang Rooftop", "Harga sewa naik drastis (+80000 Rev)", baseCost = 250000, revenueFlatBoost = 80000),
                BusinessUpgrade("2", "Security 24 Jam", "Zero komplain (-50% Biaya)", baseCost = 50000, maintenanceMultiplier = 0.5f)
            )
        ),

        // TYCOON LEVEL
        BusinessCatalogItem(
            id = "tycoon_bank", name = "Bank Swasta Nasional", category = BusinessCategory.FINANCE,
            imageUrl = "https://images.unsplash.com/photo-1501167733283-6c6fd31ab62c?auto=format&fit=crop&w=400&q=80",
            costToBuy = 50000000, monthlyRevenue = 8500000, monthlyMaintenanceCost = 2000000,
            upgrades = listOf(
                BusinessUpgrade("1", "Aplikasi Mobile Banking", "Jutaan nasabah ritel (1.5x Rev)", baseCost = 20000000, revenueMultiplier = 1.5f),
                BusinessUpgrade("2", "Akuisisi Bank Kecil", "Monopoli pasar (+5,000,000 Rev)", baseCost = 35000000, revenueFlatBoost = 5000000)
            )
        ),
        BusinessCatalogItem(
            id = "tycoon_corp", name = "Konglomerasi Multinasional", category = BusinessCategory.CORPORATION,
            imageUrl = "https://images.unsplash.com/photo-1486406146926-c627a92ad1ab?auto=format&fit=crop&w=400&q=80",
            costToBuy = 250000000, monthlyRevenue = 45000000, monthlyMaintenanceCost = 15000000,
            upgrades = listOf(
                BusinessUpgrade("1", "Lobi Politik Tingkat Tinggi", "Bebas pajak, proyek pemerintah (1.5x Rev)", baseCost = 100000000, revenueMultiplier = 1.5f, maintenanceMultiplier = 0.5f),
                BusinessUpgrade("2", "Ekspansi Antariksa", "Tambang asteroid (2x Rev)", baseCost = 500000000, revenueMultiplier = 2.0f)
            )
        ),
        
        // MEDIA EMPIRE
        BusinessCatalogItem(
            id = "media_print", name = "Print & Digital Media", category = BusinessCategory.ENTERTAINMENT,
            imageUrl = "https://images.unsplash.com/photo-1504711434969-e33886168f5c?auto=format&fit=crop&w=400&q=80",
            costToBuy = 10000, monthlyRevenue = 2000, monthlyMaintenanceCost = 500,
            upgrades = listOf(
                BusinessUpgrade("1", "Meningkatkan Oplah & Ekspansi Portal Web", "Meningkatkan kecepatan publikasi dan audiens ritel (+1500 Rev / Level)", baseCost = 5000, costMultiplier = 1.15f, revenueFlatBoost = 1500, maxLevel = 100)
            )
        ),
        BusinessCatalogItem(
            id = "media_radio", name = "Mega Event Organizer", category = BusinessCategory.ENTERTAINMENT,
            imageUrl = "https://images.unsplash.com/photo-1492684223066-81342ee5ff30?auto=format&fit=crop&w=400&q=80",
            costToBuy = 50000, monthlyRevenue = 8000, monthlyMaintenanceCost = 2500,
            upgrades = listOf(
                BusinessUpgrade("1", "Ekspansi Wilayah & Lisensi Internasional", "Memperluas kapasitas event (+5000 Rev / Level)", baseCost = 20000, costMultiplier = 1.25f, revenueFlatBoost = 5000, maxLevel = 100)
            )
        ),
        BusinessCatalogItem(
            id = "media_tv", name = "TV Station & Streaming", category = BusinessCategory.ENTERTAINMENT,
            imageUrl = "https://images.unsplash.com/photo-1593784991095-a205069470b6?auto=format&fit=crop&w=400&q=80",
            costToBuy = 100000, monthlyRevenue = 22000, monthlyMaintenanceCost = 8000,
            upgrades = listOf(
                BusinessUpgrade("1", "Satelit Broadcasting & Platform On-Demand", "Menguasai jam prime time dan layanan berlangganan (+18000 Rev / Level)", baseCost = 50000, costMultiplier = 1.25f, revenueFlatBoost = 18000, maxLevel = 100)
            )
        ),
        BusinessCatalogItem(
            id = "media_production", name = "Film Production Company", category = BusinessCategory.ENTERTAINMENT,
            imageUrl = "https://images.unsplash.com/photo-1598899134739-24c46f58b8c0?auto=format&fit=crop&w=400&q=80",
            costToBuy = 500000, monthlyRevenue = 150000, monthlyMaintenanceCost = 50000,
            upgrades = listOf(
                BusinessUpgrade("1", "Akuisisi Studio CGI & Lighting Profesional", "Menghasilkan film box office berkelas Hollywood (+50000 Rev / Level)", baseCost = 250000, costMultiplier = 1.3f, revenueFlatBoost = 50000, maxLevel = 100)
            )
        ),
        BusinessCatalogItem(
            id = "healthcare", name = "Healthcare & Protection Group", category = BusinessCategory.CORPORATION,
            imageUrl = "https://images.unsplash.com/photo-1519494026892-80bbd2d6fd0d?auto=format&fit=crop&w=400&q=80",
            costToBuy = 2000000, monthlyRevenue = 0, monthlyMaintenanceCost = 150000,
            upgrades = emptyList()
        ),
        BusinessCatalogItem(
            id = "aviation_group", name = "Aviation Group (Maskapai)", category = BusinessCategory.AVIATION,
            imageUrl = "https://images.unsplash.com/photo-1436491865332-7a61a109cc05?auto=format&fit=crop&w=400&q=80",
            costToBuy = 50000000, monthlyRevenue = 0, monthlyMaintenanceCost = 0,
            upgrades = emptyList()
        ),
        BusinessCatalogItem(
            id = "theme_park_holding", name = "Universal Theme Park & Leisure", category = BusinessCategory.THEME_PARK_HOLDING,
            imageUrl = "https://images.unsplash.com/photo-1502136969935-8d8eef54d77b?q=80&w=1169&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D",
            costToBuy = 50000000, monthlyRevenue = 0, monthlyMaintenanceCost = 500000,
            upgrades = emptyList()
        ),
        BusinessCatalogItem(
            id = "hospitality_holding", name = "Global Hospitality & Resort Holding", category = BusinessCategory.HOSPITALITY,
            imageUrl = "https://plus.unsplash.com/premium_photo-1687960116497-0dc41e1808a2?q=80&w=1171&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D",
            costToBuy = 100_000_000, monthlyRevenue = 0, monthlyMaintenanceCost = 1_000_000,
            upgrades = emptyList()
        ),
        BusinessCatalogItem(
            id = "cruise_line_holding", name = "Oceanic Cruise Line Group", category = BusinessCategory.CRUISE_LINE,
            imageUrl = "https://images.unsplash.com/photo-1559600088-01f7d8974913?q=80&w=1171&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D",
            costToBuy = 150_000_000, monthlyRevenue = 0, monthlyMaintenanceCost = 1_500_000,
            upgrades = emptyList()
        )
    )
