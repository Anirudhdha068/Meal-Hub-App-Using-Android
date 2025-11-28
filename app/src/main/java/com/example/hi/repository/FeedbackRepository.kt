package com.example.hi.repository

import android.util.Log
import com.example.hi.model.Feedback
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FeedbackRepository {

    private val db = FirebaseFirestore.getInstance()
    private val TAG = "FeedbackRepository"

    /**
     * Save feedback to: feedback/{userId}/userFeedback/{feedbackId}
     */
    suspend fun saveFeedback(feedback: Feedback): Result<String> {
        return try {
            db.collection("feedback")
                .document(feedback.userId)
                .collection("userFeedback")
                .document(feedback.feedbackId)
                .set(feedback)
                .await()

            Log.d(TAG, "Feedback saved: ${feedback.feedbackId}")
            Result.success(feedback.feedbackId)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving feedback: ${e.message}", e)
            Result.failure(Exception("Failed to save feedback: ${e.localizedMessage}"))
        }
    }

    /**
     * Get all feedbacks (for average rating or admin view)
     */
    suspend fun getAllFeedbacks(): Result<List<Feedback>> {
        return try {
            val snapshot = db.collectionGroup("userFeedback").get().await()
            val feedbacks = snapshot.documents.mapNotNull { it.toObject(Feedback::class.java) }

            Log.d(TAG, "Fetched ${feedbacks.size} feedback entries")
            Result.success(feedbacks)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching feedbacks: ${e.message}", e)
            Result.failure(Exception("Failed to fetch feedbacks"))
        }
    }
}
