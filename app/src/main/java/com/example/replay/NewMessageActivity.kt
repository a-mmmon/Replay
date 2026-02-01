package com.example.replay

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class NewMessageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_message)

        val rvUsers = findViewById<RecyclerView>(R.id.rvUsersList)

        // Sample users to message - use UserProfile from DataModels.kt
        val users = listOf(
            UserProfile(
                userId = "user1",
                username = "Taylor Swift",
                handle = "@taylorswift",
                profileImage = "",
                bio = "Singer-songwriter",
                followers = 1000000,
                following = 500
            ),
            UserProfile(
                userId = "user2",
                username = "Ariana Grande",
                handle = "@arianagrande",
                profileImage = "",
                bio = "Artist",
                followers = 2000000,
                following = 600
            ),
            UserProfile(
                userId = "user3",
                username = "BTS",
                handle = "@bts_official",
                profileImage = "",
                bio = "K-Pop Group",
                followers = 5000000,
                following = 100
            ),
            UserProfile(
                userId = "user4",
                username = "Ed Sheeran",
                handle = "@edsheeran",
                profileImage = "",
                bio = "Musician",
                followers = 1500000,
                following = 400
            ),
            UserProfile(
                userId = "user5",
                username = "Dua Lipa",
                handle = "@dualipa",
                profileImage = "",
                bio = "Pop Artist",
                followers = 1800000,
                following = 450
            ),
            UserProfile(
                userId = "user6",
                username = "The Weeknd",
                handle = "@theweeknd",
                profileImage = "",
                bio = "R&B Artist",
                followers = 2500000,
                following = 300
            )
        )

        val adapter = UsersToMessageAdapter(users) { user ->
            // Open chat with selected user
            Toast.makeText(this, "Chat with ${user.username} (Coming soon)", Toast.LENGTH_SHORT).show()
            finish()
        }

        rvUsers.layoutManager = LinearLayoutManager(this)
        rvUsers.adapter = adapter

        // Back button
        findViewById<android.widget.ImageButton>(R.id.btnBack)?.setOnClickListener {
            finish()
        }
    }
}