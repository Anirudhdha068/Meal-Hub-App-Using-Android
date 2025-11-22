package com.example.hi

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class activityhome1 : AppCompatActivity() {
    
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_activityhome1)
        window.statusBarColor = getColor(R.color.orange)
        window.decorView.systemUiVisibility = 0

        val btnBreakfast: Button = findViewById(R.id.btnBreakfast)
        val btnLunch: Button = findViewById(R.id.btnLunch)
        val btnDinner: Button = findViewById(R.id.btnDinner)

        // Handle Breakfast button click
        btnBreakfast.setOnClickListener {
            val intent = Intent(this, activitybreakfast::class.java)
            startActivity(intent)
            finish()
        }

        // Handle Lunch button click
        btnLunch.setOnClickListener {
            val intent = Intent(this, lunchactivity::class.java)
            startActivity(intent)
        }

        // Handle Dinner button click
        btnDinner.setOnClickListener {
            val intent = Intent(this, dinneractivity::class.java)
            startActivity(intent)
        }
    }
}







//package com.example.hi
//
//import android.annotation.SuppressLint
//import android.content.Intent
//import android.os.Bundle
//import androidx.activity.enableEdgeToEdge
//import androidx.appcompat.app.AppCompatActivity
//import android.widget.Button
//import com.google.android.material.bottomnavigation.BottomNavigationView
//
//class activityhome1 : AppCompatActivity() {
//
//    @SuppressLint("MissingInflatedId")
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContentView(R.layout.activity_activityhome1)
//        window.statusBarColor = getColor(R.color.orange)
//        window.decorView.systemUiVisibility = 0
//
//        val btnBreakfast: Button = findViewById(R.id.btnBreakfast)
//        val btnLunch: Button = findViewById(R.id.btnLunch)
//        val btnDinner: Button = findViewById(R.id.btnDinner)
//
//        // Handle Breakfast button click
//        btnBreakfast.setOnClickListener {
//            startActivity(Intent(this, activitybreakfast::class.java))
//        }
//
//        // Handle Lunch button click
//        btnLunch.setOnClickListener {
//            startActivity(Intent(this, lunchactivity::class.java))
//        }
//
//        // Handle Dinner button click
//        btnDinner.setOnClickListener {
//            startActivity(Intent(this, dinneractivity::class.java))
//        }
//
//        // Bottom Navigation
//        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
//
//        // Current tab highlight
//        bottomNavigationView.selectedItemId = R.id.nav_home
//
//        bottomNavigationView.setOnItemSelectedListener { item ->
//            when(item.itemId){
//                R.id.nav_home -> true // Already in Home
//                R.id.nav_orders -> {
//                    startActivity(Intent(this, OrderActivity::class.java).apply {
//                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
//                    })
//                    true
//                }
//                R.id.nav_menu -> {
//                    startActivity(Intent(this, MainActivity::class.java).apply {
//                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
//                    })
//                    true
//                }
//                R.id.nav_feedback -> {
//                    startActivity(Intent(this, FeedbackActivity::class.java).apply {
//                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
//                    })
//                    true
//                }
//                R.id.nav_profile -> {
//                    startActivity(Intent(this, ProfileActivity::class.java).apply {
//                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
//                    })
//                    true
//                }
//                else -> false
//            }
//        }
//    }
//}
