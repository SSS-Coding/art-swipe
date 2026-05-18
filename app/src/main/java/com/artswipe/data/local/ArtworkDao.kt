package com.artswipe.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.artswipe.data.local.model.ArtworkEntity
import com.artswipe.data.local.model.SwipeRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ArtworkDao {
    @Query("SELECT * FROM artworks")
    fun getAllArtworks(): Flow<List<ArtworkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArtworks(artworks: List<ArtworkEntity>)

    @Query("SELECT * FROM artworks WHERE id = :id")
    suspend fun getArtworkById(id: String): ArtworkEntity?

    @Query("DELETE FROM artworks WHERE id = :id")
    suspend fun deleteArtwork(id: String)

    @Query("DELETE FROM artworks")
    suspend fun clearAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSwipeRecord(swipeRecord: SwipeRecordEntity)

    @Query("SELECT * FROM swipe_records")
    fun getAllSwipeRecords(): Flow<List<SwipeRecordEntity>>

    @Query("SELECT * FROM swipe_records WHERE artworkId = :artworkId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestSwipeForArtwork(artworkId: String): SwipeRecordEntity?

    @Query("DELETE FROM swipe_records WHERE artworkId = :artworkId")
    suspend fun deleteSwipeRecordForArtwork(artworkId: String)

    @Query("DELETE FROM swipe_records")
    suspend fun clearAllSwipeRecords()

    @Query("SELECT * FROM artworks WHERE id NOT IN (SELECT artworkId FROM swipe_records)")
    fun getUnswipedArtworks(): Flow<List<ArtworkEntity>>

    @Query("SELECT * FROM artworks WHERE id IN (SELECT artworkId FROM swipe_records WHERE liked = 1)")
    fun getLikedArtworks(): Flow<List<ArtworkEntity>>
}
