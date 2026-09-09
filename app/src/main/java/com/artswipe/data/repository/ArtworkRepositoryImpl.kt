package com.artswipe.data.repository

import com.artswipe.data.local.ArtworkDao
import com.artswipe.data.local.model.ArtworkEntity
import com.artswipe.data.local.model.SwipeRecordEntity
import com.artswipe.data.remote.AicApi
import com.artswipe.data.remote.MetApi
import com.artswipe.domain.model.Artwork
import com.artswipe.domain.model.SwipeRecord
import com.artswipe.domain.repository.ArtworkRepository
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import com.artswipe.domain.repository.AuthRepository
import com.artswipe.domain.util.TasteEngine
import com.google.firebase.firestore.FieldPath
import android.util.Log
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.random.Random

class ArtworkRepositoryImpl @Inject constructor(
    private val metApi: MetApi,
    private val aicApi: AicApi,
    private val artworkDao: ArtworkDao,
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository
) : ArtworkRepository {

    private val swipeMutex = Mutex()
    private val fetchMutex = Mutex()

    private fun userQueue(): Flow<List<ArtworkEntity>> = authRepository.currentUser
        .map { it?.userId }.distinctUntilChanged().flatMapLatest { userId ->
            if (userId == null) flowOf(emptyList()) else artworkDao.getUnswipedArtworks(userId)
        }

    private val styles = listOf(
        "Impressionism", "Baroque", "Modernism", "Surrealism", "Realism",
        "Abstract", "Renaissance", "Landscape", "Portrait", "Mythology",
        "Still Life", "Romanticism", "Contemporary", "Pop Art", "Expressionism"
    )

    override fun getArtworkQueue(): Flow<List<Artwork>> {
        return userQueue().map { entities ->
            // Filter archival records retroactively in case they exist in DB
            entities.filterNot { isArchival(it.title, it.artist, it.department, it.medium, it.imageUrl) }
                .map { it.toDomain() }
        }
    }

    /**
     * Strictest filter to prevent archival records, administrative scans, and non-visual content.
     */
    private fun isArchival(title: String?, artist: String?, dept: String?, medium: String?, imageUrlOrId: String?): Boolean {
        val t = title?.lowercase() ?: ""
        val a = artist?.lowercase() ?: ""
        val d = dept?.lowercase() ?: ""
        val m = medium?.lowercase() ?: ""
        val u = imageUrlOrId ?: ""

        // 1. Blacklist generic AIC Archive placeholder image ID
        if (u.contains("342b2214-04d5-de63-b577-55a08a618960")) return true

        // 2. Blacklist explicitly non-art titles and administrative records
        val blockKeywords = listOf(
            "archive", "collection record", "finding aid", "reference record",
            "library record", "administrative record", "documentation", "manuscript",
            "records of", "papers of", "correspondence", "folder", "envelope",
            "negative", "microfilm", "memo", "pamphlet", "finding-aid", "scrapbook",
            "notebook", "portfolio", "letter to", "memo", "inventory"
        )

        if (blockKeywords.any { t.contains(it) || d.contains(it) || m.contains(it) || a.contains(it) }) return true

        // Skip items with titles that are just numbers or extremely short
        if (t.isBlank() || t.length < 3 || t.all { it.isDigit() || it == '-' || it == '_' || it == ' ' }) return true

        return false
    }

    override suspend fun fetchMoreArtworks() = fetchMutex.withLock { coroutineScope {
        var totalAdded = 0
        var attempts = 0

        // Persistent loop: Keep trying different styles until we have at least 15 valid artworks
        while (totalAdded < 15 && attempts < 8) {
            attempts++
            val style = styles.random()

            // Try Met Museum
            try {
                // Search without department restriction to get a wider pool
                val metSearch = metApi.search(query = style, hasImages = true)
                val ids = metSearch.objectIDs?.shuffled()?.take(30) ?: emptyList()

                val metFetches = ids.map { id ->
                    async {
                        try {
                            val obj = metApi.getObject(id)
                            if (obj.primaryImage?.startsWith("http") == true &&
                                !isArchival(obj.title, obj.artistDisplayName, obj.department, obj.medium, obj.primaryImage)) {
                                ArtworkEntity(
                                    id = "met_${obj.objectID}",
                                    source = "met",
                                    title = obj.title ?: "Untitled",
                                    artist = obj.artistDisplayName ?: "Unknown Artist",
                                    year = obj.objectDate,
                                    imageUrl = obj.primaryImage!!,
                                    styleMovement = style,
                                    medium = obj.medium,
                                    description = "From The Metropolitan Museum of Art collection.",
                                    department = obj.department,
                                    sourceUrl = obj.objectURL,
                                    randomOrder = Random.nextFloat()
                                )
                            } else null
                        } catch (e: CancellationException) { throw e } catch (e: Exception) { null }
                    }
                }
                val valid = metFetches.awaitAll().filterNotNull()
                if (valid.isNotEmpty()) {
                    totalAdded += artworkDao.insertArtworks(valid).count { it != -1L }
                }
            } catch (e: CancellationException) { throw e } catch (e: Exception) { /* Try the other museum. */ }

            // Try AIC
            try {
                // Fetch more items per page to increase hit rate
                val aicResponse = aicApi.getArtworks(page = (1..300).random(), limit = 60)
                val iiifBaseUrl = aicResponse.config.iiif_url
                val aicEntities = aicResponse.data.mapNotNull { aicArt ->
                    val imageId = aicArt.image_id ?: ""
                    // AIC is_boosted identifies high-quality gallery masterpieces
                    if (imageId.isNotEmpty() && aicArt.is_boosted == true &&
                        !isArchival(aicArt.title, aicArt.artist_display, aicArt.department_title, aicArt.classification_title, imageId)) {
                        ArtworkEntity(
                            id = "aic_${aicArt.id}",
                            source = "aic",
                            title = aicArt.title ?: "Untitled",
                            artist = aicArt.artist_display ?: "Unknown Artist",
                            year = aicArt.date_display,
                            imageUrl = "$iiifBaseUrl/$imageId/full/843,/0/default.jpg",
                            styleMovement = aicArt.style_title?.takeIf { it.isNotBlank() } ?: "Unclassified",
                            medium = aicArt.medium_display,
                            description = aicArt.description?.let { android.text.Html.fromHtml(it, android.text.Html.FROM_HTML_MODE_LEGACY).toString().trim() }.orEmpty(),
                            department = aicArt.department_title,
                            sourceUrl = aicArt.websiteUrl,
                            randomOrder = Random.nextFloat()
                        )
                    } else null
                }
                if (aicEntities.isNotEmpty()) {
                    totalAdded += artworkDao.insertArtworks(aicEntities).count { it != -1L }
                }
            } catch (e: CancellationException) { throw e } catch (e: Exception) { /* Try the other museum. */ }

            if (totalAdded < 15) {
                // Short delay to avoid rate limiting during high-frequency retries
                delay(500)
            }
        }

        if (totalAdded == 0) {
            throw Exception("Masterpieces are currently elusive. Check your connection and try again.")
        }
    }

    }

    override suspend fun getArtworkById(id: String): Artwork? {
        return artworkDao.getArtworkById(id)?.toDomain()
    }

    override suspend fun saveSwipeRecord(record: SwipeRecord) = swipeMutex.withLock {
        val previous = artworkDao.getLatestSwipeForArtwork(record.userId, record.artworkId)
        if (previous?.liked == record.liked && previous.styleMovement == record.styleMovement) return@withLock
        artworkDao.replaceSwipeRecord(
            SwipeRecordEntity(
                userId = record.userId,
                artworkId = record.artworkId,
                liked = record.liked,
                timestamp = record.timestamp,
                styleMovement = record.styleMovement
            )
        )

        val artwork = artworkDao.getArtworkById(record.artworkId)
        val swipeData = mutableMapOf(
            "artworkId" to record.artworkId,
            "liked" to record.liked,
            "timestamp" to record.timestamp,
            "styleMovement" to record.styleMovement
        )

        artwork?.let {
            swipeData["title"] = it.title
            swipeData["artist"] = it.artist
            swipeData["imageUrl"] = it.imageUrl
            swipeData["description"] = it.description
            swipeData["source"] = it.source
            swipeData["year"] = it.year ?: ""
            swipeData["medium"] = it.medium ?: ""
            swipeData["department"] = it.department ?: ""
            swipeData["sourceUrl"] = it.sourceUrl ?: ""
        }

        val userRef = firestore.collection("users").document(record.userId)
        val batch = firestore.batch()
        batch.set(userRef.collection("swipes").document(record.artworkId), swipeData)
        batch.update(userRef, "totalSwipes", FieldValue.increment(if (previous == null) 1L else 0L))
        val scoreDeltas = mutableMapOf<String, Long>()
        val dislikeDeltas = mutableMapOf<String, Long>()
        if (previous != null) {
            scoreDeltas[previous.styleMovement] = if (previous.liked) -2L else 1L
            if (!previous.liked) dislikeDeltas[previous.styleMovement] = -1L
        }
        scoreDeltas[record.styleMovement] = (scoreDeltas[record.styleMovement] ?: 0L) + if (record.liked) 2L else -1L
        if (!record.liked) dislikeDeltas[record.styleMovement] = (dislikeDeltas[record.styleMovement] ?: 0L) + 1L
        scoreDeltas.forEach { (style, delta) -> batch.update(userRef, FieldPath.of("styleScores", style), FieldValue.increment(delta)) }
        dislikeDeltas.forEach { (style, delta) -> batch.update(userRef, FieldPath.of("styleDislikes", style), FieldValue.increment(delta)) }
        // Firestore queues this atomic batch offline. Keep discovery responsive.
        batch.commit().addOnFailureListener { Log.e("ArtSwipe", "Could not sync swipe", it) }
        Unit
    }

    override suspend fun removeSwipeRecord(userId: String, artworkId: String, styleMovement: String) = swipeMutex.withLock {
        val lastSwipe = artworkDao.getLatestSwipeForArtwork(userId, artworkId) ?: return@withLock
        artworkDao.deleteSwipeRecordForArtwork(userId, artworkId)
        val userRef = firestore.collection("users").document(userId)
        val batch = firestore.batch()
        batch.delete(userRef.collection("swipes").document(artworkId))
        batch.update(userRef, "totalSwipes", FieldValue.increment(-1))
        batch.update(userRef, FieldPath.of("styleScores", lastSwipe.styleMovement), FieldValue.increment(if (lastSwipe.liked) -2L else 1L))
        if (!lastSwipe.liked) batch.update(userRef, FieldPath.of("styleDislikes", lastSwipe.styleMovement), FieldValue.increment(-1))
        batch.commit().addOnFailureListener { Log.e("ArtSwipe", "Could not sync removed swipe", it) }
        Unit
    }
    override fun getLikedArtworks(): Flow<List<Artwork>> {
        return authRepository.currentUser.map { it?.userId }.distinctUntilChanged().flatMapLatest { userId ->
            if (userId == null) flowOf(emptyList()) else artworkDao.getLikedArtworks(userId)
        }.map { entities ->
            entities.filterNot { isArchival(it.title, it.artist, it.department, it.medium, it.imageUrl) }
                .map { it.toDomain() }
        }
    }

    override fun getRecommendations(topStyles: List<String>): Flow<List<Artwork>> {
        return userQueue().map { entities ->
            entities.filterNot { isArchival(it.title, it.artist, it.department, it.medium, it.imageUrl) }
                .filter { TasteEngine.normalizeStyle(it.styleMovement) in topStyles }.map { it.toDomain() }
        }
    }

    override suspend fun resetPreferences(userId: String): Unit = swipeMutex.withLock {
        val swipesRef = firestore.collection("users").document(userId).collection("swipes")
        val swipes = swipesRef.get().await()
        for (chunk in swipes.documents.chunked(400)) {
            val batch = firestore.batch()
            chunk.forEach { batch.delete(it.reference) }
            batch.commit().await()
        }
        firestore.collection("users").document(userId).update(
            "totalSwipes", 0,
            "styleScores", emptyMap<String, Int>(),
            "styleDislikes", emptyMap<String, Int>()
        ).await()
        artworkDao.clearAllSwipeRecords(userId)
    }

    override suspend fun syncLikedArtworksFromRemote(userId: String): Unit = swipeMutex.withLock {
        try {
            val swipes = firestore.collection("users").document(userId)
                .collection("swipes").get().await()

            val entitiesToRestore = mutableListOf<ArtworkEntity>()

            for (doc in swipes.documents) {
                val artworkId = doc.getString("artworkId") ?: continue
                val styleMovement = doc.getString("styleMovement") ?: ""
                val liked = doc.getBoolean("liked") ?: false
                val imageUrl = doc.getString("imageUrl") ?: ""
                val title = doc.getString("title") ?: "Untitled"
                val artist = doc.getString("artist") ?: "Unknown Artist"
                val dept = doc.getString("department") ?: ""
                val medium = doc.getString("medium") ?: ""

                if (liked && imageUrl.startsWith("http") && !isArchival(title, artist, dept, medium, imageUrl)) {
                    entitiesToRestore.add(ArtworkEntity(
                        id = artworkId,
                        source = doc.getString("source") ?: "unknown",
                        title = title,
                        artist = artist,
                        year = doc.getString("year"),
                        imageUrl = imageUrl,
                        styleMovement = styleMovement,
                        medium = medium,
                        description = doc.getString("description") ?: "",
                        department = dept,
                        sourceUrl = doc.getString("sourceUrl"),
                        randomOrder = Random.nextFloat()
                    ))
                }

                artworkDao.replaceSwipeRecord(
                    SwipeRecordEntity(
                        userId = userId, artworkId = artworkId,
                        liked = liked, timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                        styleMovement = styleMovement
                    )
                )
            }
            if (entitiesToRestore.isNotEmpty()) artworkDao.insertArtworks(entitiesToRestore)
        } catch (e: CancellationException) { throw e } catch (e: Exception) { Log.e("ArtSwipe", "Could not sync collection", e) }
    }

    override suspend fun reshuffleQueue() {
        artworkDao.reshuffleQueue()
    }

    private fun ArtworkEntity.toDomain() = Artwork(
        id = id, source = source, title = title, artist = artist, year = year,
        imageUrl = imageUrl, styleMovement = styleMovement, medium = medium,
        description = description, department = department, sourceUrl = sourceUrl
    )
}
