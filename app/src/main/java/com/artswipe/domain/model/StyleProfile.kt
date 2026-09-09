package com.artswipe.domain.model

data class StyleProfile(
    val topStyle: String,
    val personalityLabel: String,
    val personalityDescription: String,
    val styleBreakdown: List<StylePercentage>,
    val leastLikedStyles: List<String>,
    val showPersonalityCard: Boolean,
    val totalSwipes: Int = 0
)

data class StylePercentage(
    val style: String,
    val percentage: Float,
    val likeCount: Int,
    val thumbnailUrl: String? = null
)
