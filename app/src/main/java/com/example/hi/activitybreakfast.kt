package com.example.hi

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hi.adapter.breakfastAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class activitybreakfast : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private var currentNotification: String? = null // 🔥 Temporary storage

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_activitybreakfast)

        window.statusBarColor = getColor(R.color.orange)
        window.decorView.systemUiVisibility = 0

        // Back button
        findViewById<ImageView>(R.id.backArrow).setOnClickListener { onBackPressed() }

        // 🔹 Get notification from Intent (once)
        currentNotification = intent.getStringExtra("NOTIFY_HOME")

        // 🔥 Show alert if payment or feedback message received
        if (!currentNotification.isNullOrEmpty()) {
            AlertDialog.Builder(this)
                .setTitle("Smart Canteen")
                .setMessage(currentNotification)
                .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                .show()
        }

        // 🔔 Bell icon click
        val bellIcon = findViewById<ImageView>(R.id.bellIcon)
        bellIcon.setOnClickListener {
            val message = currentNotification ?: "No new notifications"
            AlertDialog.Builder(this)
                .setTitle("Notification")
                .setMessage(message)
                .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                .show()
        }

        // Setup bottom navigation
        setupBottomNavigation()

        // Setup breakfast products
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewBreakfast)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = breakfastAdapter(Constants1.getProducts())
    }

    private fun setupBottomNavigation() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.nav_home
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_menu -> { startActivity(Intent(this, activityhome1::class.java)); true }
                R.id.nav_feedback -> { startActivity(Intent(this, FeedbackActivity::class.java)); true }
                R.id.nav_profile -> { startActivity(Intent(this, ProfileActivity::class.java)); true }
                R.id.nav_cart -> { startActivity(Intent(this, OrderActivity::class.java)); true }
                else -> false
            }
        }
    }
}
