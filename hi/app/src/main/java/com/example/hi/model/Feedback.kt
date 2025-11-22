package com.example.hi.model

import com.google.firebase.firestore.PropertyName

/**
 * Feedback Model - represents feedback in Firestore
 */
data class Feedback(
    @PropertyName("feedbackId")
    var feedbackId: String = "",
    
    @PropertyName("userId")
    var userId: String = "",
    
    @PropertyName("userName")
    var userName: String = "",
    
    @PropertyName("rating")
    var rating: Float = 0f,
    
    @PropertyName("comments")
    var comments: String = "",
    
    @PropertyName("timestamp")
    var timestamp: Long = System.currentTimeMillis()
)







