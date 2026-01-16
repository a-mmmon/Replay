package com.example.replay

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import de.hdodenhof.circleimageview.CircleImageView

class FeedAdapter(
    private var posts: List<Post>,
    private val onPostClick: (Post) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val VIEW_TYPE_TEXT = 0
        const val VIEW_TYPE_MUSIC = 1
    }

    inner class TextPostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgUserAvatar: CircleImageView = itemView.findViewById(R.id.imgUserAvatar)
        val tvUserName: TextView = itemView.findViewById(R.id.tvUserName)
        val tvPostTime: TextView = itemView.findViewById(R.id.tvPostTime)
        val tvPostContent: TextView = itemView.findViewById(R.id.tvPostContent)
        val imgLike: ImageView = itemView.findViewById(R.id.imgLike)
        val tvLikeCount: TextView = itemView.findViewById(R.id.tvLikeCount)
        val imgComment: ImageView = itemView.findViewById(R.id.imgComment)
        val tvCommentCount: TextView = itemView.findViewById(R.id.tvCommentCount)
        val imgShare: ImageView = itemView.findViewById(R.id.imgShare)

        fun bind(post: Post) {
            tvUserName.text = post.userName
            tvPostContent.text = post.content
            tvPostTime.text = getTimeAgo(post.timestamp)
            tvLikeCount.text = post.likes.toString()
            tvCommentCount.text = post.comments.toString()

            Glide.with(itemView.context)
                .load(post.userAvatar)
                .placeholder(R.drawable.ic_profile)
                .into(imgUserAvatar)

            updateLikeIcon(post.isLiked)

            imgLike.setOnClickListener {
                post.isLiked = !post.isLiked
                updateLikeIcon(post.isLiked)
                // Update like count in backend
            }

            itemView.setOnClickListener {
                onPostClick(post)
            }
        }

        private fun updateLikeIcon(isLiked: Boolean) {
            imgLike.setImageResource(
                if (isLiked) R.drawable.ic_favorite_filled
                else R.drawable.ic_favorite
            )
        }
    }

    inner class MusicPostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgUserAvatar: CircleImageView = itemView.findViewById(R.id.imgUserAvatar)
        val tvUserName: TextView = itemView.findViewById(R.id.tvUserName)
        val tvPostTime: TextView = itemView.findViewById(R.id.tvPostTime)
        val tvPostContent: TextView = itemView.findViewById(R.id.tvPostContent)
        val cardMusic: CardView = itemView.findViewById(R.id.cardMusic)
        val imgMusicCover: ImageView = itemView.findViewById(R.id.imgMusicCover)
        val tvMusicTitle: TextView = itemView.findViewById(R.id.tvMusicTitle)
        val tvMusicArtist: TextView = itemView.findViewById(R.id.tvMusicArtist)
        val imgPlayMusic: ImageView = itemView.findViewById(R.id.imgPlayMusic)
        val imgLike: ImageView = itemView.findViewById(R.id.imgLike)
        val tvLikeCount: TextView = itemView.findViewById(R.id.tvLikeCount)
        val imgComment: ImageView = itemView.findViewById(R.id.imgComment)
        val tvCommentCount: TextView = itemView.findViewById(R.id.tvCommentCount)
        val imgShare: ImageView = itemView.findViewById(R.id.imgShare)

        fun bind(post: Post) {
            tvUserName.text = post.userName
            tvPostContent.text = post.content
            tvPostTime.text = getTimeAgo(post.timestamp)
            tvLikeCount.text = post.likes.toString()
            tvCommentCount.text = post.comments.toString()

            Glide.with(itemView.context)
                .load(post.userAvatar)
                .placeholder(R.drawable.ic_profile)
                .into(imgUserAvatar)

            post.musicAttachment?.let { music ->
                tvMusicTitle.text = music.title
                tvMusicArtist.text = music.artist

                Glide.with(itemView.context)
                    .load(music.albumArt)
                    .placeholder(R.drawable.ic_music)
                    .into(imgMusicCover)

                imgPlayMusic.setOnClickListener {
                    // Play music
                }
            }

            updateLikeIcon(post.isLiked)

            imgLike.setOnClickListener {
                post.isLiked = !post.isLiked
                updateLikeIcon(post.isLiked)
            }

            itemView.setOnClickListener {
                onPostClick(post)
            }
        }

        private fun updateLikeIcon(isLiked: Boolean) {
            imgLike.setImageResource(
                if (isLiked) R.drawable.ic_favorite_filled
                else R.drawable.ic_favorite
            )
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (posts[position].type) {
            PostType.TEXT -> VIEW_TYPE_TEXT
            PostType.MUSIC -> VIEW_TYPE_MUSIC
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_TEXT -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_post_text, parent, false)
                TextPostViewHolder(view)
            }
            VIEW_TYPE_MUSIC -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_post_music, parent, false)
                MusicPostViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is TextPostViewHolder -> holder.bind(posts[position])
            is MusicPostViewHolder -> holder.bind(posts[position])
        }
    }

    override fun getItemCount(): Int = posts.size

    fun updateData(newPosts: List<Post>) {
        posts = newPosts
        notifyDataSetChanged()
    }

    private fun getTimeAgo(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        return when {
            diff < 60000 -> "Just now"
            diff < 3600000 -> "${diff / 60000}m ago"
            diff < 86400000 -> "${diff / 3600000}h ago"
            else -> "${diff / 86400000}d ago"
        }
    }
}