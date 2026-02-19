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

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
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
        try { loadUserPosts() } catch (e: Exception) {
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
<<<<<<< HEAD
=======

>>>>>>> 7866de0 (Updated post creation, feed adapter, and profile logic)
                    val profile = snapshot.getValue(UserProfile::class.java)
                    if (profile != null) {
                        usernameText?.text = profile.username.ifEmpty { "User" }
                        val handle = if (profile.handle.isNotEmpty()) profile.handle
                        else "@${profile.username.ifEmpty { auth.currentUser?.email?.substringBefore("@") ?: "user" }}"
                        handleText?.text = handle
                        followersCount?.text = profile.followers.toString()
                        followingCount?.text = profile.following.toString()
<<<<<<< HEAD
                    } else {
                        createUserProfile(userId)
=======
                        Log.d("ProfileFragment", "Profile loaded: ${profile.username}")
                    } else {
                        val email = auth.currentUser?.email ?: "user@email.com"
                        val fallbackName = email.substringBefore("@")
                        usernameText?.text = fallbackName
                        handleText?.text = "@$fallbackName"
>>>>>>> 7866de0 (Updated post creation, feed adapter, and profile logic)
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
            userId = userId, username = username, handle = "@$username",
            email = email, bio = "", profileImage = "", followers = 0, following = 0
        )
        database.getReference("users").child(userId).setValue(newProfile)
            .addOnSuccessListener {
                usernameText?.text = username
                handleText?.text = "@$username"
            }
    }

    private fun setupRecyclerView() {
        val recyclerView = postsRecyclerView ?: return
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
        tabs.addTab(tabs.newTab().setText("Reposts"))
        tabs.addTab(tabs.newTab().setText("Likes"))

        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> loadUserPosts()
                    1 -> loadUserReposts()
                    2 -> loadUserLikes()
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupSettingsButton() {
        settingsButton?.setOnClickListener { showSettingsMenu() }
    }

    private fun showSettingsMenu() {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Settings")
            .setItems(arrayOf("Edit Profile", "Logout")) { _, which ->
                when (which) {
                    0 -> startActivity(Intent(requireContext(), EditProfileActivity::class.java))
                    1 -> performLogout()
                }
            }.show()
    }

    private fun performLogout() {
        auth.signOut()
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
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
                        child.getValue(Post::class.java)?.let { userPosts.add(it) }
                    }
<<<<<<< HEAD
                    userPosts.sortByDescending { it.timestamp }
=======

                    if (userPosts.isEmpty()) {
                        val username = usernameText?.text?.toString() ?: "User"
                        userPosts.addAll(getSampleUserPosts(username))
                    }

>>>>>>> 7866de0 (Updated post creation, feed adapter, and profile logic)
                    postsAdapter?.notifyDataSetChanged()
                    tabLayout?.getTabAt(0)?.text = "Posts (${userPosts.size})"
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ProfileFragment", "Failed to load posts: ${error.message}")
                }
            })
    }

<<<<<<< HEAD
=======
    private fun loadUserReposts() {
        userPosts.clear()
        val userId = auth.currentUser?.uid ?: return

        database.getReference("posts")
            .orderByChild("userId")
            .equalTo(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!isAdded) return

                    for (child in snapshot.children) {
                        val post = child.getValue(Post::class.java)
                        if (post != null && post.isRepost) {
                            userPosts.add(post)
                        }
                    }

                    postsAdapter?.notifyDataSetChanged()
                    tabLayout?.getTabAt(1)?.text = "Reposts (${userPosts.size})"
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ProfileFragment", "Failed to load reposts: ${error.message}")
                }
            })
    }

>>>>>>> 7866de0 (Updated post creation, feed adapter, and profile logic)
    private fun loadUserLikes() {
        val userId = auth.currentUser?.uid ?: return

<<<<<<< HEAD
        // PostHelper writes likes to userLikes/{userId}/{postId}
        database.getReference("userLikes").child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!isAdded) return
                    val likedPostIds = snapshot.children.mapNotNull { it.key }.filter { it.isNotEmpty() }

                    if (likedPostIds.isEmpty()) {
                        userPosts.clear()
                        postsAdapter?.notifyDataSetChanged()
                        tabLayout?.getTabAt(1)?.text = "Likes (0)"
                        return
                    }

                    val fetchedPosts = mutableListOf<Post>()
                    var fetched = 0

                    for (postId in likedPostIds) {
                        database.getReference("posts").child(postId)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(postSnap: DataSnapshot) {
                                    if (!isAdded) return
                                    postSnap.getValue(Post::class.java)?.let { fetchedPosts.add(it) }
                                    fetched++
                                    if (fetched == likedPostIds.size) {
                                        userPosts.clear()
                                        userPosts.addAll(fetchedPosts.sortedByDescending { it.timestamp })
                                        postsAdapter?.notifyDataSetChanged()
                                        tabLayout?.getTabAt(1)?.text = "Likes (${userPosts.size})"
                                    }
                                }
                                override fun onCancelled(error: DatabaseError) {
                                    fetched++
                                    if (fetched == likedPostIds.size) {
                                        userPosts.clear()
                                        userPosts.addAll(fetchedPosts.sortedByDescending { it.timestamp })
                                        postsAdapter?.notifyDataSetChanged()
                                        tabLayout?.getTabAt(1)?.text = "Likes (${fetchedPosts.size})"
                                    }
                                }
                            })
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ProfileFragment", "Failed to load likes: ${error.message}")
                }
            })
=======
    private fun getSampleUserPosts(username: String): List<Post> {
        return listOf(
            Post(
                postId = "sample_1",
                userId = auth.currentUser?.uid ?: "current_user",
                username = username,
                caption = "Just shared my favorite playlist! 🎵",
                likes = 42,
                comments = 8,
                timestamp = System.currentTimeMillis() - 3600000,
                music = null,
                isRepost = false
            )
        )
    }

    private fun getSampleLikedPosts(): List<Post> {
        return listOf(
            Post(
                postId = "liked_1",
                userId = "user1",
                username = "Taylor Swift",
                caption = "Such a fun night making music! ✨",
                likes = 5,
                comments = 2,
                timestamp = System.currentTimeMillis() - 10800000,
                music = null,
                isRepost = false
            ),
            Post(
                postId = "liked_2",
                userId = "user2",
                username = "BTS",
                caption = "Have a wonderful concert! ✨",
                likes = 1000,
                comments = 150,
                timestamp = System.currentTimeMillis() - 3600000,
                music = null,
                isRepost = false
            ),
            Post(
                postId = "liked_3",
                userId = "user3",
                username = "Black Pink",
                caption = "How amazing is this new album! ✨",
                likes = 2000,
                comments = 200,
                timestamp = System.currentTimeMillis() - 86400000,
                music = null,
                isRepost = false
            )
        )
>>>>>>> 7866de0 (Updated post creation, feed adapter, and profile logic)
    }
}