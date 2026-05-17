package com.artswipe.domain.model

data class SwipeRecord(
    val userId: String,
    val artworkId: String,
    val liked: Boolean,
    val timestamp: Long,
    val styleMovement: String
)
