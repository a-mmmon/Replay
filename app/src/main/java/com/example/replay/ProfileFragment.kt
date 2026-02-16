package com.example.replay

import android.content.Context
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
            loadUserProfile()
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
        try {
            profileImage = view.findViewById(R.id.profileImage)
            usernameText = view.findViewById(R.id.usernameText)
            handleText = view.findViewById(R.id.handleText)
            followersCount = view.findViewById(R.id.followersCount)
            followingCount = view.findViewById(R.id.followingCount)
            streakCount = view.findViewById(R.id.streakCount)
            postsRecyclerView = view.findViewById(R.id.postsRecyclerView)
            tabLayout = view.findViewById(R.id.tabLayout)
            settingsButton = view.findViewById(R.id.settingsButton)

            Log.d("ProfileFragment", "All views initialized successfully")
        } catch (e: Exception) {
            Log.e("ProfileFragment", "Error finding views", e)
        }
    }

    private fun loadUserProfile() {
        try {
            val prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            val currentUsername = prefs.getString("username", "uri") ?: "uri"
            val currentHandle = prefs.getString("user_handle", "@$currentUsername") ?: "@$currentUsername"

            usernameText?.text = currentUsername
            handleText?.text = currentHandle

            followersCount?.text = prefs.getInt("followers", 245).toString()
            followingCount?.text = prefs.getInt("following", 189).toString()
            streakCount?.text = prefs.getInt("streak", 5).toString()

            Log.d("ProfileFragment", "User profile loaded: $currentUsername")
        } catch (e: Exception) {
            Log.e("ProfileFragment", "Error loading user profile", e)
        }
    }

    private fun setupRecyclerView() {
        try {
            val recyclerView = postsRecyclerView ?: return

            postsAdapter = FeedAdapter(userPosts) { post ->
                Log.d("ProfileFragment", "Post clicked: ${post.postId}")
            }
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.adapter = postsAdapter

            loadUserPosts()

            Log.d("ProfileFragment", "RecyclerView setup complete")
        } catch (e: Exception) {
            Log.e("ProfileFragment", "Error setting up RecyclerView", e)
        }
    }

    private fun setupTabs() {
        try {
            val tabs = tabLayout ?: return

            tabs.removeAllTabs()
            tabs.addTab(tabs.newTab().setText("Posts"))
            tabs.addTab(tabs.newTab().setText("Reposts"))  // ✅ NEW: Reposts tab
            tabs.addTab(tabs.newTab().setText("Likes"))

            tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    try {
                        when (tab?.position) {
                            0 -> {
                                Log.d("ProfileFragment", "Posts tab selected")
                                loadUserPosts()
                            }
                            1 -> {
                                Log.d("ProfileFragment", "Reposts tab selected")
                                loadUserReposts()  // ✅ NEW
                            }
                            2 -> {
                                Log.d("ProfileFragment", "Likes tab selected")
                                loadUserLikes()
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("ProfileFragment", "Error in tab selection", e)
                    }
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {}
                override fun onTabReselected(tab: TabLayout.Tab?) {}
            })

            Log.d("ProfileFragment", "Tabs setup complete")
        } catch (e: Exception) {
            Log.e("ProfileFragment", "Error setting up tabs", e)
        }
    }

    private fun setupSettingsButton() {
        try {
            settingsButton?.setOnClickListener {
                showSettingsMenu()
            }
            Log.d("ProfileFragment", "Settings button setup complete")
        } catch (e: Exception) {
            Log.e("ProfileFragment", "Error setting up settings button", e)
        }
    }

    private fun showSettingsMenu() {
        try {
            android.app.AlertDialog.Builder(requireContext())
                .setTitle("Settings")
                .setItems(arrayOf("Edit Profile", "Logout")) { _, which ->
                    when (which) {
                        0 -> {
                            Log.d("ProfileFragment", "Edit Profile clicked")
                        }
                        1 -> {
                            performLogout()
                        }
                    }
                }
                .show()
        } catch (e: Exception) {
            Log.e("ProfileFragment", "Error showing settings menu", e)
        }
    }

    private fun performLogout() {
        try {
            val prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            prefs.edit().putBoolean("is_logged_in", false).apply()

            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()

            Log.d("ProfileFragment", "Logout successful")
        } catch (e: Exception) {
            Log.e("ProfileFragment", "Error performing logout", e)
        }
    }

    private fun loadUserPosts() {
        try {
            val prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            val currentUsername = prefs.getString("username", "uri") ?: "uri"

            userPosts.clear()

            val postCount = prefs.getInt("post_count", 0)
            Log.d("ProfileFragment", "Loading $postCount posts")

            for (i in 0 until postCount) {
                val postId = prefs.getString("post_${i}_id", null)
                val caption = prefs.getString("post_${i}_caption", null)
                val timestamp = prefs.getLong("post_${i}_timestamp", 0L)
                val likes = prefs.getInt("post_${i}_likes", 0)

                val music = if (prefs.contains("post_${i}_music_trackId")) {
                    ITunesSong(
                        trackId = prefs.getLong("post_${i}_music_trackId", 0L),
                        trackName = prefs.getString("post_${i}_music_trackName", "") ?: "",
                        artistName = prefs.getString("post_${i}_music_artistName", "") ?: "",
                        artworkUrl100 = prefs.getString("post_${i}_music_artworkUrl", "") ?: "",
                        previewUrl = prefs.getString("post_${i}_music_previewUrl", "") ?: "",
                        collectionName = prefs.getString("post_${i}_music_collectionName", "") ?: "",
                        trackViewUrl = prefs.getString("post_${i}_music_trackViewUrl", "") ?: "",
                        releaseDate = prefs.getString("post_${i}_music_releaseDate", "") ?: ""
                    )
                } else {
                    null
                }

                if (postId != null && caption != null) {
                    userPosts.add(
                        Post(
                            postId = postId,
                            userId = "current_user",
                            username = currentUsername,
                            userProfileImage = "",
                            caption = caption,
                            imageUrl = "",
                            likes = likes,
                            comments = 0,
                            reposts = 0,
                            timestamp = timestamp,
                            music = music,
                            isRepost = false,
                            originalPostId = "",
                            repostedByUsername = ""
                        )
                    )
                }
            }

            if (userPosts.isEmpty()) {
                userPosts.addAll(getSampleUserPosts(currentUsername))
                Log.d("ProfileFragment", "Loaded sample posts")
            }

            postsAdapter?.notifyDataSetChanged()

            val postsTab = tabLayout?.getTabAt(0)
            postsTab?.text = "Posts (${userPosts.size})"

            Log.d("ProfileFragment", "Loaded ${userPosts.size} user posts")
        } catch (e: Exception) {
            Log.e("ProfileFragment", "Error loading user posts", e)
        }
    }

    // ✅ NEW: Load user's reposts
    private fun loadUserReposts() {
        try {
            val prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            val currentUsername = prefs.getString("username", "uri") ?: "uri"

            userPosts.clear()

            val repostCount = prefs.getInt("repost_count", 0)
            Log.d("ProfileFragment", "Loading $repostCount reposts")

            for (i in 0 until repostCount) {
                val postId = prefs.getString("repost_${i}_id", null)
                val originalPostId = prefs.getString("repost_${i}_original_post_id", null)
                val username = prefs.getString("repost_${i}_username", null)
                val caption = prefs.getString("repost_${i}_caption", null)
                val timestamp = prefs.getLong("repost_${i}_timestamp", 0L)
                val likes = prefs.getInt("repost_${i}_likes", 0)
                val comments = prefs.getInt("repost_${i}_comments", 0)

                val music = if (prefs.contains("repost_${i}_music_trackId")) {
                    ITunesSong(
                        trackId = prefs.getLong("repost_${i}_music_trackId", 0L),
                        trackName = prefs.getString("repost_${i}_music_trackName", "") ?: "",
                        artistName = prefs.getString("repost_${i}_music_artistName", "") ?: "",
                        artworkUrl100 = prefs.getString("repost_${i}_music_artworkUrl", "") ?: "",
                        previewUrl = "",
                        collectionName = "",
                        trackViewUrl = "",
                        releaseDate = ""
                    )
                } else {
                    null
                }

                if (postId != null && caption != null && username != null && originalPostId != null) {
                    userPosts.add(
                        Post(
                            postId = originalPostId,
                            userId = "other_user",
                            username = username,
                            userProfileImage = "",
                            caption = caption,
                            imageUrl = "",
                            likes = likes,
                            comments = comments,
                            reposts = 0,
                            timestamp = timestamp,
                            music = music,
                            isRepost = true,  // ✅ Mark as repost
                            originalPostId = originalPostId,
                            repostedByUsername = currentUsername  // ✅ Show who reposted
                        )
                    )
                }
            }

            postsAdapter?.notifyDataSetChanged()

            val repostsTab = tabLayout?.getTabAt(1)
            repostsTab?.text = "Reposts (${userPosts.size})"

            Log.d("ProfileFragment", "Loaded ${userPosts.size} reposts")
        } catch (e: Exception) {
            Log.e("ProfileFragment", "Error loading reposts", e)
        }
    }

    private fun loadUserLikes() {
        try {
            userPosts.clear()
            userPosts.addAll(getSampleLikedPosts())
            postsAdapter?.notifyDataSetChanged()

            Log.d("ProfileFragment", "Loaded ${userPosts.size} liked posts")
        } catch (e: Exception) {
            Log.e("ProfileFragment", "Error loading user likes", e)
        }
    }

    private fun getSampleUserPosts(username: String): List<Post> {
        return listOf(
            Post(
                postId = "sample_1",
                userId = "current_user",
                username = username,
                userProfileImage = "",
                caption = "Just shared my favorite playlist! 🎵",
                imageUrl = "",
                likes = 42,
                comments = 8,
                reposts = 0,
                timestamp = System.currentTimeMillis() - 3600000,
                music = null,
                isRepost = false,
                originalPostId = "",
                repostedByUsername = ""
            )
        )
    }

    private fun getSampleLikedPosts(): List<Post> {
        return listOf(
            Post(
                postId = "liked_1",
                userId = "user1",
                username = "Taylor Swift",
                userProfileImage = "",
                caption = "Such a fun night making music! ✨",
                imageUrl = "",
                likes = 5,
                comments = 2,
                reposts = 0,
                timestamp = System.currentTimeMillis() - 10800000,
                music = null,
                isRepost = false,
                originalPostId = "",
                repostedByUsername = ""
            ),
            Post(
                postId = "liked_2",
                userId = "user2",
                username = "BTS",
                userProfileImage = "",
                caption = "Have a wonderful concert! ✨",
                imageUrl = "",
                likes = 1000,
                comments = 150,
                reposts = 0,
                timestamp = System.currentTimeMillis() - 3600000,
                music = null,
                isRepost = false,
                originalPostId = "",
                repostedByUsername = ""
            ),
            Post(
                postId = "liked_3",
                userId = "user3",
                username = "Black Pink",
                userProfileImage = "",
                caption = "How amazing is this new album! ✨",
                imageUrl = "",
                likes = 2000,
                comments = 200,
                reposts = 0,
                timestamp = System.currentTimeMillis() - 86400000,
                music = null,
                isRepost = false,
                originalPostId = "",
                repostedByUsername = ""
            )
        )
    }
}