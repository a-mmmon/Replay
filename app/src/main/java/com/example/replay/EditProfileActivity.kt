package com.example.replay

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class EditProfileActivity : AppCompatActivity() {

    private lateinit var etUsername: EditText
    private lateinit var etHandle: EditText
    private lateinit var etBio: EditText
    private lateinit var btnSave: Button
    private lateinit var progressBar: ProgressBar

    // Firebase
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        etUsername = findViewById(R.id.etUsername)
        etHandle = findViewById(R.id.etHandle)
        etBio = findViewById(R.id.etBio)
        btnSave = findViewById(R.id.btnSave)
        progressBar = findViewById(R.id.progressBar)

        // Load existing profile data first
        loadCurrentProfile()

        btnSave.setOnClickListener {
            saveProfile()
        }
    }

    // ─── FIREBASE: Load existing profile ─────────────────────────────────────
    private fun loadCurrentProfile() {
        val userId = auth.currentUser?.uid ?: return
        progressBar.visibility = View.VISIBLE

        database.getReference("users").child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    progressBar.visibility = View.GONE
                    val profile = snapshot.getValue(UserProfile::class.java)

                    if (profile != null) {
                        etUsername.setText(profile.username)
                        etHandle.setText(profile.handle.removePrefix("@"))
                        etBio.setText(profile.bio)
                    } else {
                        // First time — pre-fill with email
                        val email = auth.currentUser?.email ?: ""
                        etUsername.setText(email.substringBefore("@"))
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    progressBar.visibility = View.GONE
                    Log.e("EditProfile", "Failed to load: ${error.message}")
                }
            })
    }

    // ─── FIREBASE: Save profile to Realtime Database ──────────────────────────
    private fun saveProfile() {
        val userId = auth.currentUser?.uid ?: run {
            Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val username = etUsername.text.toString().trim()
        val handle = etHandle.text.toString().trim()
        val bio = etBio.text.toString().trim()

        // Basic validation
        if (username.isEmpty()) {
            etUsername.error = "Username is required"
            return
        }

        if (handle.isEmpty()) {
            etHandle.error = "Handle is required"
            return
        }

        btnSave.isEnabled = false
        progressBar.visibility = View.VISIBLE

        val updatedProfile = UserProfile(
            userId = userId,
            username = username,
            handle = "@$handle",
            bio = bio,
            profileImage = "",
            followers = 0,
            following = 0
        )

        database.getReference("users").child(userId)
            .setValue(updatedProfile)
            .addOnSuccessListener {
                progressBar.visibility = View.GONE
                btnSave.isEnabled = true
                Toast.makeText(this, "Profile saved!", Toast.LENGTH_SHORT).show()
                Log.d("EditProfile", "Profile saved to Firebase")
                finish()
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                btnSave.isEnabled = true
                Toast.makeText(this, "Failed to save: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("EditProfile", "Save failed: ${e.message}")
            }
    }
}