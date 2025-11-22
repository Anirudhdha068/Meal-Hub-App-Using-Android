package com.example.hi

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class PaymentSuccessActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_payment_success)

        window.statusBarColor = getColor(R.color.orange)

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val btnTrackOrder = findViewById<Button>(R.id.btnTrackOrder)
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Back button
        btnBack.setOnClickListener { finish() }

        // Track Order button → open OrderTrackingDetailActivity
        btnTrackOrder.setOnClickListener {
            val orderId = intent.getStringExtra("ORDER_ID") ?: ""
            val intent = Intent(this, OrderTrackingDetailActivity::class.java)
            intent.putExtra("ORDER_ID", orderId)
            startActivity(intent)
        }

        // Bottom navigation
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> { startActivity(Intent(this, activitybreakfast::class.java)); true }
                R.id.nav_menu -> { startActivity(Intent(this, activityhome1::class.java)); true }
                R.id.nav_orders -> { startActivity(Intent(this, OrderTrackingDetailActivity::class.java).apply {
                    putExtra("ORDER_ID", intent.getStringExtra("ORDER_ID") ?: "")
                }); true }
                R.id.nav_feedback -> { startActivity(Intent(this, FeedbackActivity::class.java)); true }
                R.id.nav_profile -> { startActivity(Intent(this, ProfileActivity::class.java)); true }
                R.id.nav_cart -> { startActivity(Intent(this, OrderActivity::class.java)); true }
                else -> false
            }
        }
    }
}
