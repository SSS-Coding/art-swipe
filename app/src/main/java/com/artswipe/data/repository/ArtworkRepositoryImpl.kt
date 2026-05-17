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
import javax.inject.Inject

class ArtworkRepositoryImpl @Inject constructor(
    private val metApi: MetApi,
    private val aicApi: AicApi,
    private val artworkDao: ArtworkDao,
    private val firestore: FirebaseFirestore
) : ArtworkRepository {

    private val styles = listOf("Impressionism", "Baroque", "Modernism", "Surrealism", "Realism", "Abstract", "Renaissance")

    override fun getArtworkQueue(): Flow<List<Artwork>> {
        return artworkDao.getUnswipedArtworks().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun fetchMoreArtworks() {
        // Fetch from Met with more diversity
        try {
            val randomStyle = styles.random()
            val metSearch = metApi.search(randomStyle)
            metSearch.objectIDs?.shuffled()?.take(10)?.forEach { id ->
                val metObj = metApi.getObject(id)
                if (metObj.primaryImage?.isNotEmpty() == true) {
                    val entity = ArtworkEntity(
                        id = "met_${metObj.objectID}",
                        source = "met",
                        title = metObj.title ?: "Untitled",
                        artist = metObj.artistDisplayName ?: "Unknown Artist",
                        year = metObj.objectDate,
                        imageUrl = metObj.primaryImage,
                        styleMovement = randomStyle,
                        medium = metObj.medium,
                        description = "A magnificent piece from the Met Museum's ${metObj.department ?: "collection"}.",
                        department = metObj.department
                    )
                    artworkDao.insertArtworks(listOf(entity))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Fetch from AIC
        try {
            val aicResponse = aicApi.getArtworks(page = (1..50).random(), limit = 15)
            val iiifBaseUrl = aicResponse.config.iiif_url
            aicResponse.data.forEach { aicArt ->
                if (aicArt.image_id != null) {
                    val imageUrl = "$iiifBaseUrl/${aicArt.image_id}/full/843,/0/default.jpg"
                    val entity = ArtworkEntity(
                        id = "aic_${aicArt.id}",
                        source = "aic",
                        title = aicArt.title ?: "Untitled",
                        artist = aicArt.artist_display ?: "Unknown Artist",
                        year = aicArt.date_display,
                        imageUrl = imageUrl,
                        styleMovement = aicArt.style_title ?: "Modernism",
                        medium = aicArt.medium_display,
                        description = "An intriguing work from the Art Institute of Chicago's ${aicArt.department_title ?: "collection"}.",
                        department = aicArt.department_title
                    )
                    artworkDao.insertArtworks(listOf(entity))
                }
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

        val swipeData = mapOf(
            "artworkId" to record.artworkId,
            "liked" to record.liked,
            "timestamp" to record.timestamp,
            "styleMovement" to record.styleMovement
        )
        
        firestore.collection("users").document(record.userId)
            .collection("swipes").add(swipeData)

        val scoreIncrement = if (record.liked) 2L else -1L
        firestore.collection("users").document(record.userId).update(
            "totalSwipes", FieldValue.increment(1),
            "styleScores.${record.styleMovement}", FieldValue.increment(scoreIncrement)
        )
    }

    override suspend fun removeSwipeRecord(userId: String, artworkId: String, styleMovement: String) {
        // Find if it was liked or disliked
        val lastSwipe = artworkDao.getLatestSwipeForArtwork(artworkId)
        val wasLiked = lastSwipe?.liked ?: return

        // 1. Remove from local DB
        artworkDao.deleteSwipeRecordForArtwork(artworkId)

        // 2. Update Firestore scores
        val scoreDecrement = if (wasLiked) -2L else 1L
        firestore.collection("users").document(userId).update(
            "totalSwipes", FieldValue.increment(-1),
            "styleScores.$styleMovement", FieldValue.increment(scoreDecrement)
        )
        
        // Note: For simplicity, we aren't deleting the specific document in the 'swipes' collection 
        // because we don't have its ID easily here, but the scores are adjusted.
    }

    override fun getLikedArtworks(): Flow<List<Artwork>> {
        return artworkDao.getLikedArtworks().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getRecommendations(topStyles: List<String>): Flow<List<Artwork>> {
        // Simple recommendation: unswiped artworks that match top styles
        return artworkDao.getUnswipedArtworks().map { entities ->
            entities.filter { it.styleMovement in topStyles }.map { it.toDomain() }
        }
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
        department = department
    )
}
