package com.example.replay

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FollowListActivity : BaseThemedActivity() {

    private lateinit var backButton: ImageButton
    private lateinit var titleText: TextView
    private lateinit var emptyText: TextView
    private lateinit var tabLayout: TabLayout
    private lateinit var usersRecyclerView: RecyclerView
    private lateinit var usersAdapter: UsersAdapter

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private val currentUserId get() = auth.currentUser?.uid.orEmpty()

    private var targetUserId: String = ""
    private var targetUsername: String = ""
    private var selectedTab = 0 // 0 followers, 1 following

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_follow_list)

        targetUserId = intent.getStringExtra("user_id").orEmpty()
        targetUsername = intent.getStringExtra("username").orEmpty()
        val initialTab = intent.getStringExtra("initial_tab").orEmpty()
        selectedTab = if (initialTab == "following") 1 else 0

        initializeViews()
        setupRecyclerView()
        setupTabs()
        loadCurrentTabUsers()
    }

    private fun initializeViews() {
        backButton = findViewById(R.id.backButton)
        titleText = findViewById(R.id.titleText)
        emptyText = findViewById(R.id.emptyText)
        tabLayout = findViewById(R.id.tabLayout)
        usersRecyclerView = findViewById(R.id.usersRecyclerView)

        val displayName = targetUsername.ifBlank { "Connections" }
        titleText.text = displayName
        backButton.setOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        usersAdapter = UsersAdapter(emptyList()) { user ->
            if (user.userId == currentUserId) {
                finish()
                return@UsersAdapter
            }

            startActivity(Intent(this, UserProfileActivity::class.java).apply {
                putExtra("user_id", user.userId)
                putExtra("username", user.username)
            })
        }
        usersRecyclerView.layoutManager = LinearLayoutManager(this)
        usersRecyclerView.adapter = usersAdapter
    }

    private fun setupTabs() {
        tabLayout.removeAllTabs()
        tabLayout.addTab(tabLayout.newTab().setText("Followers"), selectedTab == 0)
        tabLayout.addTab(tabLayout.newTab().setText("Following"), selectedTab == 1)
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                selectedTab = tab?.position ?: 0
                loadCurrentTabUsers()
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) = Unit
            override fun onTabReselected(tab: TabLayout.Tab?) = Unit
        })
    }

    private fun loadCurrentTabUsers() {
        if (targetUserId.isBlank()) return
        val branch = if (selectedTab == 0) "followers" else "following"

        database.getReference("userFollows")
            .child(targetUserId)
            .child(branch)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userIds = snapshot.children.mapNotNull { it.key }
                    if (userIds.isEmpty()) {
                        usersAdapter.updateUsers(emptyList())
                        emptyText.visibility = android.view.View.VISIBLE
                        emptyText.text = if (selectedTab == 0) {
                            "No followers yet."
                        } else {
                            "Not following anyone yet."
                        }
                        return
                    }

                    emptyText.visibility = android.view.View.GONE
                    loadProfilesForIds(userIds)
                }

                override fun onCancelled(error: DatabaseError) {
                    usersAdapter.updateUsers(emptyList())
                    emptyText.visibility = android.view.View.VISIBLE
                    emptyText.text = "Could not load users."
                }
            })
    }

    private fun loadProfilesForIds(userIds: List<String>) {
        val users = mutableListOf<UserProfile>()
        var loaded = 0

        userIds.forEach { userId ->
            database.getReference("users")
                .child(userId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        snapshot.getValue(UserProfile::class.java)?.let { users.add(it) }
                        loaded++
                        if (loaded == userIds.size) {
                            val sorted = users.sortedBy { it.username.lowercase() }
                            usersAdapter.updateUsers(sorted)
                            emptyText.visibility = if (sorted.isEmpty()) {
                                android.view.View.VISIBLE
                            } else {
                                android.view.View.GONE
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        loaded++
                        if (loaded == userIds.size) {
                            usersAdapter.updateUsers(users.sortedBy { it.username.lowercase() })
                        }
                    }
                })
        }
    }
}
