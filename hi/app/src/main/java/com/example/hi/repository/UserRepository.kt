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
     * Save or update user in Firestore
     */
    suspend fun saveUser(user: User): Result<Unit> {
        return try {
            usersCollection.document(user.uid).set(user).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get user by UID
     */
    suspend fun getUser(uid: String): Result<User?> {
        return try {
            val snapshot = usersCollection.document(uid).get().await()
            val user = snapshot.toObject(User::class.java)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update user profile
     */
    suspend fun updateUserProfile(
        uid: String,
        name: String? = null,
        phoneNumber: String? = null,
        profileImageUrl: String? = null
    ): Result<Unit> {
        return try {
            val updates = hashMapOf<String, Any>()
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







