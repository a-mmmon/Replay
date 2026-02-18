package com.example.replay

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView

class FeedAdapter(
    private val posts: MutableList<Post>,
    private val onPostClick: (Post) -> Unit,
    private val onUsernameClick: ((Post) -> Unit)? = null  // ← NEW: tap username to open profile
) : RecyclerView.Adapter<FeedAdapter.PostViewHolder>() {

    private val likedPosts = mutableSetOf<String>()
    private val likeCounts = mutableMapOf<String, Int>()
    private val repostedPosts = mutableSetOf<String>()
    private val repostCounts = mutableMapOf<String, Int>()
    private val commentCounts = mutableMapOf<String, Int>()

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImage: ShapeableImageView = itemView.findViewById(R.id.profileImage)
        val userName: TextView = itemView.findViewById(R.id.userName)
        val timestamp: TextView = itemView.findViewById(R.id.timestamp)
        val postText: TextView = itemView.findViewById(R.id.postText)
        val musicContainer: CardView = itemView.findViewById(R.id.musicContainer)
        val musicImage: ImageView = itemView.findViewById(R.id.musicImage)
        val musicTitle: TextView = itemView.findViewById(R.id.musicTitle)
        val musicArtist: TextView = itemView.findViewById(R.id.musicArtist)
        val likesCount: TextView = itemView.findViewById(R.id.likesCount)
        val likeButton: ImageView = itemView.findViewById(R.id.likeButton)
        val likeButtonContainer: LinearLayout = itemView.findViewById(R.id.likeButtonContainer)

        val repostIndicator: TextView = itemView.findViewById(R.id.repostIndicator)
        val commentsCount: TextView = itemView.findViewById(R.id.commentsCount)
        val commentButton: ImageView = itemView.findViewById(R.id.commentButton)
        val commentButtonContainer: LinearLayout = itemView.findViewById(R.id.commentButtonContainer)
        val repostsCount: TextView = itemView.findViewById(R.id.repostsCount)
        val repostButton: ImageView = itemView.findViewById(R.id.repostButton)
        val repostButtonContainer: LinearLayout = itemView.findViewById(R.id.repostButtonContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]

        if (!likeCounts.containsKey(post.postId)) {
            likeCounts[post.postId] = post.likes
        }
        if (!repostCounts.containsKey(post.postId)) {
            repostCounts[post.postId] = post.reposts
        }
        if (!commentCounts.containsKey(post.postId)) {
            commentCounts[post.postId] = post.comments
        }

        if (post.isRepost && post.repostedByUsername.isNotEmpty()) {
            holder.repostIndicator.visibility = View.VISIBLE
            holder.repostIndicator.text = "${post.repostedByUsername} reposted"
        } else {
            holder.repostIndicator.visibility = View.GONE
        }

        holder.userName.text = post.username
        holder.postText.text = post.caption
        holder.likesCount.text = likeCounts[post.postId].toString()
        holder.commentsCount.text = commentCounts[post.postId].toString()
        holder.repostsCount.text = repostCounts[post.postId].toString()
        holder.timestamp.text = formatTimestamp(post.timestamp)

        if (post.userProfileImage.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(post.userProfileImage)
                .placeholder(R.drawable.ic_android_placeholder)
                .into(holder.profileImage)
        } else {
            holder.profileImage.setImageResource(R.drawable.ic_android_placeholder)
        }

        if (post.music != null) {
            holder.musicContainer.visibility = View.VISIBLE
            holder.musicTitle.text = post.music.trackName
            holder.musicArtist.text = post.music.artistName
<<<<<<< HEAD
=======

>>>>>>> aa23625cdec0aadc5966ebfc9b59bd4c93417628
            if (post.music.artworkUrl100.isNotEmpty()) {
                Glide.with(holder.itemView.context)
                    .load(post.music.artworkUrl100)
                    .into(holder.musicImage)
            }
        } else {
            holder.musicContainer.visibility = View.GONE
        }

<<<<<<< HEAD
        // Check Firebase for like state
        PostHelper.isPostLiked(post.postId) { isLiked ->
            if (isLiked) likedPosts.add(post.postId)
            else likedPosts.remove(post.postId)
            holder.likeButton.setImageResource(
                if (isLiked) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline
            )
        }

=======
>>>>>>> aa23625cdec0aadc5966ebfc9b59bd4c93417628
        val isLiked = likedPosts.contains(post.postId)
        holder.likeButton.setImageResource(
            if (isLiked) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline
        )

<<<<<<< HEAD
        // ✅ Username + profile image both open user profile
        val profileClickListener = View.OnClickListener {
            onUsernameClick?.invoke(post)
        }
        holder.userName.setOnClickListener(profileClickListener)
        holder.profileImage.setOnClickListener(profileClickListener)

        holder.likeButton.setOnClickListener {
            val p = holder.bindingAdapterPosition
            if (p != RecyclerView.NO_POSITION) toggleLike(posts[p], p, holder)
        }

        holder.itemView.setOnClickListener { onPostClick(post) }
=======
        // ✅ FIX: Check if user has reposted using SharedPreferences
        val context = holder.itemView.context
        val prefs = context.getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
        val hasReposted = isPostReposted(prefs, post.postId)

        if (hasReposted) {
            repostedPosts.add(post.postId)
        }

        val isReposted = repostedPosts.contains(post.postId)
        holder.repostButton.setColorFilter(
            if (isReposted) {
                context.getColor(android.R.color.holo_green_dark)
            } else {
                context.getColor(android.R.color.darker_gray)
            }
        )

        holder.likeButtonContainer.setOnClickListener {
            val currentPosition = holder.bindingAdapterPosition
            if (currentPosition != RecyclerView.NO_POSITION) {
                val currentPost = posts[currentPosition]
                toggleLike(currentPost, currentPosition)
            }
        }

        holder.commentButtonContainer.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, CommentActivity::class.java).apply {
                putExtra("POST_ID", post.postId)
                putExtra("POST_USERNAME", post.username)
                putExtra("POST_CAPTION", post.caption)
                putExtra("POST_TIMESTAMP", post.timestamp)
                putExtra("COMMENT_COUNT", commentCounts[post.postId] ?: post.comments)
            }
            context.startActivity(intent)
        }

        holder.repostButtonContainer.setOnClickListener {
            val currentPosition = holder.bindingAdapterPosition
            if (currentPosition != RecyclerView.NO_POSITION) {
                val currentPost = posts[currentPosition]
                toggleRepost(currentPost, currentPosition, holder.itemView.context)
            }
        }

        holder.itemView.setOnClickListener {
            onPostClick(post)
        }
