package com.example.replay

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.database.FirebaseDatabase

class LoginActivity : BaseThemedActivity() {

    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvSignUp: TextView
    private var isSignUpMode = false

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        // Check if already logged in
        if (auth.currentUser != null) {
            navigateToMain()
            return
        }

        setContentView(R.layout.activity_login)

        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvSignUp = findViewById(R.id.tvSignUp)
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

            btnLogin.isEnabled = false
            btnLogin.text = if (isSignUpMode) "Creating account..." else "Logging in..."

            if (isSignUpMode) performSignUp(email, password)
            else performLogin(email, password)
        }

        tvSignUp.setOnClickListener { toggleMode() }
    }

    private fun performLogin(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                btnLogin.isEnabled = true
                btnLogin.text = "Log In"
                if (task.isSuccessful) {
                    Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                    navigateToMain()
                } else {
                    val msg = when (task.exception) {
                        is FirebaseAuthInvalidUserException -> "No account found with this email"
                        is FirebaseAuthInvalidCredentialsException -> "Incorrect password"
                        else -> task.exception?.message ?: "Login failed"
                    }
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
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
                btnLogin.isEnabled = true
                btnLogin.text = "Sign Up"

                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: return@addOnCompleteListener
                    val username = email.substringBefore("@")

                    // ✅ KEY FIX: Save user profile to Firebase so they appear in search!
                    val initialProfile = UserProfile(
                        userId = userId,
                        username = username,
                        handle = "@$username",
                        bio = "",
                        profileImage = "",
                        followers = 0,
                        following = 0
                    )

                    FirebaseDatabase.getInstance()
                        .getReference("users")
                        .child(userId)
                        .setValue(initialProfile)
                        .addOnSuccessListener {
                            Log.d("LoginActivity", "Profile saved: $username")
                        }
                        .addOnFailureListener { e ->
                            Log.e("LoginActivity", "Failed to save profile: ${e.message}")
                        }

                    Toast.makeText(this, "Account created!", Toast.LENGTH_SHORT).show()
                    navigateToMain()
                } else {
                    val msg = when (task.exception) {
                        is FirebaseAuthWeakPasswordException -> "Password is too weak"
                        is FirebaseAuthUserCollisionException -> "Email already in use"
                        is FirebaseAuthInvalidCredentialsException -> "Invalid email"
                        else -> task.exception?.message ?: "Sign up failed"
                    }
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun toggleMode() {
        isSignUpMode = !isSignUpMode
        btnLogin.text = if (isSignUpMode) "Sign Up" else "Log In"
        tvSignUp.text = if (isSignUpMode) "Already have an account? Log In"
        else "Don't have an account? Sign Up"
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}