package com.example.hi

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.hi.model.User
import com.example.hi.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var loginButton: Button
    private lateinit var registerLink: TextView

    private val userRepository = UserRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login) // Keep same layout, just ignore google button
        window.statusBarColor = getColor(R.color.orange)

        auth = FirebaseAuth.getInstance()

        // Initialize views
        emailInput = findViewById(R.id.email_input)
        passwordInput = findViewById(R.id.password_input)
        loginButton = findViewById(R.id.login_button)
        registerLink = findViewById(R.id.register_link)

        // Handle email/password login
        loginButton.setOnClickListener {
            handleEmailPasswordLogin()
        }

        // Register link
        registerLink.setOnClickListener {
            startActivity(Intent(this, regactivity::class.java))
            finish()
        }
    }

    /**
     * Handle email/password login
     */
    private fun handleEmailPasswordLogin() {
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Enter a valid email", Toast.LENGTH_SHORT).show()
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val currentUser = auth.currentUser
                    if (currentUser != null) {
                        lifecycleScope.launch {
                            val userResult = userRepository.getUser(currentUser.uid)
                            if (userResult.isSuccess && userResult.getOrNull() == null) {
                                saveUserToFirestore(
                                    currentUser.uid,
                                    currentUser.email ?: "",
                                    currentUser.displayName ?: ""
                                )
                            }
                        }
                        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                        navigateToHome()
                    }
                } else {
                    Toast.makeText(
                        this,
                        "Login failed: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    /**
     * Save user to Firestore
     */
    private suspend fun saveUserToFirestore(uid: String, email: String, name: String) {
        val user = User(uid, name, email, "", "", System.currentTimeMillis())
        userRepository.saveUser(user)
    }

    /**
     * Navigate to HomeActivity
     */
    private fun navigateToHome() {
        val intent = Intent(this, activityhome1::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
