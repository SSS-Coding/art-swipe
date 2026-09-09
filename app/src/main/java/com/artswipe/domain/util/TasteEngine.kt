package com.artswipe.domain.util

import com.artswipe.domain.model.User
import java.util.Locale
import kotlin.math.roundToInt

data class TasteComparison(
    val score: Int?,
    val label: String,
    val sharedStyles: List<SharedStyle>,
    val sharedDislikes: List<String>,
    val differences: List<String>,
    val isLimitedData: Boolean
)

data class SharedStyle(val style: String, val scoreA: Float, val scoreB: Float)

/** Pure scoring logic shared by profile, recommendations and comparison. */
object TasteEngine {
    fun normalizeStyle(style: String): String = style.trim().replace(Regex("\\s+"), " ")
        .lowercase(Locale.ROOT).split(" ").joinToString(" ") { word ->
            word.replaceFirstChar { it.titlecase(Locale.ROOT) }
        }

    private fun aggregate(values: Map<String, Int>): Map<String, Long> = values.entries
        .filter { it.key.isNotBlank() && normalizeStyle(it.key) !in setOf("Unclassified", "Unknown", "None") }
        .groupBy { normalizeStyle(it.key) }
        .mapValues { (_, entries) -> entries.sumOf { it.value.toLong() } }

    // Storage uses score = 2 * likes - dislikes. Recover counts without migration.
    fun likeCounts(user: User): Map<String, Int> {
        val scores = aggregate(user.styleScores)
        val dislikes = aggregate(user.styleDislikes)
        return (scores.keys + dislikes.keys).associateWith { style ->
            (((scores[style] ?: 0L) + (dislikes[style] ?: 0L).coerceAtLeast(0L)) / 2L)
                .coerceIn(0L, Int.MAX_VALUE.toLong()).toInt()
        }.filterValues { it > 0 }
    }

    fun distribution(user: User): Map<String, Float> {
        val likes = likeCounts(user)
        val total = likes.values.sumOf { it.toDouble() }
        return if (total == 0.0) emptyMap() else likes.mapValues { (it.value / total * 100).toFloat() }
    }

    fun compare(a: User, b: User): TasteComparison {
        val pctA = distribution(a)
        val pctB = distribution(b)
        val allStyles = (pctA.keys + pctB.keys).sorted()
        val score = if (pctA.isEmpty() || pctB.isEmpty()) null else allStyles
            .sumOf { minOf(pctA[it] ?: 0f, pctB[it] ?: 0f).toDouble() }
            .roundToInt().coerceIn(0, 100)
        val shared = allStyles.filter { it in pctA && it in pctB }
            .map { SharedStyle(it, pctA.getValue(it), pctB.getValue(it)) }
            .sortedByDescending { minOf(it.scoreA, it.scoreB) }
        val dislikesA = aggregate(a.styleDislikes).filterValues { it > 0 }
        val dislikesB = aggregate(b.styleDislikes).filterValues { it > 0 }
        val otherName = b.displayName.ifBlank { "Your friend" }
        val differences = if (score == null) emptyList() else allStyles.sortedByDescending {
            kotlin.math.abs((pctA[it] ?: 0f) - (pctB[it] ?: 0f))
        }.filter { kotlin.math.abs((pctA[it] ?: 0f) - (pctB[it] ?: 0f)) >= 10f }
            .take(3).map {
                "$it makes up ${(pctA[it] ?: 0f).roundToInt()}% of your likes and " +
                    "${(pctB[it] ?: 0f).roundToInt()}% of $otherName's likes."
            }
        return TasteComparison(
            score = score,
            label = when {
                score == null -> "Still discovering"
                score >= 85 -> "Kindred Spirits"
                score >= 65 -> "Fellow Admirers"
                score >= 45 -> "Curious Contrast"
                score >= 25 -> "Different Perspectives"
                else -> "Distinct Tastes"
            },
            sharedStyles = shared,
            sharedDislikes = dislikesA.keys.intersect(dislikesB.keys).sorted(),
            differences = differences,
            isLimitedData = a.totalSwipes < 10 || b.totalSwipes < 10
        )
    }
}
