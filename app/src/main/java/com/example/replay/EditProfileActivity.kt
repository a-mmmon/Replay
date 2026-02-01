package com.example.replay

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class EditProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Implement full edit profile UI
        // For now, just show a placeholder message
        Toast.makeText(this, "Edit Profile - Coming Soon!", Toast.LENGTH_SHORT).show()
        finish()
    }
}