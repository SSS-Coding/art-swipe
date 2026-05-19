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

    // Target departments for style diversity: 11 (European Paintings), 21 (Modern Art), 9 (Antiquities)
    private val metDepartments = listOf(11, 21, 9)
    private val styles = listOf("Impressionism", "Baroque", "Modernism", "Surrealism", "Realism", "Abstract", "Renaissance")

    override fun getArtworkQueue(): Flow<List<Artwork>> {
        return artworkDao.getUnswipedArtworks().map { entities ->
            // Stability is handled by the DB 'ORDER BY randomOrder ASC'
            entities.map { it.toDomain() }
        }
    }

    override suspend fun fetchMoreArtworks() {
        // Fetch from Met with specific department filters for style diversity
        try {
            val randomStyle = styles.random()
            val randomDept = metDepartments.random()
            
            val metSearch = metApi.search(
                query = randomStyle,
                departmentId = randomDept
            )
            
            val metEntities = mutableListOf<ArtworkEntity>()
            metSearch.objectIDs?.shuffled()?.take(15)?.forEach { id ->
                try {
                    val metObj = metApi.getObject(id)
                    if (metObj.primaryImage?.isNotEmpty() == true) {
                        metEntities.add(ArtworkEntity(
                            id = "met_${metObj.objectID}",
                            source = "met",
                            title = metObj.title ?: "Untitled",
                            artist = metObj.artistDisplayName ?: "Unknown Artist",
                            year = metObj.objectDate,
                            imageUrl = metObj.primaryImage,
                            styleMovement = randomStyle,
                            medium = metObj.medium,
                            description = "A magnificent piece from the Met Museum's ${metObj.department ?: "collection"}.",
                            department = metObj.department,
                            sourceUrl = metObj.objectURL,
                            randomOrder = Random.nextFloat()
                        ))
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            if (metEntities.isNotEmpty()) {
                artworkDao.insertArtworks(metEntities)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Fetch from AIC using random pages for discovery
        try {
            val aicResponse = aicApi.getArtworks(page = (1..100).random(), limit = 15)
            val iiifBaseUrl = aicResponse.config.iiif_url
            val aicEntities = mutableListOf<ArtworkEntity>()
            aicResponse.data.forEach { aicArt ->
                if (aicArt.image_id != null) {
                    val imageUrl = "$iiifBaseUrl/${aicArt.image_id}/full/843,/0/default.jpg"
                    val style = aicArt.style_title ?: styles.random() 
                    
                    aicEntities.add(ArtworkEntity(
                        id = "aic_${aicArt.id}",
                        source = "aic",
                        title = aicArt.title ?: "Untitled",
                        artist = aicArt.artist_display ?: "Unknown Artist",
                        year = aicArt.date_display,
                        imageUrl = imageUrl,
                        styleMovement = style,
                        medium = aicArt.medium_display,
                        description = "An intriguing work from the Art Institute of Chicago's ${aicArt.department_title ?: "collection"}.",
                        department = aicArt.department_title,
                        sourceUrl = aicArt.websiteUrl,
                        randomOrder = Random.nextFloat()
                    ))
                }
            }
            if (aicEntities.isNotEmpty()) {
                artworkDao.insertArtworks(aicEntities)
            }
        } catch (e: Exception) {
            e.printStackTrace()
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
        
        // If liked, save full artwork info so it can be synced to other devices
        artwork?.let {
            swipeData["title"] = it.title
            swipeData["artist"] = it.artist
            swipeData["imageUrl"] = it.imageUrl
            swipeData["source"] = it.source
            swipeData["year"] = it.year ?: ""
            swipeData["medium"] = it.medium ?: ""
            swipeData["description"] = it.description
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
        
        firestore.collection("users").document(userId)
            .collection("swipes").document(artworkId).delete()

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
            entities.map { it.toDomain() }
        }
    }

    override fun getRecommendations(topStyles: List<String>): Flow<List<Artwork>> {
        return artworkDao.getUnswipedArtworks().map { entities ->
            entities.filter { it.styleMovement in topStyles }.map { it.toDomain() }
        }
    }

    override suspend fun resetPreferences(userId: String) {
        // Clear local swipes AND artworks so user can "look at everything again" fresh
        artworkDao.clearAllSwipeRecords()
        artworkDao.clearAll() 
        
        // Reset Firestore scores and swipes
        firestore.collection("users").document(userId).update(
            "totalSwipes", 0,
            "styleScores", emptyMap<String, Int>(),
            "styleDislikes", emptyMap<String, Int>()
        ).await()
        
        val swipesRef = firestore.collection("users").document(userId).collection("swipes")
        val swipes = swipesRef.get().await()
        for (doc in swipes.documents) {
            doc.reference.delete()
        }
        
        // Trigger a fresh fetch after reset
        fetchMoreArtworks()
    }

    override suspend fun syncLikedArtworksFromRemote(userId: String) {
        try {
            val swipes = firestore.collection("users").document(userId)
                .collection("swipes")
                .get()
                .await()
            
            val entitiesToRestore = mutableListOf<ArtworkEntity>()
            val recordsToInsert = mutableListOf<SwipeRecordEntity>()

            for (doc in swipes.documents) {
                val artworkId = doc.getString("artworkId") ?: continue
                val styleMovement = doc.getString("styleMovement") ?: ""
                val liked = doc.getBoolean("liked") ?: false
                val timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                
                // Restore Artwork Entity if it was liked (so it shows in gallery)
                if (liked) {
                    entitiesToRestore.add(ArtworkEntity(
                        id = artworkId,
                        source = doc.getString("source") ?: "unknown",
                        title = doc.getString("title") ?: "Untitled",
                        artist = doc.getString("artist") ?: "Unknown Artist",
                        year = doc.getString("year"),
                        imageUrl = doc.getString("imageUrl") ?: "",
                        styleMovement = styleMovement,
                        medium = doc.getString("medium"),
                        description = doc.getString("description") ?: "",
                        department = doc.getString("department"),
                        sourceUrl = doc.getString("sourceUrl"),
                        randomOrder = Random.nextFloat()
                    ))
                }

                recordsToInsert.add(SwipeRecordEntity(
                    userId = userId,
                    artworkId = artworkId,
                    liked = liked,
                    timestamp = timestamp,
                    styleMovement = styleMovement
                ))
            }
            
            if (entitiesToRestore.isNotEmpty()) {
                artworkDao.insertArtworks(entitiesToRestore)
            }
            recordsToInsert.forEach { artworkDao.insertSwipeRecord(it) }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun reshuffleQueue() {
        artworkDao.reshuffleQueue()
    }

    private fun ArtworkEntity.toDomain() = Artwork(
        id = id,
        source = source,
        title = title,
        artist = artist,
        year = year,
        imageUrl = imageUrl,
        styleMovement = styleMovement,
        medium = medium,
        description = description,
        department = department,
        sourceUrl = sourceUrl
    )
}
