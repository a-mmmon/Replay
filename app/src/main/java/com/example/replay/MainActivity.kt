package com.example.replay

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var fabPost: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ CHECK IF USER IS LOGGED IN FIRST
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val isLoggedIn = prefs.getBoolean("is_logged_in", false)

        if (!isLoggedIn) {
            // Not logged in, redirect to login
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        setContentView(R.layout.activity_main)

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        fabPost = findViewById(R.id.fabPost)

        // Load default fragment once
        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
            bottomNavigation.selectedItemId = R.id.nav_home
        }

        // Floating Action Button click
        fabPost.setOnClickListener {
            CreatePostBottomSheet()
                .show(supportFragmentManager, "CreatePostBottomSheet")
        }

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {

                R.id.nav_home -> {
                    loadFragment(HomeFragment())
                    fabPost.show() // Show FAB only on home
                    true
                }

                R.id.nav_discover -> {
                    loadFragment(DiscoverFragment())
                    fabPost.hide()
                    true
                }

                R.id.nav_messages -> {
                    loadFragment(MessagesFragment())
                    fabPost.hide()
                    true
                }

                R.id.nav_library -> {
                    loadFragment(LibraryFragment())
                    fabPost.hide()
                    true
                }

                R.id.nav_profile -> {
                    loadFragment(ProfileFragment())
                    fabPost.hide()
                    true
                }

                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}