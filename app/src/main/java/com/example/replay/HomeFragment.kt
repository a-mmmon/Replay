package com.example.replay

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var feedAdapter: FeedAdapter
    private val posts = mutableListOf<Post>()

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.rvFeed)

        feedAdapter = FeedAdapter(
            posts = posts,
            onPostClick = { post ->
                Log.d("HomeFragment", "Post clicked: ${post.postId}")
            },
            // ✅ NEW: clicking a username opens their profile
            onUsernameClick = { post ->
                openUserProfile(post.userId, post.username)
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = feedAdapter

        loadPostsFromFirebase()
    }

    override fun onResume() {
        super.onResume()
        loadPostsFromFirebase()
    }

    private fun loadPostsFromFirebase() {
        database.getReference("posts")
            .orderByChild("timestamp")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    posts.clear()
                    val allPosts = mutableListOf<Post>()
                    for (child in snapshot.children) {
                        val post = child.getValue(Post::class.java)
                        post?.let { allPosts.add(it) }
                    }
                    posts.addAll(allPosts.reversed())

                    if (posts.isEmpty()) {
                        posts.addAll(SampleData.samplePosts)
                    }

                    feedAdapter.notifyDataSetChanged()
                    Log.d("HomeFragment", "Loaded ${posts.size} posts")
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("HomeFragment", "Failed to load posts: ${error.message}")
                    posts.clear()
                    posts.addAll(SampleData.samplePosts)
                    feedAdapter.notifyDataSetChanged()
                }
            })
    }

    // ✅ Open another user's profile
    private fun openUserProfile(userId: String, username: String) {
        val currentUserId = auth.currentUser?.uid ?: ""

        // Don't open profile viewer for own posts — go to Profile tab instead
        if (userId == currentUserId || userId == "current_user") {
            (activity as? MainActivity)?.navigateToProfile()
            return
        }

        val intent = Intent(requireContext(), UserProfileActivity::class.java)
        intent.putExtra("user_id", userId)
        intent.putExtra("username", username)
        startActivity(intent)
    }
}