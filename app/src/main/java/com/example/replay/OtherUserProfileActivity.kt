package com.example.replay

import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class OtherUserProfileActivity : AppCompatActivity() {

    private lateinit var profileImage: ShapeableImageView
    private lateinit var usernameText: TextView
    private lateinit var handleText: TextView
    private lateinit var followersCount: TextView
    private lateinit var followingCount: TextView
    private lateinit var bioText: TextView
    private lateinit var postsRecyclerView: RecyclerView
    private lateinit var tabLayout: TabLayout
    private lateinit var backButton: ImageButton
    private lateinit var messageButton: ImageButton

    private var postsAdapter: FeedAdapter? = null
    private val userPosts = mutableListOf<Post>()

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()

    private var targetUserId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_other_user_profile)

        // Get user ID from intent
        targetUserId = intent.getStringExtra("userId") ?: ""
        if (targetUserId.isEmpty()) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initializeViews()
        setupRecyclerView()
        setupTabs()
        setupButtons()
        loadUserProfile()
        loadUserPosts()
    }

    private fun initializeViews() {
        profileImage = findViewById(R.id.profileImage)
        usernameText = findViewById(R.id.usernameText)
        handleText = findViewById(R.id.handleText)
        followersCount = findViewById(R.id.followersCount)
        followingCount = findViewById(R.id.followingCount)
        bioText = findViewById(R.id.bioText)
        postsRecyclerView = findViewById(R.id.postsRecyclerView)
        tabLayout = findViewById(R.id.tabLayout)
        backButton = findViewById(R.id.backButton)
        messageButton = findViewById(R.id.messageButton)
    }

    private fun setupRecyclerView() {
        postsAdapter = FeedAdapter(
            posts = userPosts,
            onPostClick = { post ->
                Log.d("ProfileFragment", "Post clicked: ${post.postId}")
            }// REMOVE THE onUserClick PARAMETER FROM HERE
        )
        postsRecyclerView.layoutManager = LinearLayoutManager(this)
        postsRecyclerView.adapter = postsAdapter
    }


    private fun setupTabs() {
        tabLayout.removeAllTabs()
        tabLayout.addTab(tabLayout.newTab().setText("Posts"))

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> loadUserPosts()
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupButtons() {
        // ✅ FIXED: Back button now works
        backButton.setOnClickListener {
            finish()
        }

        messageButton.setOnClickListener {
            // TODO: Open conversation with this user
            Toast.makeText(this, "Messaging feature coming soon!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadUserProfile() {
        database.getReference("users").child(targetUserId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val profile = snapshot.getValue(UserProfile::class.java)
                    if (profile != null) {
                        usernameText.text = profile.username

                        // ✅ FIXED: Handle might be missing, generate from username or email
                        val handle = if (profile.handle.isNotEmpty()) {
                            profile.handle
                        } else {
                            "@${profile.username}"
                        }
                        handleText.text = handle

                        followersCount.text = profile.followers.toString()
                        followingCount.text = profile.following.toString()
                        bioText.text = profile.bio.ifEmpty { "No bio yet" }

                        // TODO: Load profile image with Glide
                        // if (profile.profileImage.isNotEmpty()) {
                        //     Glide.with(this@OtherUserProfileActivity)
                        //         .load(profile.profileImage)
                        //         .into(profileImage)
                        // }

                        Log.d("OtherUserProfile", "Loaded profile: ${profile.username}")
                    } else {
                        Toast.makeText(
                            this@OtherUserProfileActivity,
                            "User profile not found",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("OtherUserProfile", "Failed to load profile: ${error.message}")
                    Toast.makeText(
                        this@OtherUserProfileActivity,
                        "Failed to load profile",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun loadUserPosts() {
        database.getReference("posts")
            .orderByChild("userId")
            .equalTo(targetUserId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    userPosts.clear()

                    for (child in snapshot.children) {
                        val post = child.getValue(Post::class.java)
                        post?.let { userPosts.add(it) }
                    }

                    // Sort by timestamp (newest first)
                    userPosts.sortByDescending { it.timestamp }

                    postsAdapter?.notifyDataSetChanged()
                    tabLayout.getTabAt(0)?.text = "Posts (${userPosts.size})"
                    Log.d("OtherUserProfile", "Loaded ${userPosts.size} posts")
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("OtherUserProfile", "Failed to load posts: ${error.message}")
                }
            })
    }
}