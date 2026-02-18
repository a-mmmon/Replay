package com.example.replay

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UserProfileActivity : AppCompatActivity() {

    private lateinit var backButton: ImageButton
    private lateinit var profileImage: ShapeableImageView
    private lateinit var usernameText: TextView
    private lateinit var handleText: TextView
    private lateinit var followersCount: TextView
    private lateinit var followingCount: TextView
    private lateinit var bioText: TextView
    private lateinit var messageButton: TextView
    private lateinit var postsRecyclerView: RecyclerView

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()

    private var targetUserId = ""
    private var targetUsername = ""
    private val userPosts = mutableListOf<Post>()
    private lateinit var postsAdapter: FeedAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        targetUserId = intent.getStringExtra("user_id") ?: ""
        targetUsername = intent.getStringExtra("username") ?: ""

        initializeViews()
        setupBackButton()
        loadUserProfile()
        setupRecyclerView()
        loadUserPosts()
        setupMessageButton()
    }

    private fun initializeViews() {
        backButton = findViewById(R.id.backButton)
        profileImage = findViewById(R.id.profileImage)
        usernameText = findViewById(R.id.usernameText)
        handleText = findViewById(R.id.handleText)
        followersCount = findViewById(R.id.followersCount)
        followingCount = findViewById(R.id.followingCount)
        bioText = findViewById(R.id.bioText)
        messageButton = findViewById(R.id.messageButton)
        postsRecyclerView = findViewById(R.id.postsRecyclerView)

        // Set username immediately from intent while Firebase loads
        usernameText.text = targetUsername
    }

    private fun setupBackButton() {
        backButton.setOnClickListener { finish() }
    }

    // ─── Load user profile from Firebase ─────────────────────────────────────
    private fun loadUserProfile() {
        if (targetUserId.isEmpty()) return

        database.getReference("users").child(targetUserId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val profile = snapshot.getValue(UserProfile::class.java) ?: return
                    usernameText.text = profile.username
                    handleText.text = profile.handle
                    followersCount.text = profile.followers.toString()
                    followingCount.text = profile.following.toString()
                    bioText.text = profile.bio.ifEmpty { "No bio yet" }
                    Log.d("UserProfileActivity", "Loaded profile: ${profile.username}")
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("UserProfileActivity", "Failed to load profile: ${error.message}")
                }
            })
    }

    private fun setupRecyclerView() {
        postsAdapter = FeedAdapter(
            posts = userPosts,
            onPostClick = { },
            onUsernameClick = { }
        )
        postsRecyclerView.layoutManager = LinearLayoutManager(this)
        postsRecyclerView.adapter = postsAdapter
    }

    // ─── Load this user's posts from Firebase ────────────────────────────────
    private fun loadUserPosts() {
        if (targetUserId.isEmpty()) return

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
                    userPosts.sortByDescending { it.timestamp }
                    postsAdapter.notifyDataSetChanged()
                    Log.d("UserProfileActivity", "Loaded ${userPosts.size} posts")
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("UserProfileActivity", "Failed to load posts: ${error.message}")
                }
            })
    }

    // ─── Message this user ────────────────────────────────────────────────────
    private fun setupMessageButton() {
        messageButton.setOnClickListener {
            val currentUserId = auth.currentUser?.uid ?: return@setOnClickListener
            val conversationId = listOf(currentUserId, targetUserId).sorted().joinToString("_")

            val intent = Intent(this, ConversationActivity::class.java)
            intent.putExtra("conversation_id", conversationId)
            intent.putExtra("other_user_id", targetUserId)
            intent.putExtra("other_user_name", targetUsername)
            intent.putExtra("other_user_image", "")
            startActivity(intent)
        }
    }
}