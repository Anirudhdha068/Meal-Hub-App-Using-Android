package com.example.hi.repository

import com.example.hi.model.Notification
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

/**
 * Repository for Notification operations with Firestore
 */
class NotificationRepository {
    
    private val db = FirebaseFirestore.getInstance()
    
    /**
     * Save notification to Firestore
     * Path: notifications/{uid}/{notificationID}
     */
    suspend fun saveNotification(notification: Notification): Result<String> {
        return try {
            val docRef = db.collection("notifications")
                .document(notification.userId)
                .collection("userNotifications")
                .document(notification.notificationId)
            
            docRef.set(notification).await()
            Result.success(notification.notificationId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get all notifications for a user
     */
    suspend fun getUserNotifications(userId: String): Result<List<Notification>> {
        return try {
            val snapshot = db.collection("notifications")
                .document(userId)
                .collection("userNotifications")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val notifications = snapshot.documents.mapNotNull { it.toObject(Notification::class.java) }
            Result.success(notifications)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Mark notification as read
     */
    suspend fun markAsRead(userId: String, notificationId: String): Result<Unit> {
        return try {
            db.collection("notifications")
                .document(userId)
                .collection("userNotifications")
                .document(notificationId)
                .update("isRead", true)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}







