package com.example.replay

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : BaseThemedActivity() {

    private lateinit var fabPost: FloatingActionButton
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var auth: FirebaseAuth
    private val database = FirebaseDatabase.getInstance()

    private var unreadListener: ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_main)

        bottomNavigation = findViewById(R.id.bottomNavigation)
        fabPost = findViewById(R.id.fabPost)

        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
            bottomNavigation.selectedItemId = R.id.nav_home
        }

        fabPost.setOnClickListener {
            CreatePostBottomSheet().show(supportFragmentManager, "CreatePostBottomSheet")
        }

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    loadFragment(HomeFragment())
                    fabPost.show()
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
                    // Clear badge when user opens Messages tab
                    clearMessagesBadge()
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

        // Start listening for unread messages
        listenForUnreadMessages()
    }

    // ─── Listen to Firebase for total unread count across all conversations ──
    private fun listenForUnreadMessages() {
        val userId = auth.currentUser?.uid ?: return

        unreadListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var totalUnread = 0
                for (child in snapshot.children) {
                    val unread = child.child("unreadBadge").getValue(Int::class.java) ?: 0
                    totalUnread += unread
                }
                updateMessagesBadge(totalUnread)
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        database.getReference("conversations").child(userId)
            .addValueEventListener(unreadListener!!)
    }

    // ─── Show or hide the badge on the Messages nav item ─────────────────────
    private fun updateMessagesBadge(count: Int) {
        val badge = bottomNavigation.getOrCreateBadge(R.id.nav_messages)
        if (count > 0) {
            badge.isVisible = true
            badge.number   = count
        } else {
            badge.isVisible = false
            bottomNavigation.removeBadge(R.id.nav_messages)
        }
    }

    private fun clearMessagesBadge() {
        bottomNavigation.removeBadge(R.id.nav_messages)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up listener to avoid memory leaks
        val userId = auth.currentUser?.uid
        if (userId != null && unreadListener != null) {
            database.getReference("conversations").child(userId)
                .removeEventListener(unreadListener!!)
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    fun signOut() {
        auth.signOut()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    fun navigateToProfile() {
        bottomNavigation.selectedItemId = R.id.nav_profile
    }
}