>>>>>>> aa23625cdec0aadc5966ebfc9b59bd4c93417628
    }

    override fun getItemCount(): Int = posts.size

<<<<<<< HEAD
    private fun toggleLike(post: Post, position: Int, holder: PostViewHolder) {
        PostHelper.toggleLike(post.postId) { isLiked, newCount ->
            if (isLiked) likedPosts.add(post.postId) else likedPosts.remove(post.postId)
            likeCounts[post.postId] = newCount
            holder.likesCount.text = newCount.toString()
            holder.likeButton.setImageResource(
                if (isLiked) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline
            )
        }
=======
    private fun toggleLike(post: Post, position: Int) {
        val currentCount = likeCounts[post.postId] ?: post.likes

        if (likedPosts.contains(post.postId)) {
            likedPosts.remove(post.postId)
            likeCounts[post.postId] = currentCount - 1
        } else {
            likedPosts.add(post.postId)
            likeCounts[post.postId] = currentCount + 1
        }

        notifyItemChanged(position)
>>>>>>> aa23625cdec0aadc5966ebfc9b59bd4c93417628
    }

    private fun toggleRepost(post: Post, position: Int, context: android.content.Context) {
        val currentCount = repostCounts[post.postId] ?: post.reposts
        val prefs = context.getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)

        if (repostedPosts.contains(post.postId)) {
            // Remove repost
            repostedPosts.remove(post.postId)
            repostCounts[post.postId] = currentCount - 1
            removeRepostFromProfile(prefs, post.postId)
            Toast.makeText(context, "Repost removed", Toast.LENGTH_SHORT).show()
        } else {
            // Add repost
            repostedPosts.add(post.postId)
            repostCounts[post.postId] = currentCount + 1
            saveRepostToProfile(prefs, post)
            Toast.makeText(context, "Reposted!", Toast.LENGTH_SHORT).show()
        }

        notifyItemChanged(position)
    }

    // ✅ FIX: Check if post is already reposted
    private fun isPostReposted(prefs: android.content.SharedPreferences, postId: String): Boolean {
        val repostCount = prefs.getInt("repost_count", 0)
        for (i in 0 until repostCount) {
            val originalPostId = prefs.getString("repost_${i}_original_post_id", "")
            if (originalPostId == postId) {
                return true
            }
        }
        return false
    }

    // ✅ FIX: Improved save repost function
    private fun saveRepostToProfile(prefs: android.content.SharedPreferences, post: Post) {
        val currentUsername = prefs.getString("username", "User") ?: "User"
        val repostCount = prefs.getInt("repost_count", 0)

        prefs.edit().apply {
            putString("repost_${repostCount}_id", "repost_${System.currentTimeMillis()}")
            putString("repost_${repostCount}_original_post_id", post.postId)
            putString("repost_${repostCount}_username", post.username)
            putString("repost_${repostCount}_caption", post.caption)
            putLong("repost_${repostCount}_timestamp", System.currentTimeMillis())
            putInt("repost_${repostCount}_likes", post.likes)
            putInt("repost_${repostCount}_comments", post.comments)
            putString("repost_${repostCount}_reposted_by", currentUsername)

            post.music?.let { music ->
                putLong("repost_${repostCount}_music_trackId", music.trackId)
                putString("repost_${repostCount}_music_trackName", music.trackName)
                putString("repost_${repostCount}_music_artistName", music.artistName)
                putString("repost_${repostCount}_music_artworkUrl", music.artworkUrl100)
            }

            putInt("repost_count", repostCount + 1)
            apply()
        }
    }

    private fun removeRepostFromProfile(prefs: android.content.SharedPreferences, postId: String) {
        val repostCount = prefs.getInt("repost_count", 0)
        var foundIndex = -1

        for (i in 0 until repostCount) {
            val originalPostId = prefs.getString("repost_${i}_original_post_id", "")
            if (originalPostId == postId) {
                foundIndex = i
                break
            }
        }

        if (foundIndex != -1) {
            prefs.edit().apply {
                // Shift all reposts after the removed one
                for (i in foundIndex until repostCount - 1) {
                    val nextId = prefs.getString("repost_${i + 1}_id", "")
                    val nextOriginalPostId = prefs.getString("repost_${i + 1}_original_post_id", "")
                    val nextUsername = prefs.getString("repost_${i + 1}_username", "")
                    val nextCaption = prefs.getString("repost_${i + 1}_caption", "")
                    val nextTimestamp = prefs.getLong("repost_${i + 1}_timestamp", 0L)
                    val nextLikes = prefs.getInt("repost_${i + 1}_likes", 0)
                    val nextComments = prefs.getInt("repost_${i + 1}_comments", 0)
                    val nextRepostedBy = prefs.getString("repost_${i + 1}_reposted_by", "")

                    putString("repost_${i}_id", nextId)
                    putString("repost_${i}_original_post_id", nextOriginalPostId)
                    putString("repost_${i}_username", nextUsername)
                    putString("repost_${i}_caption", nextCaption)
                    putLong("repost_${i}_timestamp", nextTimestamp)
                    putInt("repost_${i}_likes", nextLikes)
                    putInt("repost_${i}_comments", nextComments)
                    putString("repost_${i}_reposted_by", nextRepostedBy)

                    if (prefs.contains("repost_${i + 1}_music_trackId")) {
                        val musicTrackId = prefs.getLong("repost_${i + 1}_music_trackId", 0L)
                        val musicTrackName = prefs.getString("repost_${i + 1}_music_trackName", "")
                        val musicArtistName = prefs.getString("repost_${i + 1}_music_artistName", "")
                        val musicArtworkUrl = prefs.getString("repost_${i + 1}_music_artworkUrl", "")

                        putLong("repost_${i}_music_trackId", musicTrackId)
                        putString("repost_${i}_music_trackName", musicTrackName)
                        putString("repost_${i}_music_artistName", musicArtistName)
                        putString("repost_${i}_music_artworkUrl", musicArtworkUrl)
                    }
                }

                // Remove the last repost entry
                remove("repost_${repostCount - 1}_id")
                remove("repost_${repostCount - 1}_original_post_id")
                remove("repost_${repostCount - 1}_username")
                remove("repost_${repostCount - 1}_caption")
                remove("repost_${repostCount - 1}_timestamp")
                remove("repost_${repostCount - 1}_likes")
                remove("repost_${repostCount - 1}_comments")
                remove("repost_${repostCount - 1}_reposted_by")
                remove("repost_${repostCount - 1}_music_trackId")
                remove("repost_${repostCount - 1}_music_trackName")
                remove("repost_${repostCount - 1}_music_artistName")
                remove("repost_${repostCount - 1}_music_artworkUrl")

                putInt("repost_count", repostCount - 1)
                apply()
            }
        }
    }

    private fun formatTimestamp(timestamp: Long): String {
        val diff = System.currentTimeMillis() - timestamp
        return when {
            diff < 60000 -> "Just now"
            diff < 3600000 -> "${diff / 60000}m ago"
            diff < 86400000 -> "${diff / 3600000}h ago"
            diff < 604800000 -> "${diff / 86400000}d ago"
            else -> "${diff / 604800000}w ago"
        }
    }
}