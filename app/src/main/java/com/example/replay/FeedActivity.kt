package com.example.replay

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.replay.databinding.ActivityFeedBinding // 1. Import the binding class

class FeedActivity : AppCompatActivity() {

    // 2. Declare a binding variable
    private lateinit var binding: ActivityFeedBinding

    private lateinit var feedAdapter: FeedAdapter
    private val postList = mutableListOf<Post>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 3. Inflate the layout and set the content view
        binding = ActivityFeedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 4. Access views through the binding object
        feedAdapter = FeedAdapter(postList) {
            // Handle post click
        }

        binding.rvFeed.layoutManager = LinearLayoutManager(this)
        binding.rvFeed.adapter = feedAdapter

        loadPosts()

        binding.fabCreatePost.setOnClickListener {
            CreatePostBottomSheet().show(
                supportFragmentManager,
                "CreatePost"
            )
        }

        binding.bottomNav.selectedItemId = R.id.nav_home

        binding.bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> true
                R.id.nav_discover -> {
                    startActivity(Intent(this, DiscoverActivity::class.java))
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
