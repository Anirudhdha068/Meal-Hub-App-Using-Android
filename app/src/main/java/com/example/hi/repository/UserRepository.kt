package com.example.hi.repository

import com.example.hi.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Repository for User operations with Firestore
 */
class UserRepository {

    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")

    /**
     * Save new user or update existing user
     */
    suspend fun saveUser(user: User): Result<Unit> {
        return try {
            usersCollection.document(user.uid)
                .set(user, com.google.firebase.firestore.SetOptions.merge())  // 🔄 Merge instead of replace
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetch user fresh from Firestore (not cached)
     */
    suspend fun getUser(uid: String): Result<User?> {
        return try {
            val snapshot = usersCollection.document(uid)
                .get(com.google.firebase.firestore.Source.SERVER) // 🟢 Force real-time data
                .await()
            Result.success(snapshot.toObject(User::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update name, phone or profile image
     */
    suspend fun updateUserProfile(
        uid: String,
        name: String? = null,
        phoneNumber: String? = null,
        profileImageUrl: String? = null
    ): Result<Unit> {
        return try {
            val updates = mutableMapOf<String, Any>()
            name?.let { updates["name"] = it }
            phoneNumber?.let { updates["phoneNumber"] = it }
            profileImageUrl?.let { updates["profileImageUrl"] = it }

            if (updates.isNotEmpty()) {
                usersCollection.document(uid).update(updates).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
