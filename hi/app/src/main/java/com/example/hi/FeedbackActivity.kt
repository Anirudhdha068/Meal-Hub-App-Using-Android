package com.example.hi

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.hi.model.Feedback
import com.example.hi.repository.FeedbackRepository
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.util.*

/**
 * FeedbackActivity - Allows users to submit feedback
 * Features:
 * - RatingBar for rating (1-5 stars)
 * - Comments box
 * - Submit button
 * - Saves to Firestore: feedback/{uid}/{feedbackID}
 * - Shows thank you message after submission
 */
class FeedbackActivity : AppCompatActivity() {

    private lateinit var ratingBar: RatingBar
    private lateinit var etComments: EditText
    private lateinit var btnSubmit: Button
    private lateinit var progressBar: ProgressBar
    
    private val auth = FirebaseAuth.getInstance()
    private val feedbackRepository = FeedbackRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feedback)
        
        window.statusBarColor = getColor(R.color.orange)

        // Initialize views
        ratingBar = findViewById(R.id.ratingBar)
        etComments = findViewById(R.id.etComments)
        btnSubmit = findViewById(R.id.btnSubmit)
        val btnBack = findViewById<ImageView>(R.id.btnBack)
        progressBar = findViewById(R.id.progressBar)

        // Setup bottom navigation
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.nav_feedback
        setupBottomNavigation(bottomNavigationView)

        btnBack?.setOnClickListener { finish() }

        btnSubmit.setOnClickListener {
            submitFeedback()
        }
    }

    /**
     * Submit feedback to Firestore
     */
    private fun submitFeedback() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val rating = ratingBar.rating
        val comments = etComments.text.toString().trim()

        // Validation
        if (rating == 0f) {
            Toast.makeText(this, "Please provide a rating", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar.visibility = ProgressBar.VISIBLE
        btnSubmit.isEnabled = false

        lifecycleScope.launch {
            try {
                val feedback = Feedback(
                    feedbackId = UUID.randomUUID().toString(),
                    userId = currentUser.uid,
                    userName = currentUser.displayName ?: "User",
                    rating = rating,
                    comments = comments,
                    timestamp = System.currentTimeMillis()
                )

                val result = feedbackRepository.saveFeedback(feedback)

                if (result.isSuccess) {
                    // Show thank you message
                    showThankYouDialog()
                } else {
                    Toast.makeText(
                        this@FeedbackActivity,
                        "Failed to submit feedback: ${result.exceptionOrNull()?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    progressBar.visibility = ProgressBar.GONE
                    btnSubmit.isEnabled = true
                }
            } catch (e: Exception) {
                Toast.makeText(this@FeedbackActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                progressBar.visibility = ProgressBar.GONE
                btnSubmit.isEnabled = true
            }
        }
    }

    /**
     * Show thank you dialog after successful submission
     */
    private fun showThankYouDialog() {
        AlertDialog.Builder(this)
            .setTitle("Thank You!")
            .setMessage("Your feedback has been submitted successfully. We appreciate your input!")
            .setPositiveButton("OK") { _, _ ->
                // Clear form
                ratingBar.rating = 0f
                etComments.text.clear()
                progressBar.visibility = ProgressBar.GONE
                btnSubmit.isEnabled = true
                finish()
            }
            .setCancelable(false)
            .show()
    }

    /**
     * Setup bottom navigation
     */
    private fun setupBottomNavigation(bottomNavigationView: BottomNavigationView) {
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, activitybreakfast::class.java))
                    finish()
                    true
                }
                R.id.nav_menu -> {
                    startActivity(Intent(this, activityhome1::class.java))
                    finish()
                    true
                }
                R.id.nav_orders -> {
                    startActivity(Intent(this, TrackOrderActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_feedback -> {
                    // Already on feedback page
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_cart -> {
                    startActivity(Intent(this, OrderActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }
}
