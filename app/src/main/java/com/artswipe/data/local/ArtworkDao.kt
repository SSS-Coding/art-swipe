package com.artswipe.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.artswipe.data.local.model.ArtworkEntity
import com.artswipe.data.local.model.SwipeRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ArtworkDao {
    @Query("SELECT * FROM artworks")
    fun getAllArtworks(): Flow<List<ArtworkEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertArtworks(artworks: List<ArtworkEntity>): List<Long>

    @Query("SELECT * FROM artworks WHERE id = :id")
    suspend fun getArtworkById(id: String): ArtworkEntity?

    @Query("DELETE FROM artworks WHERE id = :id")
    suspend fun deleteArtwork(id: String)

    @Query("DELETE FROM artworks")
    suspend fun clearAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSwipeRecord(swipeRecord: SwipeRecordEntity)

    @Transaction
    suspend fun replaceSwipeRecord(record: SwipeRecordEntity) {
        deleteSwipeRecordForArtwork(record.userId, record.artworkId)
        insertSwipeRecord(record.copy(id = 0))
    }

    @Query("SELECT * FROM swipe_records WHERE userId = :userId")
    fun getAllSwipeRecords(userId: String): Flow<List<SwipeRecordEntity>>

    @Query("SELECT * FROM swipe_records WHERE userId = :userId AND artworkId = :artworkId ORDER BY timestamp DESC, id DESC LIMIT 1")
    suspend fun getLatestSwipeForArtwork(userId: String, artworkId: String): SwipeRecordEntity?

    @Query("DELETE FROM swipe_records WHERE userId = :userId AND artworkId = :artworkId")
    suspend fun deleteSwipeRecordForArtwork(userId: String, artworkId: String)

    @Query("DELETE FROM swipe_records WHERE userId = :userId")
    suspend fun clearAllSwipeRecords(userId: String)

    @Query("SELECT * FROM artworks WHERE id NOT IN (SELECT artworkId FROM swipe_records WHERE userId = :userId) ORDER BY randomOrder ASC")
    fun getUnswipedArtworks(userId: String): Flow<List<ArtworkEntity>>

    @Query("SELECT a.* FROM artworks a INNER JOIN swipe_records s ON a.id = s.artworkId WHERE s.userId = :userId AND s.liked = 1 AND s.id = (SELECT s2.id FROM swipe_records s2 WHERE s2.userId = :userId AND s2.artworkId = a.id ORDER BY s2.timestamp DESC, s2.id DESC LIMIT 1) ORDER BY s.timestamp DESC")
    fun getLikedArtworks(userId: String): Flow<List<ArtworkEntity>>

    @Query("UPDATE artworks SET randomOrder = ABS(RANDOM()) % 1000000 / 1000000.0")
    suspend fun reshuffleQueue()
}
