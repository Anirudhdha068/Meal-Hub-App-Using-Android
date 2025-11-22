package com.example.hi.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.hi.LoginActivity
import com.example.hi.OrderTrackingDetailActivity
import com.example.hi.R
import com.example.hi.utils.Constants
import com.example.hi.utils.NotificationHelper
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Firebase Cloud Messaging Service
 * Handles incoming push notifications from Firebase
 * 
 * Notifications triggered for:
 * - Payment success
 * - Payment failed
 * - Order confirmed
 * - Order packed
 * - Order out for delivery
 * - Order delivered
 */
class FCMService : FirebaseMessagingService() {

    override fun onCreate() {
        super.onCreate()
        // Create notification channel
        NotificationHelper.createNotificationChannel(this)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Handle notification payload
        remoteMessage.notification?.let { notification ->
            val title = notification.title ?: "Meal Hub"
            val body = notification.body ?: ""
            val type = remoteMessage.data["type"] ?: ""
            val orderId = remoteMessage.data["orderId"] ?: ""

            // Show notification
            showNotification(title, body, type, orderId)
        }

        // Handle data payload (only if notification is null)
        if (remoteMessage.notification == null && remoteMessage.data.isNotEmpty()) {
            val title = remoteMessage.data["title"] ?: "Meal Hub"
            val message = remoteMessage.data["message"] ?: ""
            val type = remoteMessage.data["type"] ?: ""
            val orderId = remoteMessage.data["orderId"] ?: ""

            if (message.isNotEmpty()) {
                showNotification(title, message, type, orderId)
            }
        }
    }

    /**
     * Show notification based on type
     */
    private fun showNotification(title: String, message: String, type: String, orderId: String) {
        val notificationId = System.currentTimeMillis().toInt()

        // Create intent based on notification type
        val intent = when (type) {
            Constants.NOTIFICATION_PAYMENT_SUCCESS,
            Constants.NOTIFICATION_PAYMENT_FAILED,
            Constants.NOTIFICATION_ORDER_CONFIRMED,
            Constants.NOTIFICATION_ORDER_PACKED,
            Constants.NOTIFICATION_OUT_FOR_DELIVERY,
            Constants.NOTIFICATION_DELIVERED -> {
                if (orderId.isNotEmpty()) {
                    Intent(this, OrderTrackingDetailActivity::class.java).apply {
                        putExtra("ORDER_ID", orderId)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                } else {
                    Intent(this, LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                }
            }
            else -> {
                Intent(this, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = NotificationHelper.CHANNEL_ID
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                NotificationHelper.CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    /**
     * Called when new FCM token is generated
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        
        // TODO: Send token to your server to save it
        // This token should be sent to Firestore under users/{uid}/fcmToken
        // Example: saveFCMTokenToFirestore(token)
    }

    companion object {
        private const val TAG = "FCMService"
        const val CHANNEL_ID = "meal_hub_notifications"
    }
}

