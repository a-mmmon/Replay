package com.example.replay

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FeedAdapter(
    private var posts: List<Post>,
    private val onPostClick: (Post) -> Unit
) : RecyclerView.Adapter<FeedAdapter.PostViewHolder>() {

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgUserAvatar: ImageView = itemView.findViewById(R.id.imgUserAvatar)
        val tvUserName: TextView = itemView.findViewById(R.id.tvUserName)
        val tvPostTime: TextView = itemView.findViewById(R.id.tvPostTime)
        val tvPostContent: TextView = itemView.findViewById(R.id.tvPostContent)
        val imgLike: ImageView = itemView.findViewById(R.id.imgLike)
        val tvLikeCount: TextView = itemView.findViewById(R.id.tvLikeCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]

        holder.tvUserName.text = post.userName
        holder.tvPostContent.text = post.content
        holder.tvLikeCount.text = post.likesCount.toString()  // ✅ FIXED: likeCount → likesCount
        holder.tvPostTime.text = post.timestamp              // ✅ FIXED: time → timestamp

        holder.itemView.setOnClickListener {
            onPostClick(post)
        }
    }

    override fun getItemCount(): Int = posts.size
}