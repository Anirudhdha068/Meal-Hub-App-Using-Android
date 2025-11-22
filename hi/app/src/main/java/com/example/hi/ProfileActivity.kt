package com.example.hi

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.hi.model.User
import com.example.hi.repository.UserRepository
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileActivity : AppCompatActivity() {

    private lateinit var ivProfile: ShapeableImageView
    private lateinit var tvName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvMobile: TextView
    private lateinit var btnUpdateProfile: LinearLayout
    private lateinit var btnViewOrders: LinearLayout
    private lateinit var logoutLayout: LinearLayout
    private lateinit var progressBar: ProgressBar

    private var currentUser: User? = null

    private val auth = FirebaseAuth.getInstance()
    private val userRepository = UserRepository()
    private val storage = FirebaseStorage.getInstance()

    // Image picker launcher: immediately upload picked image, don't hold Uri long term
    private val pickImageLauncher =
        registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                uploadProfileImage(it) // Upload immediately on selection
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        window.statusBarColor = getColor(R.color.orange)

        // Init views
        ivProfile = findViewById(R.id.ivProfile)
        tvName = findViewById(R.id.tvName)
        tvEmail = findViewById(R.id.tvEmail)
        tvMobile = findViewById(R.id.tvMobile)
        btnUpdateProfile = findViewById(R.id.btnUpdateProfile)
        btnViewOrders = findViewById(R.id.btnViewOrders)
        logoutLayout = findViewById(R.id.llLogout)
        progressBar = findViewById(R.id.progressBar)

        val currentUserAuth = auth.currentUser
        if (currentUserAuth == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Bottom navigation
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.nav_profile
        setupBottomNavigation(bottomNavigationView)

        // Load cached or fetch user data (async)
        loadUserData(currentUserAuth.uid)

        // Click listeners
        ivProfile.setOnClickListener { showProfileOptions() }
        findViewById<ImageView>(R.id.btnBack)?.setOnClickListener { finish() }
        btnUpdateProfile.setOnClickListener { showUpdateProfileDialog() }
        btnViewOrders.setOnClickListener { startActivity(Intent(this, TrackOrderActivity::class.java)) }
        findViewById<LinearLayout>(R.id.btnFeedback)?.setOnClickListener { startActivity(Intent(this, FeedbackActivity::class.java)) }
        logoutLayout.setOnClickListener { showLogoutDialog() }
    }

    private fun loadUserData(uid: String) {
        // If user already cached, just update UI immediately
        currentUser?.let { user ->
            updateUI(user)
            return
        }

        progressBar.visibility = ProgressBar.VISIBLE

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Fetch user from Firestore or create new
                val result = userRepository.getUser(uid)
                val user = result.getOrNull() ?: User(
                    uid = uid,
                    name = auth.currentUser?.displayName ?: "",
                    email = auth.currentUser?.email ?: "",
                    phoneNumber = "",
                    profileImageUrl = "",
                    createdAt = System.currentTimeMillis()
                ).also { userRepository.saveUser(it) }

                currentUser = user

                withContext(Dispatchers.Main) {
                    updateUI(user)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ProfileActivity, "Error loading profile: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } finally {
                withContext(Dispatchers.Main) { progressBar.visibility = ProgressBar.GONE }
            }
        }
    }

    private fun updateUI(user: User) {
        tvName.text = user.name.ifEmpty { "User Name" }
        tvEmail.text = user.email.ifEmpty { "user@email.com" }
        tvMobile.text = user.phoneNumber.ifEmpty { "Not provided" }

        if (user.profileImageUrl.isNotEmpty()) {
            Glide.with(this).load(user.profileImageUrl).placeholder(R.drawable.ic_profile_placeholder).into(ivProfile)
        } else {
            ivProfile.setImageResource(R.drawable.ic_profile_placeholder)
        }
    }

    private fun showProfileOptions() {
        val options = arrayOf("View Picture", "Upload New Picture")
        AlertDialog.Builder(this)
            .setTitle("Profile Options")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showProfileDialog()
                    1 -> pickImageLauncher.launch("image/*")
                }
            }.show()
    }

    private fun showProfileDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_view_image)
        dialog.setCancelable(true)
        val ivDialogImage = dialog.findViewById<ShapeableImageView>(R.id.ivDialogImage)
        if (!currentUser?.profileImageUrl.isNullOrEmpty())
            Glide.with(this).load(currentUser?.profileImageUrl).into(ivDialogImage)
        else
            ivDialogImage.setImageResource(R.drawable.ic_profile_placeholder)
        dialog.show()
    }

    private fun uploadProfileImage(uri: Uri) {
        val uid = auth.currentUser?.uid ?: return
        progressBar.visibility = ProgressBar.VISIBLE
        val ref = storage.reference.child("profile_images/$uid.jpg")

        // Upload the selected image Uri
        ref.putFile(uri)
            .addOnSuccessListener {
                // On success get download URL and update Firestore user profileImageUrl field
                ref.downloadUrl.addOnSuccessListener { downloadUri ->
                    lifecycleScope.launch {
                        val result = userRepository.updateUserProfile(uid, profileImageUrl = downloadUri.toString())
                        if (result.isSuccess) {
                            currentUser?.profileImageUrl = downloadUri.toString()
                            updateUI(currentUser!!)
                            Toast.makeText(this@ProfileActivity, "Profile picture updated!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@ProfileActivity, "Failed to update profile picture", Toast.LENGTH_SHORT).show()
                        }
                        progressBar.visibility = ProgressBar.GONE
                    }
                }.addOnFailureListener { e ->
                    Toast.makeText(this@ProfileActivity, "Failed to get download URL: ${e.message}", Toast.LENGTH_SHORT).show()
                    progressBar.visibility = ProgressBar.GONE
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
                progressBar.visibility = ProgressBar.GONE
            }
    }

    private fun showUpdateProfileDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_update_profile, null)
        val etName = dialogView.findViewById<EditText>(R.id.etName)
        val etPhone = dialogView.findViewById<EditText>(R.id.etPhone)
        etName.setText(currentUser?.name ?: "")
        etPhone.setText(currentUser?.phoneNumber ?: "")

        AlertDialog.Builder(this)
            .setTitle("Update Profile")
            .setView(dialogView)
            .setPositiveButton("Update") { _, _ ->
                val name = etName.text.toString().trim()
                val phone = etPhone.text.toString().trim()
                if (name.isEmpty()) {
                    Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                updateProfile(name, phone)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateProfile(name: String, phone: String) {
        val uid = auth.currentUser?.uid ?: return
        progressBar.visibility = ProgressBar.VISIBLE
        lifecycleScope.launch(Dispatchers.IO) {
            val result = userRepository.updateUserProfile(uid, name, phone)
            withContext(Dispatchers.Main) {
                if (result.isSuccess) {
                    currentUser?.name = name
                    currentUser?.phoneNumber = phone
                    updateUI(currentUser!!)
                    Toast.makeText(this@ProfileActivity, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                } else Toast.makeText(this@ProfileActivity, "Failed to update profile", Toast.LENGTH_SHORT).show()
                progressBar.visibility = ProgressBar.GONE
            }
        }
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                auth.signOut()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

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
                    startActivity(Intent(this, FeedbackActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_profile -> true // Already here
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
