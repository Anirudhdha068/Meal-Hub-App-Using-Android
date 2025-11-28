package com.example.hi

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

class PaymentSuccessActivity : AppCompatActivity() {

    private lateinit var tvOrderNumber: TextView
    private lateinit var tvPaymentDate: TextView
    private lateinit var tvTrackingNumber: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_payment_success)

        window.statusBarColor = getColor(R.color.orange)

        // Ask for notification permission (Android 13+)
        requestNotificationPermission()

        // Create notification channel
        createNotificationChannel()

        // UI components
        tvOrderNumber = findViewById(R.id.tvOrderNumber)
        tvPaymentDate = findViewById(R.id.tvPaymentDate)
        tvTrackingNumber = findViewById(R.id.tvTrackingNumber)

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val btnTrackOrder = findViewById<Button>(R.id.btnTrackOrder)

        // Get data from previous activity
        val orderId = intent.getStringExtra("ORDER_ID") ?: "N/A"
        val timestamp = intent.getLongExtra("ORDER_TIMESTAMP", System.currentTimeMillis())
        val isSuccess = intent.getBooleanExtra("PAYMENT_SUCCESS", true) // Allow dynamic message

        // Generate short order data
        val shortOrderId = "ORD" + orderId.takeLast(6).uppercase()
        val shortTrackingNo = "TRK" + orderId.takeLast(6).uppercase()

        // Format payment date
        val formattedDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date(timestamp))

        // Set UI values
        tvOrderNumber.text = "Order Number: $shortOrderId"
        tvPaymentDate.text = "Payment Date: $formattedDate"
        tvTrackingNumber.text = "Tracking Number: $shortTrackingNo"

        // Show notification
        showOrderNotification(isSuccess, shortOrderId)

        // Back button → return to home with message
        btnBack.setOnClickListener {
            val message = if (isSuccess)
                "Payment Successful! Your order $shortOrderId is placed."
            else
                "Payment Failed! Please try again."

            val intent = Intent(this, activityhome1::class.java)
            intent.putExtra("NOTIFY_HOME", message)
            startActivity(intent)
            finish()
        }

        // Track Order button
        btnTrackOrder.setOnClickListener {
            val intent = Intent(this, TrackOrderActivity::class.java)
            intent.putExtra("ORDER_ID", orderId)
            startActivity(intent)
            finish()
        }
    }

    // Ask permission for Android 13+
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1)
            }
        }
    }

    // Create notification channel (required for Android 8+)
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "order_channel",
                "Order Updates",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    // Show order confirmation/cancellation notification
    private fun showOrderNotification(isSuccess: Boolean, shortOrderId: String) {
        val intent = Intent(this, activityhome1::class.java)
        intent.putExtra("NOTIFY_HOME", if (isSuccess)
            "Payment Successful! Order No: $shortOrderId"
        else
            "Payment Failed! Try again."
        )

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (isSuccess) "Order Confirmed" else "Order Failed"
        val message = if (isSuccess)
            "Your order $shortOrderId has been placed!"
        else
            "Your order $shortOrderId failed."

        val notification = NotificationCompat.Builder(this, "order_channel")
            .setSmallIcon(R.drawable.notification)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(this).notify(1, notification)
    }
}
