package com.artswipe.data.repository

import com.artswipe.domain.model.User
import com.artswipe.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override val currentUser: Flow<User?> = callbackFlow {
        val authListener = FirebaseAuth.AuthStateListener { auth ->
            val firebaseUser = auth.currentUser
            if (firebaseUser == null) {
                trySend(null)
            } else {
                firestore.collection("users").document(firebaseUser.uid)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document != null && document.exists()) {
                            trySend(document.toUser(firebaseUser.email ?: ""))
                        } else {
                            trySend(User(userId = firebaseUser.uid, displayName = "", email = firebaseUser.email ?: "", joinDate = ""))
                        }
                    }
                    .addOnFailureListener {
                        trySend(null)
                    }
            }
        }
        firebaseAuth.addAuthStateListener(authListener)
        awaitClose { firebaseAuth.removeAuthStateListener(authListener) }
    }

    override suspend fun signInWithEmail(email: String, pass: String): Result<Unit> {
        return try {
            firebaseAuth.signInWithEmailAndPassword(email, pass).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signUpWithEmail(email: String, pass: String, displayName: String): Result<Unit> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, pass).await()
            val firebaseUser = result.user ?: throw Exception("User creation failed")
            
            val shareCode = "ART-${UUID.randomUUID().toString().take(4).uppercase()}"
            val joinDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            
            val userDoc = mapOf(
                "userId" to firebaseUser.uid,
                "displayName" to displayName,
                "joinDate" to joinDate,
                "totalSwipes" to 0,
                "styleScores" to emptyMap<String, Int>(),
                "shareCode" to shareCode
            )
            
            firestore.collection("users").document(firebaseUser.uid).set(userDoc).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signOut() {
        firebaseAuth.signOut()
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun findUserByShareCode(code: String): Result<User?> {
        return try {
            val query = firestore.collection("users")
                .whereEqualTo("shareCode", code.uppercase())
                .limit(1)
                .get()
                .await()
            
            if (query.isEmpty) {
                Result.success(null)
            } else {
                val doc = query.documents.first()
                Result.success(doc.toUser("")) // Email is private
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toUser(email: String): User {
        return User(
            userId = getString("userId") ?: "",
            displayName = getString("displayName") ?: "",
            email = email,
            joinDate = getString("joinDate") ?: "",
            totalSwipes = getLong("totalSwipes")?.toInt() ?: 0,
            styleScores = (get("styleScores") as? Map<String, Long>)
                ?.mapValues { it.value.toInt() } ?: emptyMap(),
            shareCode = getString("shareCode") ?: ""
        )
    }
}
