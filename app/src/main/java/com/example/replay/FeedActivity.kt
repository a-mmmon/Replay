package com.example.replay

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class FeedActivity : AppCompatActivity() {

    private lateinit var rvFeed: RecyclerView
    private lateinit var feedAdapter: FeedAdapter
    private lateinit var fabCreatePost: FloatingActionButton
    private lateinit var bottomNav: BottomNavigationView

    private val postList = mutableListOf<Post>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feed)

        rvFeed = findViewById(R.id.rvFeed)
        fabCreatePost = findViewById(R.id.fabCreatePost)
        bottomNav = findViewById(R.id.bottomNav)

        feedAdapter = FeedAdapter(postList) {

        }

        rvFeed.layoutManager = LinearLayoutManager(this)
        rvFeed.adapter = feedAdapter

        loadPosts()

        fabCreatePost.setOnClickListener {
            CreatePostBottomSheet().show(
                supportFragmentManager,
                "CreatePost"
            )
        }

        bottomNav.selectedItemId = R.id.nav_home

        bottomNav.setOnItemSelectedListener {
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
