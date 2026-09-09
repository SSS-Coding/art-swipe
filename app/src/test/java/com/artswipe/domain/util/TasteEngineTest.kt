package com.artswipe.domain.util

import com.artswipe.domain.model.User
import org.junit.Assert.*
import org.junit.Test
import kotlin.random.Random

class TasteEngineTest {
    private fun user(scores: Map<String, Int>, dislikes: Map<String, Int> = emptyMap(), swipes: Int = 20) =
        User("id", "Sam", "", "", swipes, scores, dislikes)

    @Test fun identicalDistributionsMatchRegardlessOfVolume() {
        assertEquals(100, TasteEngine.compare(user(mapOf("Abstract" to 6, "Realism" to 2)),
            user(mapOf("Abstract" to 60, "Realism" to 20))).score)
    }

    @Test fun disjointLikesHaveZeroOverlapEvenWithSharedPasses() {
        val a = user(mapOf("Abstract" to 4, "Baroque" to -5), mapOf("Baroque" to 5))
        val b = user(mapOf("Realism" to 4, "Baroque" to -5), mapOf("Baroque" to 5))
        val result = TasteEngine.compare(a, b)
        assertEquals(0, result.score)
        assertEquals(listOf("Baroque"), result.sharedDislikes)
    }

    @Test fun negativeNetScoreStillRecoversRealLikes() {
        val a = user(mapOf("Abstract" to -2), mapOf("Abstract" to 6))
        assertEquals(mapOf("Abstract" to 2), TasteEngine.likeCounts(a))
        assertEquals(100, TasteEngine.compare(a, user(mapOf("Abstract" to 4))).score)
    }

    @Test fun emptyOrOnlyDislikedProfilesHaveNoScore() {
        val liked = user(mapOf("Abstract" to 4))
        assertNull(TasteEngine.compare(user(emptyMap()), liked).score)
        assertNull(TasteEngine.compare(user(mapOf("Realism" to -2), mapOf("Realism" to 2)), liked).score)
        assertNull(TasteEngine.compare(user(emptyMap()), user(emptyMap())).score)
        assertTrue(TasteEngine.compare(user(emptyMap()), liked).differences.isEmpty())
    }

    @Test fun expectedOverlapUsesIndependentDistributions() {
        val a = user(mapOf("Abstract" to 6, "Realism" to 2))
        val b = user(mapOf("Abstract" to 2, "Realism" to 6))
        val result = TasteEngine.compare(a, b)
        assertEquals(50, result.score)
        assertEquals(75f, result.sharedStyles.first { it.style == "Abstract" }.scoreA, 0.001f)
        assertEquals(25f, result.sharedStyles.first { it.style == "Abstract" }.scoreB, 0.001f)
    }

    @Test fun limitedDataChecksBothPeople() {
        val a = user(mapOf("Abstract" to 2), swipes = 3)
        val b = user(mapOf("Abstract" to 20))
        assertTrue(TasteEngine.compare(a, b).isLimitedData)
        assertTrue(TasteEngine.compare(b, a).isLimitedData)
        assertFalse(TasteEngine.compare(b, b).isLimitedData)
    }

    @Test fun styleNamesAreNormalizedBeforeRecoveringCounts() {
        val a = user(mapOf(" abstract " to 1, "ABSTRACT" to 1, "still  life" to 2))
        assertEquals(mapOf("Abstract" to 1, "Still Life" to 1), TasteEngine.likeCounts(a))
        assertEquals(100, TasteEngine.compare(a, user(mapOf("Abstract" to 2, "Still Life" to 2))).score)
    }

    @Test fun zeroDislikesDoNotBecomeSharedPasses() {
        val a = user(mapOf("Abstract" to 2), mapOf("Realism" to 0))
        assertTrue(TasteEngine.compare(a, a).sharedDislikes.isEmpty())
    }

    @Test fun missingMetadataCannotCreateArtificialCompatibility() {
        val unknown = user(mapOf("Unclassified" to 20, " " to 10))
        assertTrue(TasteEngine.likeCounts(unknown).isEmpty())
        assertNull(TasteEngine.compare(unknown, unknown).score)
    }

    @Test fun largeCountsDoNotOverflow() {
        val a = user(mapOf("Abstract" to Int.MAX_VALUE, "Realism" to Int.MAX_VALUE))
        assertEquals(100, TasteEngine.compare(a, a).score)
        assertEquals(100f, TasteEngine.distribution(a).values.sum(), 0.001f)
    }

    @Test fun scoreIsSymmetricAndBoundedAcrossRandomHistories() {
        val random = Random(42)
        fun randomUser(): User {
            val likes = (0..12).associate { "Style $it" to random.nextInt(0, 100) }
            val dislikes = likes.mapValues { random.nextInt(0, 100) }
            return user(likes.mapValues { (style, count) -> 2 * count - dislikes.getValue(style) }, dislikes)
        }
        repeat(500) {
            val a = randomUser(); val b = randomUser()
            val score = TasteEngine.compare(a, b).score!!
            assertTrue(score in 0..100)
            assertEquals(score, TasteEngine.compare(b, a).score)
            assertEquals(100, TasteEngine.compare(a, a).score)
        }
    }
}
