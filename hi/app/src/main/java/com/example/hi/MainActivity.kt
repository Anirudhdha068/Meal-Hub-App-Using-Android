//package com.example.hi
//
//import android.content.Intent
//import android.os.Bundle
//import androidx.appcompat.app.AppCompatActivity
//import com.google.android.material.bottomnavigation.BottomNavigationView
//
//class MainActivity : AppCompatActivity() {
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//
//        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
//
//        // Current tab highlight
//        bottomNavigationView.selectedItemId = R.id.nav_menu
//
//        // Navigation listener
//        bottomNavigationView.setOnItemSelectedListener { item ->
//            when (item.itemId) {
//                R.id.nav_home -> {
//                    val intent = Intent(this, activityhome1::class.java)
//                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
//                    startActivity(intent)
//                    true
//                }
//
//
//                R.id.nav_orders -> {
//                    val intent = Intent(this, OrderActivity::class.java)
//                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
//                    startActivity(intent)
//                    true
//                }
//                R.id.nav_menu -> {
//                    val intent = Intent(this, activityhome1::class.java)
//                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
//                    startActivity(intent)
//                    true
//                }
//                R.id.nav_feedback -> {
//                    val intent = Intent(this, FeedbackActivity::class.java)
//                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
//                    startActivity(intent)
//                    true
//                }
//                R.id.nav_profile -> {
//                    val intent = Intent(this, ProfileActivity::class.java)
//                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
//                    startActivity(intent)
//                    true
//                }
//                else -> false
//            }
//        }
//    }
//}



package com.example.hi

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        window.statusBarColor = getColor(R.color.orange)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Current tab highlight (MainActivity = Menu tab)
        bottomNavigationView.selectedItemId = R.id.nav_menu

        // Navigation listener
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    if (bottomNavigationView.selectedItemId != R.id.nav_home) {
                        startActivity(
                            Intent(this, activityhome1::class.java)
                                .apply {
                                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                                }
                        )
                    }
                    true
                }
                R.id.nav_orders -> {
                    if (bottomNavigationView.selectedItemId != R.id.nav_orders) {
                        startActivity(
                            Intent(this, OrderActivity::class.java)
                                .apply {
                                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                                }
                        )
                    }
                    true
                }
                R.id.nav_menu -> true // Already in Menu
                R.id.nav_feedback -> {
                    if (bottomNavigationView.selectedItemId != R.id.nav_feedback) {
                        startActivity(
                            Intent(this, FeedbackActivity::class.java)
                                .apply {
                                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                                }
                        )
                    }
                    true
                }
                R.id.nav_profile -> {
                    if (bottomNavigationView.selectedItemId != R.id.nav_profile) {
                        startActivity(
                            Intent(this, ProfileActivity::class.java)
                                .apply {
                                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                                }
                        )
                    }
                    true
                }
                else -> false
            }
        }
    }
}

