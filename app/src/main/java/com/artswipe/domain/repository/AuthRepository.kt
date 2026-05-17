package com.artswipe.domain.repository

import com.artswipe.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<User?>
    suspend fun signInWithEmail(email: String, pass: String): Result<Unit>
    suspend fun signUpWithEmail(email: String, pass: String, displayName: String): Result<Unit>
    suspend fun signOut()
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
    suspend fun findUserByShareCode(code: String): Result<User?>
}
