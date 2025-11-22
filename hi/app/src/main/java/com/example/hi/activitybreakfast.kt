package com.example.hi

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hi.adapter.breakfastAdapter
import com.example.hi.model.breakfast
import com.example.hi.repository.NotificationRepository
import com.example.hi.repository.OrderRepository
import com.example.hi.utils.Constants
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class activitybreakfast : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val notificationRepository = NotificationRepository()
    private val orderRepository = OrderRepository()
    private lateinit var notificationIcon: ImageView
    private lateinit var notificationBadge: TextView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_activitybreakfast)

        window.statusBarColor = getColor(R.color.orange)
        window.decorView.systemUiVisibility = 0

        // Back button
        findViewById<ImageView>(R.id.backArrow).setOnClickListener { onBackPressed() }

        // Notification icon & badge
        notificationIcon = findViewById(R.id.bellIcon)
        notificationBadge = findViewById(R.id.notificationBadge)
        notificationIcon.setOnClickListener { showLatestNotification() }

        // Bottom navigation
        setupBottomNavigation()

        // Setup breakfast products
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewBreakfast)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = breakfastAdapter(Constants1.getProducts())

        // Check notifications immediately
        checkNotifications()
    }

    private fun setupBottomNavigation() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.nav_menu
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_menu -> { startActivity(Intent(this, activityhome1::class.java)); true }
                R.id.nav_orders -> { startActivity(Intent(this, TrackOrderActivity::class.java)); true }
                R.id.nav_feedback -> { startActivity(Intent(this, FeedbackActivity::class.java)); true }
                R.id.nav_profile -> { startActivity(Intent(this, ProfileActivity::class.java)); true }
                R.id.nav_cart -> { startActivity(Intent(this, OrderActivity::class.java)); true }
                else -> false
            }
        }
    }

    /**
     * Checks for new payment notifications and updates badge
     */
    private fun checkNotifications() {
        val user = auth.currentUser ?: return
        lifecycleScope.launch {
            try {
                val result = notificationRepository.getUserNotifications(user.uid)
                if (result.isSuccess) {
                    val notifications = result.getOrNull() ?: emptyList()
                    val unseenPayments = notifications.filter {
                        it.type in listOf(Constants.NOTIFICATION_PAYMENT_SUCCESS, Constants.NOTIFICATION_PAYMENT_FAILED)
                                && !it.isRead
                    }

                    if (unseenPayments.isNotEmpty()) {
                        notificationBadge.visibility = TextView.VISIBLE
                        notificationBadge.text = unseenPayments.size.toString()
                    } else {
                        notificationBadge.visibility = TextView.GONE
                    }
                }
            } catch (_: Exception) { notificationBadge.visibility = TextView.GONE }
        }
    }

    /**
     * Show the latest payment notification as a dialog
     */
    private fun showLatestNotification() {
        val user = auth.currentUser ?: return
        lifecycleScope.launch {
            try {
                val result = notificationRepository.getUserNotifications(user.uid)
                val notifications = result.getOrNull() ?: emptyList()
                val latest = notifications.firstOrNull {
                    it.type in listOf(Constants.NOTIFICATION_PAYMENT_SUCCESS, Constants.NOTIFICATION_PAYMENT_FAILED)
                }

                if (latest != null) {
                    val msg = if (latest.type == Constants.NOTIFICATION_PAYMENT_SUCCESS) {
                        "Payment Successful! Order #${latest.orderId}"
                    } else {
                        "Payment Failed! Order #${latest.orderId}"
                    }

                    AlertDialog.Builder(this@activitybreakfast)
                        .setTitle("Payment Update")
                        .setMessage(msg)
                        .setPositiveButton("View Order") { _, _ ->
                            val intent = Intent(this@activitybreakfast, OrderTrackingDetailActivity::class.java)
                            intent.putExtra("ORDER_ID", latest.orderId)
                            startActivity(intent)
                        }
                        .setNegativeButton("OK", null)
                        .show()

                    // Mark as read
                    if (!latest.isRead) {
                        notificationRepository.markAsRead(user.uid, latest.notificationId)
                        notificationBadge.visibility = TextView.GONE
                    }
                } else {
                    AlertDialog.Builder(this@activitybreakfast)
                        .setTitle("Notifications")
                        .setMessage("No new notifications")
                        .setPositiveButton("OK", null)
                        .show()
                }
            } catch (_: Exception) {
                AlertDialog.Builder(this@activitybreakfast)
                    .setTitle("Notifications")
                    .setMessage("Unable to fetch notifications")
                    .setPositiveButton("OK", null)
                    .show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkNotifications()
    }
}
