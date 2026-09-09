package com.artswipe

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.artswipe.data.local.ArtDatabase
import com.artswipe.data.local.model.ArtworkEntity
import com.artswipe.data.local.model.SwipeRecordEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ArtworkDaoTest {
    private lateinit var database: ArtDatabase
    @Before fun setUp() {
        database = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getInstrumentation().targetContext,
            ArtDatabase::class.java).build()
    }
    @After fun tearDown() = database.close()
    private fun art(id: String) = ArtworkEntity(id, "test", "Artwork $id", "Artist", null,
        "https://example.com/art.jpg", "Abstract", null, "", null)

    @Test fun historiesAndResetsAreIsolatedByAccount() = runBlocking {
        val dao = database.artworkDao()
        dao.insertArtworks(listOf(art("one")))
        dao.replaceSwipeRecord(SwipeRecordEntity(userId = "a", artworkId = "one", liked = true, timestamp = 1, styleMovement = "Abstract"))
        assertEquals(1, dao.getLikedArtworks("a").first().size)
        assertTrue(dao.getLikedArtworks("b").first().isEmpty())
        assertEquals(1, dao.getUnswipedArtworks("b").first().size)
        dao.replaceSwipeRecord(SwipeRecordEntity(userId = "b", artworkId = "one", liked = true, timestamp = 2, styleMovement = "Abstract"))
        dao.clearAllSwipeRecords("a")
        assertEquals(1, dao.getLikedArtworks("b").first().size)
        assertTrue(dao.getLikedArtworks("a").first().isEmpty())
    }

    @Test fun replacementCleansLegacyDuplicatesAndUsesLatestChoice() = runBlocking {
        val dao = database.artworkDao()
        dao.insertArtworks(listOf(art("one")))
        val record = SwipeRecordEntity(userId = "a", artworkId = "one", liked = true, timestamp = 1, styleMovement = "Abstract")
        dao.insertSwipeRecord(record)
        dao.insertSwipeRecord(record.copy(timestamp = 2, liked = false))
        assertTrue(dao.getLikedArtworks("a").first().isEmpty())
        dao.replaceSwipeRecord(record.copy(timestamp = 3))
        dao.replaceSwipeRecord(record.copy(timestamp = 4))
        assertEquals(1, dao.getAllSwipeRecords("a").first().size)
        assertEquals(1, dao.getLikedArtworks("a").first().size)
    }

    @Test fun galleryIsNewestFirstAndRefetchPreservesMetadata() = runBlocking {
        val dao = database.artworkDao()
        dao.insertArtworks(listOf(art("one"), art("two")))
        dao.insertArtworks(listOf(art("one").copy(styleMovement = "Wrong random style")))
        assertEquals("Abstract", dao.getArtworkById("one")!!.styleMovement)
        dao.replaceSwipeRecord(SwipeRecordEntity(userId = "a", artworkId = "one", liked = true, timestamp = 1, styleMovement = "Abstract"))
        dao.replaceSwipeRecord(SwipeRecordEntity(userId = "a", artworkId = "two", liked = true, timestamp = 2, styleMovement = "Abstract"))
        assertEquals(listOf("two", "one"), dao.getLikedArtworks("a").first().map { it.id })
    }
}
