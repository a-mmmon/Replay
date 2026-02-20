package com.example.replay

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UserProfileActivity : BaseThemedActivity() {

    private lateinit var backButton: ImageButton
    private lateinit var profileImage: ShapeableImageView
    private lateinit var usernameText: TextView
    private lateinit var handleText: TextView
    private lateinit var followersCount: TextView
    private lateinit var followingCount: TextView
    private lateinit var followersSection: View
    private lateinit var followingSection: View
    private lateinit var bioText: TextView
    private lateinit var followButton: MaterialButton
    private lateinit var messageButton: MaterialButton
    private lateinit var connectionStatusText: TextView
    private lateinit var tabLayout: TabLayout
    private lateinit var postsRecyclerView: RecyclerView

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private val currentUserId get() = auth.currentUser?.uid ?: ""

    private var targetUserId = ""
    private var targetUsername = ""
    private var targetUserImage = ""
    private var isFollowing = false
    private var followsBack = false
    private var selectedTab = 0
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
        setupTabs()
        loadContentForSelectedTab()
        setupFollowButton()
        setupMessageButton()
    }

    private fun initializeViews() {
        backButton = findViewById(R.id.backButton)
        profileImage = findViewById(R.id.profileImage)
        usernameText = findViewById(R.id.usernameText)
        handleText = findViewById(R.id.handleText)
        followersCount = findViewById(R.id.followersCount)
        followingCount = findViewById(R.id.followingCount)
        followersSection = findViewById(R.id.followersSection)
        followingSection = findViewById(R.id.followingSection)
        bioText = findViewById(R.id.bioText)
        followButton = findViewById(R.id.followButton)
        messageButton = findViewById(R.id.messageButton)
        connectionStatusText = findViewById(R.id.connectionStatusText)
        tabLayout = findViewById(R.id.tabLayout)
        postsRecyclerView = findViewById(R.id.postsRecyclerView)

        // Set username immediately from intent while Firebase loads
        usernameText.text = targetUsername
        profileImage.setImageResource(ThemeManager.getDefaultAvatarRes(this))
        followersSection.setOnClickListener { openFollowList("followers") }
        followingSection.setOnClickListener { openFollowList("following") }
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
                    targetUsername = profile.username
                    targetUserImage = profile.profileImage

                    if (profile.profileImage.isNotBlank()) {
                        Glide.with(this@UserProfileActivity)
                            .load(profile.profileImage)
                            .placeholder(ThemeManager.getDefaultAvatarRes(this@UserProfileActivity))
                            .error(ThemeManager.getDefaultAvatarRes(this@UserProfileActivity))
                            .into(profileImage)
                    } else {
                        profileImage.setImageResource(
                            ThemeManager.getDefaultAvatarRes(this@UserProfileActivity)
                        )
                    }

                    loadRelationshipState()
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
            onPostClick = { }
        )
        postsRecyclerView.layoutManager = LinearLayoutManager(this)
        postsRecyclerView.adapter = postsAdapter
    }

    private fun setupTabs() {
        tabLayout.removeAllTabs()
        tabLayout.addTab(tabLayout.newTab().setText("Posts"))
        tabLayout.addTab(tabLayout.newTab().setText("Likes"))
        tabLayout.addTab(tabLayout.newTab().setText("Reposts"))
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                selectedTab = tab?.position ?: 0
                loadContentForSelectedTab()
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) = Unit
            override fun onTabReselected(tab: TabLayout.Tab?) = Unit
        })
    }

    private fun loadContentForSelectedTab() {
        when (selectedTab) {
            1 -> loadUserLikes()
            2 -> loadUserReposts()
            else -> loadUserPosts()
        }
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
                        if (post != null && !post.isRepost && post.originalPostId.isBlank()) {
                            userPosts.add(post)
                        }
                    }
                    userPosts.sortByDescending { it.timestamp }
                    postsAdapter.notifyDataSetChanged()
                    tabLayout.getTabAt(0)?.text = "Posts (${userPosts.size})"
                    Log.d("UserProfileActivity", "Loaded ${userPosts.size} posts")
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("UserProfileActivity", "Failed to load posts: ${error.message}")
                }
            })
    }

    private fun loadUserLikes() {
        if (targetUserId.isBlank()) return
        database.getReference("userLikes").child(targetUserId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val likedIds = snapshot.children.mapNotNull { it.key }
                    if (likedIds.isEmpty()) {
                        userPosts.clear()
                        postsAdapter.notifyDataSetChanged()
                        tabLayout.getTabAt(1)?.text = "Likes (0)"
                        return
                    }

                    val fetched = mutableListOf<Post>()
                    var count = 0
                    likedIds.forEach { id ->
                        database.getReference("posts").child(id)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(postSnap: DataSnapshot) {
                                    postSnap.getValue(Post::class.java)?.let { fetched.add(it) }
                                    count++
                                    if (count == likedIds.size) {
                                        userPosts.clear()
                                        userPosts.addAll(fetched.sortedByDescending { it.timestamp })
                                        postsAdapter.notifyDataSetChanged()
                                        tabLayout.getTabAt(1)?.text = "Likes (${userPosts.size})"
                                    }
                                }
                                override fun onCancelled(error: DatabaseError) {
                                    count++
                                }
                            })
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("UserProfileActivity", "Failed to load likes: ${error.message}")
                }
            })
    }

    private fun loadUserReposts() {
        if (targetUserId.isBlank()) return
        database.getReference("posts")
            .orderByChild("userId")
            .equalTo(targetUserId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    userPosts.clear()
                    for (child in snapshot.children) {
                        val post = child.getValue(Post::class.java) ?: continue
                        if (post.isRepost || post.originalPostId.isNotBlank()) {
                            userPosts.add(post)
                        }
                    }
                    userPosts.sortByDescending { it.timestamp }
                    postsAdapter.notifyDataSetChanged()
                    tabLayout.getTabAt(2)?.text = "Reposts (${userPosts.size})"
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("UserProfileActivity", "Failed to load reposts: ${error.message}")
                }
            })
    }

    // ─── Message this user ────────────────────────────────────────────────────
    private fun setupMessageButton() {
        messageButton.setOnClickListener {
            if (!(isFollowing && followsBack)) {
                Toast.makeText(
                    this,
                    "Messaging unlocks after they follow you back.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val conversationId = listOf(currentUserId, targetUserId).sorted().joinToString("_")
            startActivity(
                Intent(this, ConversationActivity::class.java).apply {
                    putExtra("conversation_id", conversationId)
                    putExtra("other_user_id", targetUserId)
                    putExtra("other_user_name", targetUsername)
                    putExtra("other_user_image", targetUserImage)
                }
            )
        }
    }

    private fun loadRelationshipState() {
        if (targetUserId.isBlank() || currentUserId.isBlank()) return

        if (targetUserId == currentUserId) {
            followButton.visibility = android.view.View.GONE
            messageButton.visibility = android.view.View.GONE
            connectionStatusText.text = ""
            return
        }

        FollowManager.checkRelationship(currentUserId, targetUserId) { following, followedBack ->
            isFollowing = following
            followsBack = followedBack
            updateActionButtons()
        }
    }

    private fun updateActionButtons() {
        followButton.text = if (isFollowing) "Following" else "Follow"
        connectionStatusText.text = when {
            !isFollowing -> "Follow to request messaging."
            followsBack -> "You follow each other. Messaging unlocked."
            else -> "Waiting for follow back to unlock messaging."
        }
        messageButton.isEnabled = isFollowing && followsBack
        messageButton.alpha = if (messageButton.isEnabled) 1f else 0.5f
    }

    private fun setupFollowButton() {
        followButton.setOnClickListener {
            val nextState = !isFollowing
            FollowManager.setFollowStatus(
                currentUserId = currentUserId,
                targetUserId = targetUserId,
                follow = nextState
            ) { success, error ->
                if (!success) {
                    Toast.makeText(
                        this,
                        error ?: "Could not update follow status.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setFollowStatus
                }

                isFollowing = nextState
                updateActionButtons()
                loadRelationshipState()
                loadUserProfile()
            }
        }
    }

    private fun openFollowList(initialTab: String) {
        if (targetUserId.isBlank()) return
        startActivity(Intent(this, FollowListActivity::class.java).apply {
            putExtra("user_id", targetUserId)
            putExtra("username", targetUsername)
            putExtra("initial_tab", initialTab)
        })
    }
}
