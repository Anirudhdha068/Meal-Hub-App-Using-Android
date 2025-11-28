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
import com.example.hi.adapter.lunchAdapter
import com.example.hi.utils.Constants2
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class lunchactivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private var currentNotification: String? = null  // 🔥 Store message temporarily

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_lunchactivity)

        window.statusBarColor = getColor(R.color.orange)
        window.decorView.systemUiVisibility = 0

        // 🔙 Back button
        findViewById<ImageView>(R.id.backArrow).setOnClickListener { onBackPressed() }

        // 🔔 Get and store notification
        currentNotification = intent.getStringExtra("NOTIFY_HOME")

        // Show alert ONCE on page open
        if (!currentNotification.isNullOrEmpty()) {
            AlertDialog.Builder(this)
                .setTitle("Smart Canteen")
                .setMessage(currentNotification)
                .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                .show()

            intent.removeExtra("NOTIFY_HOME")  // Avoid showing repeatedly
        }

        // Notification bell
        val bellIcon = findViewById<ImageView>(R.id.bellIcon)
        bellIcon.setOnClickListener {
            val message = currentNotification ?: "No new notifications"
            AlertDialog.Builder(this)
                .setTitle("Notification")
                .setMessage(message)
                .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                .show()
        }

        // Bottom navigation
        setupBottomNavigation()

        // RecyclerView for lunch items
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewlunch)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = lunchAdapter(Constants2.getProducts())
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
