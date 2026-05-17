package com.artswipe.domain.model

data class Artwork(
    val id: String,
    val source: String,         // "met" or "aic"
    val title: String,
    val artist: String,
    val year: String?,
    val imageUrl: String,
    val styleMovement: String,  // e.g. "Impressionism"
    val medium: String?,
    val description: String,    // shown post-swipe
    val department: String?
)
