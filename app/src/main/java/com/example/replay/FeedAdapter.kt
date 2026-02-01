package com.example.replay

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.bumptech.glide.Glide

class FeedAdapter(
    private val posts: MutableList<Post>,
    private val onPostClick: (Post) -> Unit
) : RecyclerView.Adapter<FeedAdapter.PostViewHolder>() {

    // Track liked posts and their like counts
    private val likedPosts = mutableSetOf<String>()
    private val likeCounts = mutableMapOf<String, Int>()

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
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]

        // Initialize like count for this post if not already done
        if (!likeCounts.containsKey(post.postId)) {
            likeCounts[post.postId] = post.likes
        }

        holder.userName.text = post.username
        holder.postText.text = post.caption
        holder.likesCount.text = likeCounts[post.postId].toString()
        holder.timestamp.text = formatTimestamp(post.timestamp)

        // Load profile image
        if (post.userProfileImage.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(post.userProfileImage)
                .placeholder(R.drawable.ic_android_placeholder)
                .into(holder.profileImage)
        } else {
            holder.profileImage.setImageResource(R.drawable.ic_android_placeholder)
        }

        // Show/hide music container based on whether post has music
        if (post.music != null) {
            holder.musicContainer.visibility = View.VISIBLE
            holder.musicTitle.text = post.music.trackName
            holder.musicArtist.text = post.music.artistName

            // Load album art
            if (post.music.artworkUrl100.isNotEmpty()) {
                Glide.with(holder.itemView.context)
                    .load(post.music.artworkUrl100)
                    .into(holder.musicImage)
            }
        } else {
            holder.musicContainer.visibility = View.GONE
        }

        // ✅ FIXED: Set like button icon based on liked state
        val isLiked = likedPosts.contains(post.postId)
        holder.likeButton.setImageResource(
            if (isLiked) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline
        )

        // ✅ FIXED: Like button click listener that actually works
        holder.likeButton.setOnClickListener {
            val currentPosition = holder.bindingAdapterPosition
            if (currentPosition != RecyclerView.NO_POSITION) {
                val currentPost = posts[currentPosition]
                toggleLike(currentPost, currentPosition)
            }
        }

        // Post click listener
        holder.itemView.setOnClickListener {
            onPostClick(post)
        }
    }

    override fun getItemCount(): Int = posts.size

    // ✅ FIXED: Toggle like functionality without reassigning val
    private fun toggleLike(post: Post, position: Int) {
        val currentCount = likeCounts[post.postId] ?: post.likes

        if (likedPosts.contains(post.postId)) {
            // Unlike
            likedPosts.remove(post.postId)
            likeCounts[post.postId] = currentCount - 1
        } else {
            // Like
            likedPosts.add(post.postId)
            likeCounts[post.postId] = currentCount + 1
        }

        // Update only this item
        notifyItemChanged(position)
    }

    private fun formatTimestamp(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        return when {
            diff < 60000 -> "Just now"
            diff < 3600000 -> "${diff / 60000}m ago"
            diff < 86400000 -> "${diff / 3600000}h ago"
            diff < 604800000 -> "${diff / 86400000}d ago"
            else -> "${diff / 604800000}w ago"
        }
    }
}