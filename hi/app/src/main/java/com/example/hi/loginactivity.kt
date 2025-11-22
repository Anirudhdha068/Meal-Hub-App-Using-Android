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
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch

/**
 * LoginActivity - Handles email/password login and Google Sign-In
 * After login, saves user details to Firestore
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var loginButton: Button
    private lateinit var registerLink: TextView
    private lateinit var googleSignInButton: Button

    private val userRepository = UserRepository()

    companion object {
        private const val RC_SIGN_IN = 9001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_with_google)
        
        window.statusBarColor = getColor(R.color.orange)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Note: Removed auto-login check - always show login screen first
        // If you want auto-login, uncomment the following:
        // if (auth.currentUser != null) {
        //     navigateToHome()
        //     return
        // }

        // Initialize Google Sign-In
        // Note: You need to add the OAuth 2.0 Client ID from Firebase Console
        // Go to Firebase Console > Project Settings > Your Apps > SHA certificate fingerprint
        // Then add the Web Client ID to strings.xml as "default_web_client_id"
        val gso = try {
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        } catch (e: Exception) {
            // Fallback if web client ID not configured
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build()
        }

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Initialize views
        emailInput = findViewById(R.id.email_input)
        passwordInput = findViewById(R.id.password_input)
        loginButton = findViewById(R.id.login_button)
        registerLink = findViewById(R.id.register_link)
        googleSignInButton = findViewById(R.id.btnGoogleSignIn)

        // Handle email/password login
        loginButton.setOnClickListener {
            handleEmailPasswordLogin()
        }

        // Handle register link
        registerLink.setOnClickListener {
            startActivity(Intent(this, regactivity::class.java))
            finish()
        }

        // Handle Google Sign-In
        googleSignInButton.setOnClickListener {
            signInWithGoogle()
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
                        // Check if user exists in Firestore, if not create one
                        lifecycleScope.launch {
                            val userResult = userRepository.getUser(currentUser.uid)
                            if (userResult.isSuccess && userResult.getOrNull() == null) {
                                // User doesn't exist in Firestore, create one
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
     * Start Google Sign-In flow
     */
    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    /**
     * Handle Google Sign-In result
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                Toast.makeText(this, "Google sign in failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Authenticate with Firebase using Google account
     */
    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount?) {
        if (account == null) return

        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val currentUser = auth.currentUser
                    if (currentUser != null) {
                        // Save user to Firestore
                        lifecycleScope.launch {
                            val user = User(
                                uid = currentUser.uid,
                                name = account.displayName ?: "",
                                email = account.email ?: "",
                                phoneNumber = "",
                                profileImageUrl = account.photoUrl?.toString() ?: "",
                                createdAt = System.currentTimeMillis()
                            )
                            
                            val result = userRepository.saveUser(user)
                            if (result.isSuccess) {
                                Toast.makeText(this@LoginActivity, "Login successful!", Toast.LENGTH_SHORT).show()
                                navigateToHome()
                            } else {
                                Toast.makeText(this@LoginActivity, "Login successful but failed to save profile", Toast.LENGTH_SHORT).show()
                                navigateToHome()
                            }
                        }
                    }
                } else {
                    Toast.makeText(this, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    /**
     * Save user to Firestore
     */
    private suspend fun saveUserToFirestore(uid: String, email: String, name: String) {
        val user = User(
            uid = uid,
            name = name,
            email = email,
            phoneNumber = "",
            profileImageUrl = "",
            createdAt = System.currentTimeMillis()
        )
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
