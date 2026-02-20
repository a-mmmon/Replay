package com.example.replay

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

object PostHelper {

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()

    // ─── Save post to Firebase ────────────────────────────────────────────────
    fun savePost(context: Context, caption: String, music: ITunesSong?) {
        val userId = auth.currentUser?.uid ?: run {
            Log.e("PostHelper", "User not logged in")
            return
        }

        // Get username from Firebase
        database.getReference("users").child(userId).get()
            .addOnSuccessListener { snapshot ->
                val profile = snapshot.getValue(UserProfile::class.java)
                val username = profile?.username ?: auth.currentUser?.email?.substringBefore("@") ?: "User"

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
    }
}