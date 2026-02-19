package com.example.replay

import android.content.Context
import android.util.Log

/**
 * Helper object to save and load posts with music data
 */
object PostHelper {

    /**
     * Save a post to SharedPreferences
     */
    fun savePost(context: Context, caption: String, music: ITunesSong?) {
        try {
            val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            val postCount = prefs.getInt("post_count", 0)
            val editor = prefs.edit()

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
    }
}
