package com.example.replay

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.replay.databinding.ActivityFeedBinding // 1. Import ViewBinding

class FeedActivity : AppCompatActivity() {

    // 2. Declare the binding variable
    private lateinit var binding: ActivityFeedBinding

    private lateinit var feedAdapter: FeedAdapter
    private val postList = mutableListOf<Post>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 3. Inflate the layout using ViewBinding
        binding = ActivityFeedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup RecyclerView
        feedAdapter = FeedAdapter(postList) { post ->
            // Handle post click if you need to
        }
        binding.rvFeed.layoutManager = LinearLayoutManager(this)
        binding.rvFeed.adapter = feedAdapter

        loadPosts()

        // 4. Set the current item in the BottomNav
        binding.bottomNav.selectedItemId = R.id.nav_home

        // 5. Set up the listener for the BottomNav
        binding.bottomNav.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    // Already on the Home/Feed screen
                    true
                }
                R.id.nav_discover -> {
                    startActivity(Intent(this, DiscoverActivity::class.java))
                    // Add flags to prevent creating a new activity if it's already running
                    // and to clear the stack above it.
                    overridePendingTransition(0, 0) // Optional: for no animation
                    true
                }
                R.id.nav_post -> {
                    // *** THIS IS THE FIX ***
                    // Open the CreatePost bottom sheet
                    CreatePostBottomSheet().show(supportFragmentManager, "CreatePostBottomSheetTag")
                    // Return false so the 'Home' item remains selected
                    false
                }
                R.id.nav_library -> {
                    startActivity(Intent(this, LibraryActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_profile -> {
                    // Handle profile navigation if you have a ProfileActivity
                    true
                }
                else -> false
            }
        }
    }

    private fun loadPosts() {
        postList.clear()
        postList.addAll(SampleData.posts)
        feedAdapter.notifyDataSetChanged()
    }
}
