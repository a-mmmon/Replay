package com.example.replay

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvSignUp: TextView
    private var isSignUpMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if already logged in
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val isLoggedIn = prefs.getBoolean("is_logged_in", false)

        if (isLoggedIn) {
            // Already logged in, go to main activity
            navigateToMain()
            return
        }

        setContentView(R.layout.activity_login)

        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvSignUp = findViewById(R.id.tvSignUp)

        btnLogin.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (isSignUpMode) {
                // Sign up
                performSignUp(username, password)
            } else {
                // Login
                performLogin(username, password)
            }
        }

        tvSignUp.setOnClickListener {
            toggleMode()
        }
    }

    private fun performLogin(username: String, password: String) {
        // Simple validation (in real app, verify with backend)
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val savedUsername = prefs.getString("username", "")
        val savedPassword = prefs.getString("password", "")

        if (username == savedUsername && password == savedPassword) {
            // Login successful
            prefs.edit().putBoolean("is_logged_in", true).apply()

            Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
            navigateToMain()
        } else {
            Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show()
        }
    }

    private fun performSignUp(username: String, password: String) {
        // Validate password length
        if (password.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            return
        }

        // Save credentials (in real app, send to backend)
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        prefs.edit().apply {
            putString("username", username)
            putString("password", password)
            putString("user_handle", "@$username")
            putBoolean("is_logged_in", true)
            apply()
        }

        Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show()
        navigateToMain()
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