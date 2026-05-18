package com.artswipe.data.repository

import com.artswipe.domain.model.User
import com.artswipe.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override val currentUser: Flow<User?> = callbackFlow {
        var snapshotListener: ListenerRegistration? = null
        
        val authListener = FirebaseAuth.AuthStateListener { auth ->
            val firebaseUser = auth.currentUser
            snapshotListener?.remove()
            
            if (firebaseUser == null) {
                trySend(null)
            } else {
                snapshotListener = firestore.collection("users").document(firebaseUser.uid)
                    .addSnapshotListener { document, error ->
                        if (error != null) {
                            return@addSnapshotListener
                        }
                        
                        if (document != null && document.exists()) {
                            val user = User(
                                userId = firebaseUser.uid,
                                displayName = document.getString("displayName") ?: "",
                                email = firebaseUser.email ?: "",
                                joinDate = document.getString("joinDate") ?: "",
                                totalSwipes = document.getLong("totalSwipes")?.toInt() ?: 0,
                                styleScores = (document.get("styleScores") as? Map<String, Long>)
                                    ?.mapValues { it.value.toInt() } ?: emptyMap(),
                                shareCode = document.getString("shareCode") ?: ""
                            )
                            trySend(user)
                        } else {
                            trySend(User(userId = firebaseUser.uid, displayName = firebaseUser.displayName ?: "", email = firebaseUser.email ?: "", joinDate = ""))
                        }
                    }
            }
        }

        firebaseAuth.addAuthStateListener(authListener)
        awaitClose { 
            firebaseAuth.removeAuthStateListener(authListener)
            snapshotListener?.remove()
        }
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
            createInitialUserDocAsync(firebaseUser.uid, displayName)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signInWithGoogle(idToken: String): Result<Unit> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            firebaseAuth.signInWithCredential(credential).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun createInitialUserDocAsync(uid: String, displayName: String) {
        val shareCode = "ART-${UUID.randomUUID().toString().take(4).uppercase()}"
        val joinDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        
        val userDoc = mapOf(
            "userId" to uid,
            "displayName" to displayName,
            "joinDate" to joinDate,
            "totalSwipes" to 0,
            "styleScores" to emptyMap<String, Int>(),
            "shareCode" to shareCode
        )
        
        firestore.collection("users").document(uid).set(userDoc).await()
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
                val user = User(
                    userId = doc.getString("userId") ?: "",
                    displayName = doc.getString("displayName") ?: "",
                    email = "", 
                    joinDate = doc.getString("joinDate") ?: "",
                    totalSwipes = doc.getLong("totalSwipes")?.toInt() ?: 0,
                    styleScores = (doc.get("styleScores") as? Map<String, Long>)
                        ?.mapValues { it.value.toInt() } ?: emptyMap(),
                    shareCode = doc.getString("shareCode") ?: ""
                )
                Result.success(user)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
