package com.artswipe.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "swipe_records")
data class SwipeRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val artworkId: String,
    val liked: Boolean,
    val timestamp: Long,
    val styleMovement: String
)
