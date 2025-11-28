package com.example.hi

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class OrderFailedActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_payment_failed)

        window.statusBarColor = getColor(R.color.orange)

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val btnRetryOrder = findViewById<Button>(R.id.btnRetryOrder)
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Go back
        btnBack.setOnClickListener { finish() }

        // Retry → takes user to Order Screen
        btnRetryOrder.setOnClickListener {
            startActivity(Intent(this, OrderActivity::class.java))
            finish()
        }

        // Bottom Navigation

        }
    }

