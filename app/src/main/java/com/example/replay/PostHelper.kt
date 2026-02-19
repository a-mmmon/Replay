package com.example.replay

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

object PostHelper {

<<<<<<< HEAD
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()

    // ─── Save post to Firebase ────────────────────────────────────────────────
=======
    /**
     * Save a post to SharedPreferences
     */
>>>>>>> 7866de0 (Updated post creation, feed adapter, and profile logic)
    fun savePost(context: Context, caption: String, music: ITunesSong?) {
        val userId = auth.currentUser?.uid ?: run {
            Log.e("PostHelper", "User not logged in")
            return
        }

<<<<<<< HEAD
        // Get username from Firebase
        database.getReference("users").child(userId).get()
            .addOnSuccessListener { snapshot ->
                val profile = snapshot.getValue(UserProfile::class.java)
                val username = profile?.username ?: auth.currentUser?.email?.substringBefore("@") ?: "User"
=======
            val postId = "post_${System.currentTimeMillis()}"
            val timestamp = System.currentTimeMillis()

            // Save post data
            editor.putString("post_${postCount}_id", postId)
            editor.putString("post_${postCount}_caption", caption)
            editor.putLong("post_${postCount}_timestamp", timestamp)
            editor.putInt("post_${postCount}_likes", 0)
            editor.putInt("post_${postCount}_comments", 0)
            editor.putInt("post_${postCount}_reposts", 0)
            editor.putBoolean("post_${postCount}_isRepost", false)
            editor.putString("post_${postCount}_originalPostId", "")
            editor.putString("post_${postCount}_repostedByUsername", "")
>>>>>>> 7866de0 (Updated post creation, feed adapter, and profile logic)

                val postId = "post_${System.currentTimeMillis()}"
                val post = Post(
                    postId = postId,
                    userId = userId,
                    username = username,
                    userProfileImage = profile?.profileImage ?: "",
                    caption = caption,
                    imageUrl = "",
                    likes = 0,
                    comments = 0,
                    timestamp = System.currentTimeMillis(),
                    music = music
                )

                // Save to Firebase under posts/{postId}
                database.getReference("posts").child(postId)
                    .setValue(post)
                    .addOnSuccessListener {
                        Log.d("PostHelper", "Post saved to Firebase: $postId")
                    }
                    .addOnFailureListener { e ->
                        Log.e("PostHelper", "Failed to save post: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                Log.e("PostHelper", "Failed to get user profile: ${e.message}")
            }
    }

    // ─── Toggle like on a post ────────────────────────────────────────────────
    fun toggleLike(postId: String, onComplete: (isLiked: Boolean, newCount: Int) -> Unit) {
        val userId = auth.currentUser?.uid ?: return

<<<<<<< HEAD
        val likeRef = database.getReference("likes").child(postId).child(userId)
        val postRef = database.getReference("posts").child(postId)

        likeRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                // Already liked — unlike it
                likeRef.removeValue()
                postRef.child("likes").get().addOnSuccessListener { likesSnapshot ->
                    val currentLikes = (likesSnapshot.getValue(Int::class.java) ?: 1) - 1
                    val newLikes = maxOf(0, currentLikes)
                    postRef.child("likes").setValue(newLikes)

                    // Remove from user's liked posts
                    database.getReference("userLikes").child(userId).child(postId).removeValue()

                    onComplete(false, newLikes)
                }
            } else {
                // Not liked — like it
                likeRef.setValue(true)
                postRef.child("likes").get().addOnSuccessListener { likesSnapshot ->
                    val newLikes = (likesSnapshot.getValue(Int::class.java) ?: 0) + 1
                    postRef.child("likes").setValue(newLikes)

                    // Save to user's liked posts
                    database.getReference("userLikes").child(userId).child(postId).setValue(true)

                    onComplete(true, newLikes)
                }
            }
        }
    }

    // ─── Check if post is liked by current user ───────────────────────────────
    fun isPostLiked(postId: String, onResult: (Boolean) -> Unit) {
        val userId = auth.currentUser?.uid ?: run { onResult(false); return }
        database.getReference("likes").child(postId).child(userId).get()
            .addOnSuccessListener { snapshot -> onResult(snapshot.exists()) }
            .addOnFailureListener { onResult(false) }
    }

    // ─── Load posts (kept for backward compat) ────────────────────────────────
    fun loadPosts(context: Context, username: String): List<Post> {
        return emptyList() // Now handled by HomeFragment via Firebase
=======
            Log.d("PostHelper", "Saved post $postCount with music: ${music != null}")

        } catch (e: Exception) {
            Log.e("PostHelper", "Error saving post", e)
        }
    }

    /**
     * Load all posts
     */
    fun loadPosts(context: Context, username: String): List<Post> {
        val posts = mutableListOf<Post>()

        try {
            val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            val postCount = prefs.getInt("post_count", 0)

            for (i in 0 until postCount) {
                val postId = prefs.getString("post_${i}_id", null)
                val caption = prefs.getString("post_${i}_caption", null)
                val timestamp = prefs.getLong("post_${i}_timestamp", 0L)
                val likes = prefs.getInt("post_${i}_likes", 0)
                val comments = prefs.getInt("post_${i}_comments", 0)
                val reposts = prefs.getInt("post_${i}_reposts", 0)
                val isRepost = prefs.getBoolean("post_${i}_isRepost", false)
                val originalPostId = prefs.getString("post_${i}_originalPostId", "") ?: ""
                val repostedByUsername = prefs.getString("post_${i}_repostedByUsername", "") ?: ""

                // Load music if exists
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
                    posts.add(
                        Post(
                            postId = postId,
                            userId = "current_user",
                            username = username,
                            caption = caption,
                            likes = likes,
                            comments = comments,
                            reposts = reposts,
                            timestamp = timestamp,
                            music = music,
                            isRepost = isRepost,
                            originalPostId = originalPostId,
                            repostedByUsername = repostedByUsername
                        )
                    )

                }
            }

            Log.d("PostHelper", "Loaded ${posts.size} posts")

        } catch (e: Exception) {
            Log.e("PostHelper", "Error loading posts", e)
        }

        return posts
>>>>>>> 7866de0 (Updated post creation, feed adapter, and profile logic)
    }
}
