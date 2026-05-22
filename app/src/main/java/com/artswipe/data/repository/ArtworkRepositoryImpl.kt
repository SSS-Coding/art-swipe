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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.random.Random

class ArtworkRepositoryImpl @Inject constructor(
    private val metApi: MetApi,
    private val aicApi: AicApi,
    private val artworkDao: ArtworkDao,
    private val firestore: FirebaseFirestore
) : ArtworkRepository {

    private val styles = listOf(
        "Impressionism", "Baroque", "Modernism", "Surrealism", "Realism", 
        "Abstract", "Renaissance", "Landscape", "Portrait", "Mythology", 
        "Still Life", "Romanticism", "Contemporary", "Pop Art", "Expressionism"
    )

    override fun getArtworkQueue(): Flow<List<Artwork>> {
        return artworkDao.getUnswipedArtworks().map { entities ->
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

    override suspend fun fetchMoreArtworks() = coroutineScope {
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
                                    description = "A magnificent piece from the Met Museum.",
                                    department = obj.department,
                                    sourceUrl = obj.objectURL,
                                    randomOrder = Random.nextFloat()
                                )
                            } else null
                        } catch (e: Exception) { null }
                    }
                }
                val valid = metFetches.awaitAll().filterNotNull()
                if (valid.isNotEmpty()) {
                    artworkDao.insertArtworks(valid)
                    totalAdded += valid.size
                }
            } catch (e: Exception) { /* continue */ }

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
                            styleMovement = aicArt.style_title ?: style,
                            medium = aicArt.medium_display,
                            description = "A magnificent work from the Art Institute of Chicago.",
                            department = aicArt.department_title,
                            sourceUrl = aicArt.websiteUrl,
                            randomOrder = Random.nextFloat()
                        )
                    } else null
                }
                if (aicEntities.isNotEmpty()) {
                    artworkDao.insertArtworks(aicEntities)
                    totalAdded += aicEntities.size
                }
            } catch (e: Exception) { /* continue */ }
            
            if (totalAdded < 15) {
                // Short delay to avoid rate limiting during high-frequency retries
                delay(500)
            }
        }

        if (totalAdded == 0) {
            throw Exception("Masterpieces are currently elusive. Check your connection and try again.")
        }
    }

    override suspend fun getArtworkById(id: String): Artwork? {
        return artworkDao.getArtworkById(id)?.toDomain()
    }

    override suspend fun saveSwipeRecord(record: SwipeRecord) {
        artworkDao.insertSwipeRecord(
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
            swipeData["source"] = it.source
            swipeData["year"] = it.year ?: ""
            swipeData["medium"] = it.medium ?: ""
            swipeData["department"] = it.department ?: ""
            swipeData["sourceUrl"] = it.sourceUrl ?: ""
        }
        
        firestore.collection("users").document(record.userId)
            .collection("swipes").document(record.artworkId).set(swipeData)

        if (record.liked) {
            firestore.collection("users").document(record.userId).update(
                "totalSwipes", FieldValue.increment(1),
                "styleScores.${record.styleMovement}", FieldValue.increment(2)
            )
        } else {
            firestore.collection("users").document(record.userId).update(
                "totalSwipes", FieldValue.increment(1),
                "styleScores.${record.styleMovement}", FieldValue.increment(-1),
                "styleDislikes.${record.styleMovement}", FieldValue.increment(1)
            )
        }
    }

    override suspend fun removeSwipeRecord(userId: String, artworkId: String, styleMovement: String) {
        val lastSwipe = artworkDao.getLatestSwipeForArtwork(artworkId)
        val wasLiked = lastSwipe?.liked ?: return

        artworkDao.deleteSwipeRecordForArtwork(artworkId)
        firestore.collection("users").document(userId).collection("swipes").document(artworkId).delete()

        if (wasLiked) {
            firestore.collection("users").document(userId).update(
                "totalSwipes", FieldValue.increment(-1),
                "styleScores.$styleMovement", FieldValue.increment(-2)
            )
        } else {
            firestore.collection("users").document(userId).update(
                "totalSwipes", FieldValue.increment(-1),
                "styleScores.$styleMovement", FieldValue.increment(1),
                "styleDislikes.$styleMovement", FieldValue.increment(-1)
            )
        }
    }

    override fun getLikedArtworks(): Flow<List<Artwork>> {
        return artworkDao.getLikedArtworks().map { entities -> 
            entities.filterNot { isArchival(it.title, it.artist, it.department, it.medium, it.imageUrl) }
                .map { it.toDomain() } 
        }
    }

    override fun getRecommendations(topStyles: List<String>): Flow<List<Artwork>> {
        return artworkDao.getUnswipedArtworks().map { entities ->
            entities.filterNot { isArchival(it.title, it.artist, it.department, it.medium, it.imageUrl) }
                .filter { it.styleMovement in topStyles }.map { it.toDomain() }
        }
    }

    override suspend fun resetPreferences(userId: String) {
        artworkDao.clearAllSwipeRecords()
        artworkDao.clearAll() 
        
        firestore.collection("users").document(userId).update(
            "totalSwipes", 0,
            "styleScores", emptyMap<String, Int>(),
            "styleDislikes", emptyMap<String, Int>()
        ).await()
        
        val swipesRef = firestore.collection("users").document(userId).collection("swipes")
        val swipes = swipesRef.get().await()
        for (doc in swipes.documents) doc.reference.delete()
        
        fetchMoreArtworks()
    }

    override suspend fun syncLikedArtworksFromRemote(userId: String) {
        try {
            val swipes = firestore.collection("users").document(userId)
                .collection("swipes").get().await()
            
            val entitiesToRestore = mutableListOf<ArtworkEntity>()
            val recordsToInsert = mutableListOf<SwipeRecordEntity>()

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

                artworkDao.insertSwipeRecord(
                    SwipeRecordEntity(
                        userId = userId, artworkId = artworkId,
                        liked = liked, timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                        styleMovement = styleMovement
                    )
                )
            }
            if (entitiesToRestore.isNotEmpty()) artworkDao.insertArtworks(entitiesToRestore)
            recordsToInsert.forEach { artworkDao.insertSwipeRecord(it) }
        } catch (e: Exception) { e.printStackTrace() }
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
