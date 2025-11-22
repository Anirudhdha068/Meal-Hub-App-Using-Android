package com.example.hi.model

import com.google.firebase.firestore.PropertyName

/**
 * Notification Model - represents notification in Firestore
 */
data class Notification(
    @PropertyName("notificationId")
    var notificationId: String = "",
    
    @PropertyName("userId")
    var userId: String = "",
    
    @PropertyName("title")
    var title: String = "",
    
    @PropertyName("message")
    var message: String = "",
    
    @PropertyName("type")
    var type: String = "", // payment_success, payment_failed, order_confirmed, order_packed, out_for_delivery, delivered
    
    @PropertyName("orderId")
    var orderId: String = "",
    
    @PropertyName("isRead")
    var isRead: Boolean = false,
    
    @PropertyName("timestamp")
    var timestamp: Long = System.currentTimeMillis()
)







