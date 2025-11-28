package com.example.hi

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.hi.model.User
import com.example.hi.repository.UserRepository
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

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

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { saveProfileImageLocally(it) }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val rootLayout = findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.rootLayout)
        ViewCompat.setOnApplyWindowInsetsListener(rootLayout) { view, insets ->
            val sysBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars())
            view.updatePadding(top = sysBars.top, bottom = sysBars.bottom)
            insets
        }

        window.statusBarColor = getColor(R.color.orange)

        ivProfile = findViewById(R.id.ivProfile)
        tvName = findViewById(R.id.tvName)
        tvEmail = findViewById(R.id.tvEmail)
        tvMobile = findViewById(R.id.tvMobile)
        btnUpdateProfile = findViewById(R.id.btnUpdateProfile)
        btnViewOrders = findViewById(R.id.btnViewOrders)
        logoutLayout = findViewById(R.id.llLogout)
        progressBar = findViewById(R.id.progressBar)

        findViewById<ImageView>(R.id.btnBack)?.setOnClickListener { finish() }

        auth.currentUser ?: run {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        loadUserData()

        ivProfile.setOnClickListener { showProfileOptions() }
        btnUpdateProfile.setOnClickListener { showUpdateProfileDialog() }
        btnViewOrders.setOnClickListener { startActivity(Intent(this, TrackOrderActivity::class.java)) }
        findViewById<LinearLayout>(R.id.btnFeedback)?.setOnClickListener { startActivity(Intent(this, FeedbackActivity::class.java)) }
        logoutLayout.setOnClickListener { showLogoutDialog() }

        setupBottomNavigation(findViewById(R.id.bottom_navigation))
    }

    override fun onResume() {
        super.onResume()
        loadUserData()
    }

    private fun loadUserData() {
        val uid = auth.currentUser?.uid ?: return
        progressBar.visibility = ProgressBar.VISIBLE

        lifecycleScope.launch(Dispatchers.IO) {
            val result = userRepository.getUser(uid)
            currentUser = result.getOrNull()

            withContext(Dispatchers.Main) {
                currentUser?.let { updateUI(it) }
                progressBar.visibility = ProgressBar.GONE
            }
        }
    }

    /** ✅ Corrected: load local image first, then Firestore URL, then placeholder */
    private fun updateUI(user: User) {
        tvName.text = user.name.ifEmpty { "User Name" }
        tvEmail.text = user.email.ifEmpty { "No Email" }
        tvMobile.text = user.phoneNumber.ifEmpty { "No Phone Number" }

        val localFile = File(filesDir, "profile_image.jpg")
        val imageUri: Any = if (localFile.exists()) {
            localFile.absolutePath
        } else if (user.profileImageUrl.isNotEmpty()) {
            Uri.parse(user.profileImageUrl)
        } else {
            R.drawable.ic_profile_placeholder
        }

        Glide.with(this)
            .load(imageUri)
            .into(ivProfile)
    }

    private fun showProfileOptions() {
        AlertDialog.Builder(this)
            .setTitle("Profile Options")
            .setItems(arrayOf("View Picture", "Upload New Picture")) { _, which ->
                when (which) {
                    0 -> showProfileDialog()
                    1 -> pickImageLauncher.launch("image/*")
                }
            }.show()
    }

    private fun showProfileDialog() {
        Dialog(this).apply {
            setContentView(R.layout.dialog_view_image)
            val img = findViewById<ShapeableImageView>(R.id.ivDialogImage)
            val localFile = File(filesDir, "profile_image.jpg")
            val imageUri: Any = if (localFile.exists()) {
                localFile.absolutePath
            } else {
                currentUser?.profileImageUrl?.ifEmpty { R.drawable.ic_profile_placeholder } ?: R.drawable.ic_profile_placeholder
            }

            Glide.with(this@ProfileActivity)
                .load(imageUri)
                .into(img)
            show()
        }
    }

    /** Save selected image to internal storage and update UI immediately */
    private fun saveProfileImageLocally(uri: Uri) {
        progressBar.visibility = ProgressBar.VISIBLE
        lifecycleScope.launch(Dispatchers.IO) {
            val savedPath = try {
                val inputStream = contentResolver.openInputStream(uri) ?: throw Exception("Cannot open image")
                val file = File(filesDir, "profile_image.jpg")
                file.outputStream().use { output -> inputStream.copyTo(output) }
                file.absolutePath
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }

            withContext(Dispatchers.Main) {
                progressBar.visibility = ProgressBar.GONE
                if (savedPath != null) {
                    Glide.with(this@ProfileActivity)
                        .load(savedPath)
                        .into(ivProfile)
                    Toast.makeText(this@ProfileActivity, "Profile Picture Updated!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@ProfileActivity, "Failed to save image", Toast.LENGTH_SHORT).show()
                }
            }
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
            .setPositiveButton("Update") { _, _ -> updateProfile(etName.text.toString(), etPhone.text.toString()) }
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
                    currentUser?.apply {
                        this.name = name
                        this.phoneNumber = phone
                    }
                    updateUI(currentUser!!)
                    Toast.makeText(this@ProfileActivity, "Profile Updated!", Toast.LENGTH_SHORT).show()
                }
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
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setupBottomNavigation(bottomNavigationView: BottomNavigationView) {
        bottomNavigationView.selectedItemId = R.id.nav_profile

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> { startActivity(Intent(this, activitybreakfast::class.java)); true }
                R.id.nav_menu -> { startActivity(Intent(this, activityhome1::class.java)); true }
                R.id.nav_feedback -> { startActivity(Intent(this, FeedbackActivity::class.java)); true }
                R.id.nav_profile -> true
                R.id.nav_cart -> { startActivity(Intent(this, OrderActivity::class.java)); true }
                else -> false
            }
        }
    }
}
