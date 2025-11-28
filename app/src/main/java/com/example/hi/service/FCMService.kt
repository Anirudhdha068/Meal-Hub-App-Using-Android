package com.example.hi.service

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.util.Log

/**
 * FCMService - Minimal version
 * Notifications removed as per COD-only requirement.
 */
class FCMService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Notification system removed. No action taken.
        Log.d("FCMService", "FCM message received but notifications are disabled.")
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // You can store the FCM token if needed for future use
        Log.d("FCMService", "New FCM token generated: $token")
    }
}
