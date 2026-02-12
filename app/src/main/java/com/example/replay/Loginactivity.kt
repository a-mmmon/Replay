package com.example.replay

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseAuthUserCollisionException

class LoginActivity : AppCompatActivity() {

    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvSignUp: TextView
    private var isSignUpMode = false

    // Firebase Auth instance
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // TEMPORARY FIX: Force sign out to fix navigation issue
        // Remove this line after you successfully log in again
        auth.signOut()

        // Check if already logged in via Firebase
        if (auth.currentUser != null) {
            navigateToMain()
            return
        }

        setContentView(R.layout.activity_login)

        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvSignUp = findViewById(R.id.tvSignUp)

        // Update hint to Email since Firebase Auth uses email
        etUsername.hint = "Email"

        btnLogin.setOnClickListener {
            val email = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Disable button to prevent double clicks
            btnLogin.isEnabled = false
            btnLogin.text = if (isSignUpMode) "Creating account..." else "Logging in..."

            if (isSignUpMode) {
                performSignUp(email, password)
            } else {
                performLogin(email, password)
            }
        }

        tvSignUp.setOnClickListener {
            toggleMode()
        }
    }

    private fun performLogin(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                // Re-enable button
                btnLogin.isEnabled = true
                btnLogin.text = "Log In"

                if (task.isSuccessful) {
                    Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                    navigateToMain()
                } else {
                    // Show specific error messages
                    val errorMessage = when (task.exception) {
                        is FirebaseAuthInvalidUserException ->
                            "No account found with this email"
                        is FirebaseAuthInvalidCredentialsException ->
                            "Incorrect password"
                        else -> task.exception?.message ?: "Login failed"
                    }
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun performSignUp(email: String, password: String) {
        if (password.length < 6) {
            btnLogin.isEnabled = true
            btnLogin.text = "Sign Up"
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                // Re-enable button
                btnLogin.isEnabled = true
                btnLogin.text = "Sign Up"

                if (task.isSuccessful) {
                    Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show()
                    navigateToMain()
                } else {
                    // Show specific error messages
                    val errorMessage = when (task.exception) {
                        is FirebaseAuthWeakPasswordException ->
                            "Password is too weak. Use at least 6 characters"
                        is FirebaseAuthUserCollisionException ->
                            "An account already exists with this email"
                        is FirebaseAuthInvalidCredentialsException ->
                            "Invalid email address"
                        else -> task.exception?.message ?: "Sign up failed"
                    }
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun toggleMode() {
        isSignUpMode = !isSignUpMode
        if (isSignUpMode) {
            btnLogin.text = "Sign Up"
            tvSignUp.text = "Already have an account? Log In"
        } else {
            btnLogin.text = "Log In"
            tvSignUp.text = "Don't have an account? Sign Up"
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}