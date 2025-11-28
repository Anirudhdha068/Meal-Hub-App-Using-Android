package com.example.hi

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.hi.model.Feedback
import com.example.hi.repository.FeedbackRepository
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

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

        // Init Views
        ratingBar = findViewById(R.id.ratingBar)
        etComments = findViewById(R.id.etComments)
        btnSubmit = findViewById(R.id.btnSubmit)
        progressBar = findViewById(R.id.progressBar)
        val btnBack = findViewById<ImageView>(R.id.btnBack)

        // Setup bottom navigation
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.nav_feedback
        setupBottomNavigation(bottomNavigationView)

        btnBack.setOnClickListener { finish() }
        btnSubmit.setOnClickListener { submitFeedback() }
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

        if (rating == 0f) {
            Toast.makeText(this, "Please provide a rating", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar.visibility = View.VISIBLE
        btnSubmit.isEnabled = false

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val feedback = Feedback(
                    feedbackId = UUID.randomUUID().toString(),
                    userId = currentUser.uid,
                    userName = currentUser.displayName ?: "User",
                    rating = rating,
                    comments = comments,
                    timestamp = System.currentTimeMillis()
                )

                feedbackRepository.saveFeedback(feedback)

                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    btnSubmit.isEnabled = true
                    showThankYouDialog()
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    btnSubmit.isEnabled = true
                    Toast.makeText(this@FeedbackActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * Thank you dialog with notification message to home
     */
    private fun showThankYouDialog() {
        AlertDialog.Builder(this)
            .setTitle("Thank You!")
            .setMessage("Your feedback has been submitted successfully.")
            .setPositiveButton("OK") { _, _ ->
                // Redirect to breakfast/home page with notification message
                val intent = Intent(this, activitybreakfast::class.java)
                intent.putExtra("NOTIFY_HOME", "Thank you for your feedback!")
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                finish()
            }
            .setCancelable(false)
            .show()
    }

    /**
     * Bottom Navigation Setup
     */
    private fun setupBottomNavigation(bottomNavigationView: BottomNavigationView) {
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {

                R.id.nav_home -> {
                    startActivity(Intent(this, activitybreakfast::class.java))
                    true
                }

                R.id.nav_menu -> {
                    startActivity(Intent(this, activityhome1::class.java))
                    true
                }

                R.id.nav_feedback -> true // current page

                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }

                R.id.nav_cart -> {
                    startActivity(Intent(this, OrderActivity::class.java))
                    true
                }

                else -> false
            }
        }
    }
}
