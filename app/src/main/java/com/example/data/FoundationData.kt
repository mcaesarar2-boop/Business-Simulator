package com.example.data

import java.util.UUID

enum class FoundationType(val label: String, val legalCost: Long, val setupMonths: Int) {
    EDUCATION("Yayasan Pendidikan & Riset", 5_000_000L, 3),
    HEALTHCARE("Yayasan Kesehatan & Medis", 10_000_000L, 6),
    HUMANITARIAN("Badan Amal & Kemanusiaan Terpadu", 2_000_000L, 2)
}

data class FoundationEntity(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: FoundationType,
    val isLegalized: Boolean = false,
    val constructionMonthsLeft: Int,
    val endowmentFund: Long = 0L, // Dana abadi yayasan
    val facilities: List<FoundationFacility> = emptyList()
)

data class FoundationFacility(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val category: String, // Cth: "Sekolah Dasar", "RSUD", etc.
    val tier: String, // Cth: "Subsidi", "Swasta Nasional", "Internasional"
    val buildCost: Long,
    val buildMonthsLeft: Int,
    val monthlyOperationalCost: Long, // Memotong Dana Abadi (Endowment)
    val prestigeReward: Long = 0L, // Menghasilkan Legacy Points setelah beroperasi
    val isOperational: Boolean = false
)

// Standard blueprints for facilities
data class FacilityBlueprint(
    val category: String,
    val baseCost: Long,
    val buildMonths: Int,
    val baseMonthlyOps: Long,
    val basePrestige: Long,
    val description: String
)

object FoundationBlueprints {
    val blueprints = mapOf(
        FoundationType.EDUCATION to listOf(
            FacilityBlueprint("Sekolah Dasar (SD)", 1_000_000L, 2, 20_000L, 100L, "Pendidikan dasar bagi tunas bangsa."),
            FacilityBlueprint("Sekolah Menengah (SMP/SMA)", 3_500_000L, 3, 60_000L, 300L, "Jenjang menengah untuk membentuk karakter."),
            FacilityBlueprint("Universitas & Kampus Riset", 15_000_000L, 8, 300_000L, 1500L, "Institusi pendidikan tinggi & pusat inovasi ilmu pengetahuan.")
        ),
        FoundationType.HEALTHCARE to listOf(
            FacilityBlueprint("Klinik Pelosok Desa", 500_000L, 2, 12_000L, 80L, "Pelayanan medis primer bagi masyarakat terpencil."),
            FacilityBlueprint("Rumah Sakit Umum Daerah (RSUD)", 6_000_000L, 5, 120_000L, 600L, "Fasilitas rawat inap & pembedahan skala menengah."),
            FacilityBlueprint("Pusat Riset Kanker Tingkat Lanjut", 20_000_000L, 10, 500_000L, 2200L, "Riset mutakhir untuk pengobatan onkologi global.")
        ),
        FoundationType.HUMANITARIAN to listOf(
            FacilityBlueprint("Dapur Umum & Sandang", 300_000L, 1, 8_000L, 50L, "Bantuan makanan & kebutuhan dasar harian bagi dhuafa."),
            FacilityBlueprint("Panti Asuhan & Jompo Terpadu", 1_500_000L, 3, 30_000L, 200L, "Tempat bernaung layak bagi anak yatim & lansia telantar."),
            FacilityBlueprint("Badan Penanggulangan Bencana", 8_000_000L, 6, 180_000L, 900L, "Satgas tanggap darurat logistik & evakuasi bencana alam.")
        )
    )

    val tiers = listOf(
        FacilityTier("Sekolah Subsidi Rakyat / Klinik Gratis", "Subsidi", 0.5, 0.4, 1.5, "Murni amal nirlaba. Biaya murah, dampak sosial & legacy maksimal."),
        FacilityTier("Swasta Nasional", "Swasta", 1.0, 1.0, 1.0, "Standar kurikulum & fasilitas nasional."),
        FacilityTier("Internasional Elite (Cambridge / Mayo)", "Internasional", 3.0, 2.5, 3.5, "Gengsi legendaris luar biasa, biaya tinggi, reputasi internasional.")
    )
}

data class FacilityTier(
    val name: String,
    val key: String,
    val costMultiplier: Double,
    val opsMultiplier: Double,
    val prestigeMultiplier: Double,
    val description: String
)
