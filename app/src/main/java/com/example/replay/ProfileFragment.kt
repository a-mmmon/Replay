package com.example.replay

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ProfileFragment : Fragment() {

    private lateinit var imgProfile: ImageView
    private lateinit var postsAdapter: FeedAdapter
    private lateinit var rvUserPosts: RecyclerView
    private lateinit var btnPostsTab: LinearLayout
    private lateinit var btnLikesTab: LinearLayout
    private lateinit var tabIndicator: View
    private lateinit var tvPostsCount: TextView
    private lateinit var tvLikesCount: TextView

    private var currentTab = "posts" // "posts" or "likes"

    // 🔹 Image picker launcher
    private val imagePicker =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                imgProfile.setImageURI(it)
                saveImageUri(it)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        imgProfile = view.findViewById(R.id.imgProfile)
        val tvName = view.findViewById<TextView>(R.id.tvName)
        val tvHandle = view.findViewById<TextView>(R.id.tvHandle)
        val tvFollowers = view.findViewById<TextView>(R.id.tvFollowers)
        val tvFollowing = view.findViewById<TextView>(R.id.tvFollowing)
        val tvStreak = view.findViewById<TextView>(R.id.tvStreak)
        val btnLogout = view.findViewById<Button>(R.id.btnLogout)
        rvUserPosts = view.findViewById(R.id.rvUserPosts)

        // Tab buttons
        btnPostsTab = view.findViewById(R.id.btnPostsTab)
        btnLikesTab = view.findViewById(R.id.btnLikesTab)
        tabIndicator = view.findViewById(R.id.tabIndicator)
        tvPostsCount = view.findViewById(R.id.tvPostsCount)
        tvLikesCount = view.findViewById(R.id.tvLikesCount)

        // Get user profile data
        val userProfile = SampleData.currentUserProfile

        // Set user info
        tvName.text = userProfile.userName
        tvHandle.text = userProfile.userHandle
        tvFollowers.text = userProfile.followersCount.toString()
        tvFollowing.text = userProfile.followingCount.toString()
        tvStreak.text = userProfile.streakCount.toString()

        // Update counts
        updateCounts()

        loadSavedImage()

        // Setup user posts RecyclerView
        postsAdapter = FeedAdapter(userProfile.userPosts) { post ->
            // Optional: handle post click
        }
        rvUserPosts.layoutManager = LinearLayoutManager(requireContext())
        rvUserPosts.adapter = postsAdapter

        // Set Posts tab as default selected
        selectPostsTab()

        // Posts tab click
        btnPostsTab.setOnClickListener {
            selectPostsTab()
        }

        // Likes tab click
        btnLikesTab.setOnClickListener {
            selectLikesTab()
        }

        // 📸 Tap image to change
        imgProfile.setOnClickListener {
            imagePicker.launch("image/*")
        }

        btnLogout.setOnClickListener {
            Toast.makeText(requireContext(), "Logged out", Toast.LENGTH_SHORT).show()
        }
    }

    private fun selectPostsTab() {
        currentTab = "posts"

        // Show user's posts
        val userProfile = SampleData.currentUserProfile
        postsAdapter = FeedAdapter(userProfile.userPosts) { post ->
            // Optional: handle post click
        }
        rvUserPosts.adapter = postsAdapter

        // Update tab indicator position (move to left half)
        val params = tabIndicator.layoutParams as ViewGroup.MarginLayoutParams
        params.width = btnPostsTab.width
        params.marginStart = 0
        tabIndicator.layoutParams = params
    }

    private fun selectLikesTab() {
        currentTab = "likes"

        // Show liked posts (filter posts with likes > 0 or create a separate liked posts list)
        // For now, showing all posts from feed that user might have liked
        val likedPosts = SampleData.posts.filter { it.likesCount > 0 }
        postsAdapter = FeedAdapter(likedPosts) { post ->
            // Optional: handle post click
        }
        rvUserPosts.adapter = postsAdapter

        // Update tab indicator position (move to right half)
        val params = tabIndicator.layoutParams as ViewGroup.MarginLayoutParams
        params.width = btnLikesTab.width
        params.marginStart = btnPostsTab.width
        tabIndicator.layoutParams = params
    }

    private fun updateCounts() {
        val userProfile = SampleData.currentUserProfile
        tvPostsCount.text = " (${userProfile.userPosts.size})"

        // Count liked posts (you can customize this logic)
        val likedCount = SampleData.posts.count { it.likesCount > 0 }
        tvLikesCount.text = " ($likedCount)"
    }

    // 💾 Save image URI
    private fun saveImageUri(uri: Uri) {
        val prefs = requireContext()
            .getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)

        prefs.edit()
            .putString("profile_image", uri.toString())
            .apply()
    }

    // 🔁 Load saved image
    private fun loadSavedImage() {
        val prefs = requireContext()
            .getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)

        val uriString = prefs.getString("profile_image", null)
        uriString?.let {
            imgProfile.setImageURI(Uri.parse(it))
        }
    }

    // Call this when fragment resumes to refresh posts
    override fun onResume() {
        super.onResume()
        updateCounts()

        // Refresh current tab
        if (currentTab == "posts") {
            selectPostsTab()
        } else {
            selectLikesTab()
        }
    }
}