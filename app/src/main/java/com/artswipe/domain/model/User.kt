package com.artswipe.domain.model

data class User(
    val userId: String,
    val displayName: String,
    val email: String,
    val joinDate: String,
    val totalSwipes: Int = 0,
    val styleScores: Map<String, Int> = emptyMap(),
    val shareCode: String = ""
)
