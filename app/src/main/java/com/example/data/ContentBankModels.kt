package com.example.data

import java.util.UUID
import kotlin.random.Random

enum class ContentType(val displayName: String, val categoryTag: String) {
    SHORT_FILM("Short Film", "Sinema Pendek"),
    DEEP_DIVE_ESSAY("Deep-Dive Essay", "Video Essay"),
    DOCUMENTARY("Dokumenter", "Dokumenter Investigatif")
}

enum class ContentStatus(val displayName: String) {
    AVAILABLE("Tersedia"),
    ACQUIRED_LUMP_SUM("Diakuisisi - Jual Putus"),
    LICENSED("Lisensi Berjalan")
}

enum class ContentSortOption(val displayName: String) {
    ENGAGEMENT_HIGHEST("Engagement Teratas"),
    ROYALTY_HIGHEST("Royalti Tertinggi"),
    EXPIRING_SOON("Kontrak Segera Habis"),
    BUDGET_HIGHEST("Budget Produksi Terbesar")
}

enum class CoProductionFundingScheme(val displayName: String, val shortDesc: String) {
    FULL_CREATOR("100% Dana Kreator", "100% dari Kas Kreator. Profit penuh masuk ke Kreator."),
    FULL_STUDIO("100% Dana Studio Film", "100% dari Kas Studio Film yang dipilih. Kreator sebagai konseptor."),
    JOINT_VENTURE_50_50("Joint Venture (50/50)", "50% dari Kas Kreator & 50% dari Kas Studio Film.")
}

data class ContentWork(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val type: ContentType = ContentType.SHORT_FILM,
    val budget: Long = 0L,
    val engagementScore: Int? = 50, // 1 - 100, or null if unreleased / in production
    val status: ContentStatus = ContentStatus.AVAILABLE,
    val monthlyRoyalty: Long = 0L,
    val acquiredLumpSum: Long = 0L,
    val acquiredByPH: String? = null,
    val contractDurationMonths: Int? = null,
    val remainingContractMonths: Int? = null,
    val partnerStudioName: String? = null,
    val partnerStudioId: String? = null,
    val fundingScheme: CoProductionFundingScheme? = null,
    val isShadowRecord: Boolean = false,
    val movieProjectId: String? = null,
    val createdTimestamp: Long = System.currentTimeMillis()
)

data class ProductionHouseOffer(
    val id: String = UUID.randomUUID().toString(),
    val phName: String = "",
    val phTier: String = "Major Global Studio",
    val contentId: String = "",
    val contentTitle: String = "",
    val contentType: ContentType = ContentType.SHORT_FILM,
    val engagementScore: Int = 50,
    val lumpSumOffer: Long = 0L,
    val royaltyUpfront: Long = 0L,
    val monthlyRoyalty: Long = 0L,
    val contractDurationMonths: Int = 24, // 12, 24, or 36 months
    val pitchMessage: String = "",
    val durationSeconds: Int = 20,
    val createdTimestamp: Long = System.currentTimeMillis()
)

object ContentProductionEngine {

    fun calculateEngagementScore(budget: Long, channelLevel: Int, employees: Int): Int {
        // Budget baseline evaluation:
        val budgetFactor = when {
            budget >= 250_000L -> 82
            budget >= 100_000L -> 75
            budget >= 50_000L -> 68
            budget >= 20_000L -> 60
            budget >= 5_000L -> 50
            budget >= 2_000L -> 42
            else -> 32
        }

        // Studio Level influence (up to +12)
        val levelBonus = (channelLevel / 8).coerceAtMost(12)

        // Employees influence (up to +14)
        val employeeBonus = (employees * 1.5).toInt().coerceAtMost(14)

        // RNG variance (-6 to +12)
        val rng = Random.nextInt(-6, 13)

        return (budgetFactor + levelBonus + employeeBonus + rng).coerceIn(1, 100)
    }

    private val MAJOR_PH_NAMES = listOf(
        "Warner Bros. Pictures",
        "Netflix Originals",
        "A24 Studios",
        "Paramount Pictures",
        "Universal Pictures",
        "Sony Pictures Entertainment",
        "Falcon Pictures",
        "Starvision Plus",
        "Lionsgate Films",
        "Blumhouse Productions",
        "HBO Max Studios",
        "Amazon MGM Studios",
        "Disney+ Originals",
        "Apple Original Films"
    )

