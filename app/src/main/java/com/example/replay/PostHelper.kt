package com.example.replay

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

/**
 * Helper object to save and load posts with music data
 */
object PostHelper {

    /**
     * Save a post to SharedPreferences (call this when creating a new post)
     */
    fun savePost(context: Context, caption: String, music: ITunesSong?) {
        try {
            val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            val postCount = prefs.getInt("post_count", 0)
            val editor = prefs.edit()

            // Save post data
            editor.putString("post_${postCount}_id", "post_${System.currentTimeMillis()}")
            editor.putString("post_${postCount}_caption", caption)
            editor.putLong("post_${postCount}_timestamp", System.currentTimeMillis())
            editor.putInt("post_${postCount}_likes", 0)

            // Save music data if present
            if (music != null) {
                editor.putLong("post_${postCount}_music_trackId", music.trackId)
                editor.putString("post_${postCount}_music_trackName", music.trackName)
                editor.putString("post_${postCount}_music_artistName", music.artistName)
                editor.putString("post_${postCount}_music_artworkUrl", music.artworkUrl100)
                editor.putString("post_${postCount}_music_previewUrl", music.previewUrl)
                editor.putString("post_${postCount}_music_collectionName", music.collectionName)
                editor.putString("post_${postCount}_music_trackViewUrl", music.trackViewUrl)
                editor.putString("post_${postCount}_music_releaseDate", music.releaseDate)
            }

            // Increment post count
            editor.putInt("post_count", postCount + 1)
            editor.apply()

            Log.d("PostHelper", "Saved post $postCount with music: ${music != null}")
        } catch (e: Exception) {
            Log.e("PostHelper", "Error saving post", e)
        }
    }

    /**
     * Load all posts for the current user
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

                // Load music data if it exists
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
                            userProfileImage = "",
                            caption = caption,
                            imageUrl = "",
                            likes = likes,
                            comments = 0,
                            timestamp = timestamp,
                            music = music
                        )
                    )
                }
            }

            Log.d("PostHelper", "Loaded ${posts.size} posts")
        } catch (e: Exception) {
            Log.e("PostHelper", "Error loading posts", e)
        }

        return posts
    }
}