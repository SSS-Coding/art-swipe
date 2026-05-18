package com.artswipe.domain.repository

import com.artswipe.domain.model.Artwork
import com.artswipe.domain.model.SwipeRecord
import kotlinx.coroutines.flow.Flow

interface ArtworkRepository {
    fun getArtworkQueue(): Flow<List<Artwork>>
    suspend fun fetchMoreArtworks()
    suspend fun getArtworkById(id: String): Artwork?
    suspend fun saveSwipeRecord(record: SwipeRecord)
    suspend fun removeSwipeRecord(userId: String, artworkId: String, styleMovement: String)
    fun getLikedArtworks(): Flow<List<Artwork>>
    fun getRecommendations(topStyles: List<String>): Flow<List<Artwork>>
    suspend fun resetPreferences(userId: String)
    suspend fun syncLikedArtworksFromRemote(userId: String)
}
