package com.artswipe.domain.model

data class Artwork(
    val id: String,
    val source: String,
    val title: String,
    val artist: String,
    val year: String?,
    val imageUrl: String,
    val styleMovement: String,
    val medium: String?,
    val description: String,
    val department: String?,
    val sourceUrl: String? = null
)
