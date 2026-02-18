package com.example.replay

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfileFragment : Fragment() {

    private var profileImage: ShapeableImageView? = null
    private var usernameText: TextView? = null
    private var handleText: TextView? = null
    private var followersCount: TextView? = null
    private var followingCount: TextView? = null
    private var streakCount: TextView? = null
    private var postsRecyclerView: RecyclerView? = null
    private var tabLayout: TabLayout? = null
    private var settingsButton: ImageButton? = null

    private var postsAdapter: FeedAdapter? = null
    private val userPosts = mutableListOf<Post>()

    // Firebase
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return try {
            inflater.inflate(R.layout.fragment_profile, container, false)
        } catch (e: Exception) {
            Log.e("ProfileFragment", "Error inflating layout", e)
            null
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            initializeViews(view)
            loadUserProfileFromFirebase()
            setupRecyclerView()
            setupTabs()
            setupSettingsButton()
        } catch (e: Exception) {
            Log.e("ProfileFragment", "Error in onViewCreated", e)
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            loadUserPosts()
        } catch (e: Exception) {
            Log.e("ProfileFragment", "Error in onResume", e)
        }
    }

    private fun initializeViews(view: View) {
        profileImage = view.findViewById(R.id.profileImage)
        usernameText = view.findViewById(R.id.usernameText)
        handleText = view.findViewById(R.id.handleText)
        followersCount = view.findViewById(R.id.followersCount)
        followingCount = view.findViewById(R.id.followingCount)
        streakCount = view.findViewById(R.id.streakCount)
        postsRecyclerView = view.findViewById(R.id.postsRecyclerView)
        tabLayout = view.findViewById(R.id.tabLayout)
        settingsButton = view.findViewById(R.id.settingsButton)
    }

    private fun loadUserProfileFromFirebase() {
        val userId = auth.currentUser?.uid ?: return

        database.getReference("users").child(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!isAdded) return

                    val profile = snapshot.getValue(UserProfile::class.java)
                    if (profile != null) {
                        usernameText?.text = profile.username.ifEmpty { "User" }

                        // Handle might be missing, generate from username or email
                        val handle = if (profile.handle.isNotEmpty()) {
                            profile.handle
                        } else {
                            "@${profile.username.ifEmpty { auth.currentUser?.email?.substringBefore("@") ?: "user" }}"
                        }
                        handleText?.text = handle

                        followersCount?.text = profile.followers.toString()
                        followingCount?.text = profile.following.toString()
                        Log.d("ProfileFragment", "Profile loaded from Firebase: ${profile.username}")
                    } else {
                        // No profile in DB yet — create one
                        createUserProfile(userId)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ProfileFragment", "Failed to load profile: ${error.message}")
                }
            })
    }

    private fun createUserProfile(userId: String) {
        val email = auth.currentUser?.email ?: "user@email.com"
        val username = email.substringBefore("@")

        val newProfile = UserProfile(
            userId = userId,
            username = username,
            handle = "@$username",
            email = email,
            bio = "",
            profileImage = "",
            followers = 0,
            following = 0
        )

        database.getReference("users").child(userId).setValue(newProfile)
            .addOnSuccessListener {
                Log.d("ProfileFragment", "User profile created: $username")
                usernameText?.text = username
                handleText?.text = "@$username"
            }
            .addOnFailureListener { e ->
                Log.e("ProfileFragment", "Failed to create profile", e)
                usernameText?.text = username
                handleText?.text = "@$username"
            }
    }

    private fun setupRecyclerView() {
        val recyclerView = postsRecyclerView ?: return

        // ✅ FIXED: Provide the onPostClick callback with named parameter
        postsAdapter = FeedAdapter(userPosts, onPostClick = { post ->
            Log.d("ProfileFragment", "Post clicked: ${post.postId}")
        })

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = postsAdapter
        loadUserPosts()
    }

    private fun setupTabs() {
        val tabs = tabLayout ?: return
        tabs.removeAllTabs()
        tabs.addTab(tabs.newTab().setText("Posts"))
        tabs.addTab(tabs.newTab().setText("Likes"))

        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> loadUserPosts()
                    1 -> loadUserLikes()
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupSettingsButton() {
        settingsButton?.setOnClickListener {
            showSettingsMenu()
        }
    }

    private fun showSettingsMenu() {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Settings")
            .setItems(arrayOf("Edit Profile", "Logout")) { _, which ->
                when (which) {
                    0 -> {
                        startActivity(Intent(requireContext(), EditProfileActivity::class.java))
                    }
                    1 -> performLogout()
                }
            }
            .show()
    }

    private fun performLogout() {
        auth.signOut()
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
        Log.d("ProfileFragment", "Logged out via Firebase")
    }

    private fun loadUserPosts() {
        val userId = auth.currentUser?.uid ?: return

        database.getReference("posts")
            .orderByChild("userId")
            .equalTo(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!isAdded) return
                    userPosts.clear()

                    for (child in snapshot.children) {
                        val post = child.getValue(Post::class.java)
                        post?.let { userPosts.add(it) }
                    }

                    // Sort by timestamp (newest first)
                    userPosts.sortByDescending { it.timestamp }

                    postsAdapter?.notifyDataSetChanged()
                    tabLayout?.getTabAt(0)?.text = "Posts (${userPosts.size})"
                    Log.d("ProfileFragment", "Loaded ${userPosts.size} posts")
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ProfileFragment", "Failed to load posts: ${error.message}")
                }
            })
    }

    private fun loadUserLikes() {
        userPosts.clear()
        // TODO: Load actual liked posts from Firebase
        postsAdapter?.notifyDataSetChanged()
        tabLayout?.getTabAt(1)?.text = "Likes (0)"
    }
}