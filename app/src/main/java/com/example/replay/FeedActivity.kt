package com.example.replay

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.chip.ChipGroup
import com.google.android.material.appbar.MaterialToolbar

class FeedActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var chipGroup: ChipGroup
    private lateinit var feedRecyclerView: RecyclerView
    private lateinit var fabCreatePost: FloatingActionButton

    private lateinit var feedAdapter: FeedAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feed)

        initViews()
        setupToolbar()
        setupRecyclerView()
        setupListeners()
        loadFeedData()
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        chipGroup = findViewById(R.id.chipGroupFeed)
        feedRecyclerView = findViewById(R.id.feedRecyclerView)
        fabCreatePost = findViewById(R.id.fabCreatePost)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Feed"

        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        feedRecyclerView.layoutManager = LinearLayoutManager(this)
        feedAdapter = FeedAdapter(emptyList()) { post ->
            onPostClicked(post)
        }
        feedRecyclerView.adapter = feedAdapter
    }

    private fun setupListeners() {
        fabCreatePost.setOnClickListener {
            showCreatePostDialog()
        }

        chipGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.chipText -> filterTextPosts()
                R.id.chipMusicFeed -> filterMusicPosts()
            }
        }
    }

    private fun loadFeedData() {
        val posts = getSamplePosts()
        feedAdapter.updateData(posts)
    }

    private fun filterTextPosts() {
        val textPosts = getSamplePosts().filter { it.type == PostType.TEXT }
        feedAdapter.updateData(textPosts)
    }

    private fun filterMusicPosts() {
        val musicPosts = getSamplePosts().filter { it.type == PostType.MUSIC }
        feedAdapter.updateData(musicPosts)
    }

    private fun showCreatePostDialog() {
        val dialog = CreatePostBottomSheet()
        dialog.setOnPostCreatedListener { post ->
            // Add new post to feed
            loadFeedData()
        }
        dialog.show(supportFragmentManager, "CreatePostBottomSheet")
    }

    private fun onPostClicked(post: Post) {
        // Handle post click - play music, open details, etc.
    }

    private fun getSamplePosts(): List<Post> {
        return listOf(
            Post(
                id = "1",
                userId = "user1",
                userName = "John Doe",
                userAvatar = "https://picsum.photos/100",
                content = "Just discovered this amazing track! 🎵",
                type = PostType.MUSIC,
                musicAttachment = Music("1", "Song Title", "Artist Name", "https://picsum.photos/400"),
                timestamp = System.currentTimeMillis(),
                likes = 100,
                comments = 25
            ),
            Post(
                id = "2",
                userId = "user2",
                userName = "Jane Smith",
                userAvatar = "https://picsum.photos/101",
                content = "What's everyone listening to today?",
                type = PostType.TEXT,
                musicAttachment = null,
                timestamp = System.currentTimeMillis() - 3600000,
                likes = 45,
                comments = 12
            ),
            Post(
                id = "3",
                userId = "user3",
                userName = "Mike Johnson",
                userAvatar = "https://picsum.photos/102",
                content = "New album alert! This is fire 🔥",
                type = PostType.MUSIC,
                musicAttachment = Music("2", "Album Track", "Cool Artist", "https://picsum.photos/401"),
                timestamp = System.currentTimeMillis() - 7200000,
                likes = 230,
                comments = 56
            )
        )
    }
}