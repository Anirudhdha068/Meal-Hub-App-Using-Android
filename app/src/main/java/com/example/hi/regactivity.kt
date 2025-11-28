package com.example.hi

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.hi.model.User
import com.example.hi.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class regactivity : AppCompatActivity() {

    private lateinit var etFullName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var tvLogin: TextView
    private lateinit var btnRegister: Button
    private lateinit var auth: FirebaseAuth
    private var isRegistering = false
    private val userRepository = UserRepository()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_regactivity)

        window.statusBarColor = getColor(R.color.orange)
        window.decorView.systemUiVisibility = 0

        auth = FirebaseAuth.getInstance()

        etFullName = findViewById(R.id.et_fullname)
        etEmail = findViewById(R.id.et_email)
        etPassword = findViewById(R.id.et_password)
        etConfirmPassword = findViewById(R.id.et_confirm_password)
        tvLogin = findViewById(R.id.tv_login)
        btnRegister = findViewById(R.id.btn_register)

        btnRegister.setOnClickListener {
            if (isRegistering) return@setOnClickListener

            val fullName = etFullName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            when {
                fullName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() -> {
                    showToast("Please fill all fields")
                }
                !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    showToast("Enter a valid email")
                }
                password.length < 6 -> {
                    showToast("Password must be at least 6 characters")
                }
                password != confirmPassword -> {
                    showToast("Passwords do not match")
                }
                else -> registerUser(fullName, email, password)
            }
        }

        tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun registerUser(fullName: String, email: String, password: String) {
        isRegistering = true
        btnRegister.isEnabled = false

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    
                    if (userId != null) {
                        // Save user to Firestore
                        lifecycleScope.launch {
                            try {
                                val user = User(
                                    uid = userId,
                                    name = fullName,
                                    email = email,
                                    phoneNumber = "",
                                    profileImageUrl = "",
                                    createdAt = System.currentTimeMillis()
                                )
                                
                                val result = userRepository.saveUser(user)
                                
                                if (result.isSuccess) {
                                    showToast("Registered Successfully!")
                                    val intent = Intent(this@regactivity, activityhome1::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    startActivity(intent)
                                    finish()
                                } else {
                                    showToast("Account created but saving data failed.")
                                    val intent = Intent(this@regactivity, LoginActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                            } catch (e: Exception) {
                                showToast("Error: ${e.message}")
                            } finally {
                                isRegistering = false
                                btnRegister.isEnabled = true
                            }
                        }
                    }
                } else {
                    // 🔹 Reset state if failed
                    isRegistering = false
                    btnRegister.isEnabled = true
                    val errorMessage = when {
                        task.exception?.message?.contains("email address is already in use") == true ->
                            "This email is already registered. Please login."
                        else -> "Registration failed: ${task.exception?.message}"
                    }
                    showToast(errorMessage)
                }
            }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
