package com.example.hi.repository

import com.example.hi.model.Feedback
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Repository for Feedback operations with Firestore
 */
class FeedbackRepository {
    
    private val db = FirebaseFirestore.getInstance()
    
    /**
     * Save feedback to Firestore
     * Path: feedback/{uid}/{feedbackID}
     */
    suspend fun saveFeedback(feedback: Feedback): Result<String> {
        return try {
            val docRef = db.collection("feedback")
                .document(feedback.userId)
                .collection("userFeedback")
                .document(feedback.feedbackId)
            
            docRef.set(feedback).await()
            Result.success(feedback.feedbackId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get all feedbacks for calculating average rating
     */
    suspend fun getAllFeedbacks(): Result<List<Feedback>> {
        return try {
            val snapshot = db.collectionGroup("userFeedback").get().await()
            val feedbacks = snapshot.documents.mapNotNull { it.toObject(Feedback::class.java) }
            Result.success(feedbacks)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}







