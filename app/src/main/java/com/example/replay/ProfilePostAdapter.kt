package com.example.replay

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.content.Context
import android.content.ContextWrapper
import androidx.fragment.app.FragmentActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.bumptech.glide.Glide

class ProfilePostsAdapter(
    private var posts: List<Post>
) : RecyclerView.Adapter<ProfilePostsAdapter.PostViewHolder>() {

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
        val music = resolveSong(post)

        holder.userName.text = post.username
        holder.postText.text = post.caption
        holder.likesCount.text = post.likes.toString()
        holder.timestamp.text = formatTimestamp(post.timestamp)

        if (post.userProfileImage.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(post.userProfileImage)
                .placeholder(ThemeManager.getDefaultAvatarRes(holder.itemView.context))
                .into(holder.profileImage)
        } else {
            holder.profileImage.setImageResource(ThemeManager.getDefaultAvatarRes(holder.itemView.context))
        }

        if (music != null) {
            holder.musicContainer.visibility = View.VISIBLE
            holder.musicTitle.text = music.trackName
            holder.musicArtist.text = music.artistName
            if (music.artworkUrl100.isNotEmpty()) {
                Glide.with(holder.itemView.context)
                    .load(music.artworkUrl100)
                    .into(holder.musicImage)
            }
            holder.musicContainer.setOnClickListener {
                showMusicBottomSheet(holder.itemView.context, music)
            }
        } else {
            holder.musicContainer.visibility = View.GONE
            holder.musicContainer.setOnClickListener(null)
        }

        holder.likeButton.setImageResource(R.drawable.ic_heart_outline)
        holder.likeButton.setOnClickListener { onLikeClicked(post) }
    }

    override fun getItemCount(): Int = posts.size

    fun updatePosts(newPosts: List<Post>) {
        posts = newPosts
        notifyDataSetChanged()
    }

    private fun onLikeClicked(post: Post) {}

    private fun formatTimestamp(timestamp: Long): String {
        val diff = System.currentTimeMillis() - timestamp
        return when {
            diff < 60000 -> "Just now"
            diff < 3600000 -> "${diff / 60000}m"
            diff < 86400000 -> "${diff / 3600000}h"
            diff < 604800000 -> "${diff / 86400000}d"
            else -> "${diff / 604800000}w"
        }
    }

    private fun showMusicBottomSheet(context: android.content.Context, song: ITunesSong?) {
        val activity = findFragmentActivity(context) ?: return
        val validSong = song ?: return
        val bottomSheet = MusicPlayerBottomSheet(validSong) { }
        bottomSheet.show(activity.supportFragmentManager, "MusicPlayerBottomSheet")
    }

    private fun findFragmentActivity(context: Context): FragmentActivity? {
        var current = context
        while (current is ContextWrapper) {
            if (current is FragmentActivity) return current
            current = current.baseContext
        }
        return null
    }

    private fun resolveSong(post: Post): ITunesSong? {
        post.music?.let { return it }
        if (post.songTitle.isNullOrBlank()) return null
        return ITunesSong(
            trackId = if (post.postId.isNotBlank()) post.postId.hashCode().toLong() else 0L,
            trackName = post.songTitle ?: "",
            artistName = post.songArtist ?: "",
            artworkUrl100 = post.songImageUrl ?: ""
        )
    }
}
