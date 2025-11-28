package com.example.hi

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class splashscreen : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splashscreen)
        window.statusBarColor = getColor(R.color.orange)
        window.decorView.systemUiVisibility = 0



        Handler(Looper.getMainLooper()).postDelayed({startActivity(Intent(
            this, LoginActivity::class.java))
            finish()
        }, 3000)
    }
}