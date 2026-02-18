package com.example.replay

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
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

        if (!likeCounts.containsKey(post.postId)) {
            likeCounts[post.postId] = post.likes
        }

        holder.userName.text = post.username
        holder.postText.text = post.caption
        holder.likesCount.text = likeCounts[post.postId].toString()
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
            if (post.music.artworkUrl100.isNotEmpty()) {
                Glide.with(holder.itemView.context)
                    .load(post.music.artworkUrl100)
                    .into(holder.musicImage)
            }
        } else {
            holder.musicContainer.visibility = View.GONE
        }

        // Check Firebase for like state
        PostHelper.isPostLiked(post.postId) { isLiked ->
            if (isLiked) likedPosts.add(post.postId)
            else likedPosts.remove(post.postId)
            holder.likeButton.setImageResource(
                if (isLiked) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline
            )
        }

        val isLiked = likedPosts.contains(post.postId)
        holder.likeButton.setImageResource(
            if (isLiked) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline
        )

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
    }

    override fun getItemCount(): Int = posts.size

    private fun toggleLike(post: Post, position: Int, holder: PostViewHolder) {
        PostHelper.toggleLike(post.postId) { isLiked, newCount ->
            if (isLiked) likedPosts.add(post.postId) else likedPosts.remove(post.postId)
            likeCounts[post.postId] = newCount
            holder.likesCount.text = newCount.toString()
            holder.likeButton.setImageResource(
                if (isLiked) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline
            )
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