    fun generateTargetedOffer(
        portfolio: List<ContentWork>,
        channelLevel: Int = 1,
        forceTarget: ContentWork? = null
    ): ProductionHouseOffer? {
        val eligibleWorks = portfolio.filter {
            it.status == ContentStatus.AVAILABLE &&
            !it.isShadowRecord &&
            it.engagementScore != null &&
            it.engagementScore > 0
        }
        if (eligibleWorks.isEmpty() && forceTarget == null) return null

        val targetWork = if (forceTarget != null) {
            if (forceTarget.isShadowRecord || forceTarget.status != ContentStatus.AVAILABLE || forceTarget.engagementScore == null || forceTarget.engagementScore <= 0) {
                return null
            }
            forceTarget
        } else {
            // Weighted random selection based on engagement score squared
            val totalWeight = eligibleWorks.sumOf {
                val s = it.engagementScore ?: 50
                (s * s).coerceAtLeast(1)
            }
            var randomWeight = Random.nextInt(totalWeight)
            var selected: ContentWork = eligibleWorks.first()
            for (work in eligibleWorks) {
                val s = work.engagementScore ?: 50
                val weight = (s * s).coerceAtLeast(1)
                if (randomWeight < weight) {
                    selected = work
                    break
                }
                randomWeight -= weight
            }
            selected
        }

        val phName = MAJOR_PH_NAMES.random()
        val score = targetWork.engagementScore ?: 50
        val budget = targetWork.budget

        // Multiplier based on score tier
        val scoreMultiplier = when {
            score >= 90 -> Random.nextDouble(5.0, 9.5) // Masterpiece
            score >= 75 -> Random.nextDouble(3.2, 5.2) // Hit
            score >= 50 -> Random.nextDouble(2.0, 3.5) // Solid
            else -> Random.nextDouble(1.3, 2.2)        // Indie
        }

        val baseValuation = (budget * 1.6) + (score * 12_000L * (channelLevel / 5.0 + 1.0))
        val rawLumpSum = (baseValuation * scoreMultiplier).toLong()
        val lumpSumOffer = roundToNiceNumber(rawLumpSum.coerceAtLeast(budget * 2 + 10_000L))

        // Royalty option: 25-35% upfront cash + monthly recurring royalty (MRR)
        val royaltyUpfront = roundToNiceNumber((lumpSumOffer * Random.nextDouble(0.22, 0.35)).toLong().coerceAtLeast(5_000L))
        val monthlyRoyalty = roundToNiceNumber((lumpSumOffer * Random.nextDouble(0.045, 0.085) * (score / 65.0)).toLong().coerceAtLeast(1_500L))
        val contractDurationMonths = listOf(12, 24, 36).random()

        val pitchPitch = when (targetWork.type) {
            ContentType.SHORT_FILM -> "Studio $phName sangat tertarik dengan karya \"${targetWork.title}\" milik Anda! Mereka ingin mengadaptasinya menjadi proyek layar lebar."
            ContentType.DEEP_DIVE_ESSAY -> "Studio $phName terpukau dengan kedalaman materi \"${targetWork.title}\" milik Anda! Mereka ingin mengadaptasinya menjadi serial dokumenter eksklusif."
            ContentType.DOCUMENTARY -> "Studio $phName tertarik dengan investigasi mendalam pada \"${targetWork.title}\" milik Anda! Mereka ingin membeli lisensi distribusi global ke bioskop & OTT."
        }

        return ProductionHouseOffer(
            phName = phName,
            phTier = if (score >= 90) "Hollywood Prestige Studio" else "Major Production House",
            contentId = targetWork.id,
            contentTitle = targetWork.title,
            contentType = targetWork.type,
            engagementScore = score,
            lumpSumOffer = lumpSumOffer,
            royaltyUpfront = royaltyUpfront,
            monthlyRoyalty = monthlyRoyalty,
            contractDurationMonths = contractDurationMonths,
            pitchMessage = pitchPitch,
            durationSeconds = 20
        )
    }

    private fun roundToNiceNumber(value: Long): Long {
        return when {
            value > 10_000_000 -> (value / 100_000) * 100_000
            value > 1_000_000 -> (value / 25_000) * 25_000
            value > 100_000 -> (value / 5_000) * 5_000
            value > 10_000 -> (value / 500) * 500
            else -> (value / 100) * 100
        }
    }
}
