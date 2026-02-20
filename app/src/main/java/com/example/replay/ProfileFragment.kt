package com.example.replay

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage

class ProfileFragment : Fragment() {

    private var profileImage: ShapeableImageView? = null
    private var editPhotoButton: MaterialButton? = null
    private var usernameText: TextView? = null
    private var handleText: TextView? = null
    private var bioText: TextView? = null
    private var followersCount: TextView? = null
    private var followingCount: TextView? = null
    private var followersSection: View? = null
    private var followingSection: View? = null
    private var streakCount: TextView? = null
    private var postsRecyclerView: RecyclerView? = null
    private var tabLayout: TabLayout? = null
    private var settingsButton: ImageButton? = null

    private var postsAdapter: FeedAdapter? = null
    private val userPosts = mutableListOf<Post>()

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()

    private val storage = FirebaseStorage.getInstance()

    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { uploadProfileImage(it) }
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

        initializeViews(view)
        setupRecyclerView()
        setupTabs()
        setupSettingsButton()

        loadUserProfileFromFirebase()
        loadUserPosts()
    }

    override fun onResume() {
        super.onResume()
        loadUserPosts()
    }

    // ----------------------------
    // View Initialization
    // ----------------------------
    private fun initializeViews(view: View) {
        profileImage = view.findViewById(R.id.profileImage)
        editPhotoButton = view.findViewById(R.id.editPhotoButton)
        usernameText = view.findViewById(R.id.usernameText)
        handleText = view.findViewById(R.id.handleText)
        bioText = view.findViewById(R.id.bioText)
        followersCount = view.findViewById(R.id.followersCount)
        followingCount = view.findViewById(R.id.followingCount)
        followersSection = view.findViewById(R.id.followersSection)
        followingSection = view.findViewById(R.id.followingSection)
        streakCount = view.findViewById(R.id.streakCount)
        postsRecyclerView = view.findViewById(R.id.postsRecyclerView)
        tabLayout = view.findViewById(R.id.tabLayout)
        settingsButton = view.findViewById(R.id.settingsButton)

        profileImage?.setImageResource(ThemeManager.getDefaultAvatarRes(requireContext()))
        profileImage?.setOnClickListener { openImagePicker() }
        editPhotoButton?.setOnClickListener { openImagePicker() }
        followersSection?.setOnClickListener { openFollowList("followers") }
        followingSection?.setOnClickListener { openFollowList("following") }
    }

    private fun openFollowList(initialTab: String) {
        val user = auth.currentUser ?: return
        startActivity(Intent(requireContext(), FollowListActivity::class.java).apply {
            putExtra("user_id", user.uid)
            putExtra("username", usernameText?.text?.toString().orEmpty())
            putExtra("initial_tab", initialTab)
        })
    }

    // ----------------------------
    // Profile Data
    // ----------------------------
    private fun loadUserProfileFromFirebase() {
        val userId = auth.currentUser?.uid ?: return

        database.getReference("users").child(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!isAdded) return

                    val profile = snapshot.getValue(UserProfile::class.java)
                    profile?.let {
                        usernameText?.text = it.username
                        handleText?.text = it.handle
                        bioText?.text = if (it.bio.isBlank()) {
                            "Share your vibe with a short bio."
                        } else {
                            it.bio
                        }
                        followersCount?.text = it.followers.toString()
                        followingCount?.text = it.following.toString()
                        loadProfileImage(it.profileImage)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ProfileFragment", error.message)
                }
            })
    }

    // ----------------------------
    // RecyclerView Setup
    // ----------------------------
    private fun setupRecyclerView() {
        postsAdapter = FeedAdapter(userPosts) { post ->
            Log.d("ProfileFragment", "Clicked: ${post.postId}")
        }

        postsRecyclerView?.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = postsAdapter
        }
    }

    // ----------------------------
    // Tabs
    // ----------------------------
    private fun setupTabs() {
        tabLayout?.apply {
            removeAllTabs()
            addTab(newTab().setText("Posts"))
            addTab(newTab().setText("Likes"))
            addTab(newTab().setText("Reposts"))

            addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    when (tab?.position) {
                        0 -> loadUserPosts()
                        1 -> loadUserLikes()
                        2 -> loadUserReposts()
                    }
                }
                override fun onTabUnselected(tab: TabLayout.Tab?) {}
                override fun onTabReselected(tab: TabLayout.Tab?) {}
            })
        }
    }

    private fun openImagePicker() {
        imagePickerLauncher.launch("image/*")
    }

    private fun uploadProfileImage(imageUri: Uri) {
        val userId = auth.currentUser?.uid ?: return
        val imageRef = storage.reference.child("profileImages/$userId.jpg")

        imageRef.putFile(imageUri)
            .continueWithTask { task ->
                if (!task.isSuccessful) {
                    throw task.exception ?: Exception("Upload failed")
                }
                imageRef.downloadUrl
            }
            .addOnSuccessListener { downloadUri ->
                database.getReference("users").child(userId).child("profileImage")
                    .setValue(downloadUri.toString())
                    .addOnSuccessListener {
                        if (!isAdded) return@addOnSuccessListener
                        loadProfileImage(downloadUri.toString())
                        Toast.makeText(
                            requireContext(),
                            "Profile photo updated",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .addOnFailureListener { error ->
                        if (!isAdded) return@addOnFailureListener
                        Toast.makeText(
                            requireContext(),
                            "Failed to save photo: ${error.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
            }
            .addOnFailureListener { error ->
                if (!isAdded) return@addOnFailureListener
                Toast.makeText(
                    requireContext(),
                    "Failed to upload photo: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun loadProfileImage(url: String?) {
        if (!isAdded) return

        if (url.isNullOrBlank()) {
            profileImage?.setImageResource(ThemeManager.getDefaultAvatarRes(requireContext()))
            return
        }

        Glide.with(this)
            .load(url)
            .placeholder(ThemeManager.getDefaultAvatarRes(requireContext()))
            .error(ThemeManager.getDefaultAvatarRes(requireContext()))
            .into(profileImage ?: return)
    }

    // ----------------------------
    // Load User Posts
    // ----------------------------
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
                        child.getValue(Post::class.java)?.let {
                            if (!it.isRepost && it.originalPostId.isBlank()) {
                                userPosts.add(it)
                            }
                        }
                    }

                    userPosts.sortByDescending { it.timestamp }

                    // 🔥 Calculate Streak
                    val streak = calculateStreak(userPosts)
                    streakCount?.text = streak.toString()

                    postsAdapter?.notifyDataSetChanged()
                    tabLayout?.getTabAt(0)?.text = "Posts (${userPosts.size})"
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ProfileFragment", error.message)
                }
            })
    }

    private fun loadUserReposts() {
        val userId = auth.currentUser?.uid ?: return
        database.getReference("posts")
            .orderByChild("userId")
            .equalTo(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!isAdded) return

                    val firebaseReposts = mutableListOf<Post>()
                    for (child in snapshot.children) {
                        val post = child.getValue(Post::class.java) ?: continue
                        if (post.isRepost || post.originalPostId.isNotBlank()) {
                            firebaseReposts.add(post)
                        }
                    }

                    // Keep compatibility with older local repost storage.
                    val localReposts = loadLocalRepostsFromPrefs()

                    val merged = (firebaseReposts + localReposts)
                        .distinctBy { it.postId }
                        .sortedByDescending { it.timestamp }

                    userPosts.clear()
                    userPosts.addAll(merged)
                    postsAdapter?.notifyDataSetChanged()
                    tabLayout?.getTabAt(2)?.text = "Reposts (${userPosts.size})"
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ProfileFragment", error.message)
                }
            })
    }

    private fun loadLocalRepostsFromPrefs(): List<Post> {
        val context = context ?: return emptyList()
        val prefs = context.getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
        val count = prefs.getInt("repost_count", 0)
        val reposts = mutableListOf<Post>()

        for (i in 0 until count) {
            val postId = prefs.getString("repost_${i}_original_post_id", "").orEmpty()
            if (postId.isBlank()) continue

            val caption = prefs.getString("repost_${i}_caption", "").orEmpty()
            val username = prefs.getString("repost_${i}_username", "").orEmpty()
            val timestamp = prefs.getLong("repost_${i}_timestamp", 0L)
            val likes = prefs.getInt("repost_${i}_likes", 0)
            val comments = prefs.getInt("repost_${i}_comments", 0)
            val repostedBy = prefs.getString("repost_${i}_reposted_by", "").orEmpty()

            reposts.add(
                Post(
                    postId = postId,
                    userId = auth.currentUser?.uid.orEmpty(),
                    username = username,
                    caption = caption,
                    likes = likes,
                    comments = comments,
                    timestamp = timestamp,
                    isRepost = true,
                    originalPostId = postId,
                    repostedByUsername = repostedBy
                )
            )
        }

        return reposts
    }

    // ----------------------------
    // Load User Likes
    // ----------------------------
    private fun loadUserLikes() {
        val userId = auth.currentUser?.uid ?: return

        database.getReference("userLikes").child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!isAdded) return

                    val likedIds = snapshot.children.mapNotNull { it.key }

                    if (likedIds.isEmpty()) {
                        userPosts.clear()
                        postsAdapter?.notifyDataSetChanged()
                        tabLayout?.getTabAt(1)?.text = "Likes (0)"
                        return
                    }

                    val fetched = mutableListOf<Post>()
                    var count = 0

                    for (id in likedIds) {
                        database.getReference("posts").child(id)
                            .addListenerForSingleValueEvent(object : ValueEventListener {

                                override fun onDataChange(postSnap: DataSnapshot) {
                                    postSnap.getValue(Post::class.java)?.let {
                                        fetched.add(it)
                                    }

                                    count++
                                    if (count == likedIds.size) {
                                        userPosts.clear()
                                        userPosts.addAll(
                                            fetched.sortedByDescending { it.timestamp }
                                        )
                                        postsAdapter?.notifyDataSetChanged()
                                        tabLayout?.getTabAt(1)?.text =
                                            "Likes (${userPosts.size})"
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    count++
                                }
                            })
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ProfileFragment", error.message)
                }
            })
    }

    // ----------------------------
    // 🔥 STREAK LOGIC
    // ----------------------------
    private fun calculateStreak(posts: List<Post>): Int {
        if (posts.isEmpty()) return 0

        val oneDay = 24 * 60 * 60 * 1000L
        val today = System.currentTimeMillis() / oneDay

        val uniqueDays = posts
            .map { it.timestamp / oneDay }
            .toSet()

        var streak = 0

        while (uniqueDays.contains(today - streak)) {
            streak++
        }

        return streak
    }

    // ----------------------------
    // Settings
    // ----------------------------
    private fun setupSettingsButton() {
        settingsButton?.setOnClickListener { showSettingsMenu() }
    }

    private fun showSettingsMenu() {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Settings")
            .setItems(arrayOf("Edit Profile", "Change Theme", "Logout")) { _, which ->
                when (which) {
                    0 -> startActivity(
                        Intent(requireContext(), EditProfileActivity::class.java)
                    )
                    1 -> showThemePicker()
                    2 -> performLogout()
                }
            }
            .show()
    }

    private fun showThemePicker() {
        val themeLabels = arrayOf("Default", "Ocean", "Sunset", "Royal")
        val themeKeys = arrayOf(
            ThemeManager.THEME_DEFAULT,
            ThemeManager.THEME_OCEAN,
            ThemeManager.THEME_SUNSET,
            ThemeManager.THEME_ROYAL
        )

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Choose Theme")
            .setSingleChoiceItems(
                themeLabels,
                ThemeManager.getThemeIndex(requireContext())
            ) { dialog, which ->
                val changed = ThemeManager.saveTheme(requireContext(), themeKeys[which])
                dialog.dismiss()

                if (changed) {
                    requireActivity().recreate()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performLogout() {
        auth.signOut()
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }
}
