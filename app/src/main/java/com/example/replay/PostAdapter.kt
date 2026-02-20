package com.example.replay

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class PostAdapter(private val posts: List<Post>) :
    RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val userName: TextView = itemView.findViewById(R.id.userName)
        val postText: TextView = itemView.findViewById(R.id.postText)
        val musicContainer: CardView = itemView.findViewById(R.id.musicContainer)
        val musicImage: ImageView = itemView.findViewById(R.id.musicImage)
        val musicTitle: TextView = itemView.findViewById(R.id.musicTitle)
        val musicArtist: TextView = itemView.findViewById(R.id.musicArtist)

        val likesCount: TextView = itemView.findViewById(R.id.likesCount)
        val commentsCount: TextView = itemView.findViewById(R.id.commentsCount)
        val repostsCount: TextView = itemView.findViewById(R.id.repostsCount)


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]

        holder.userName.text = post.username
        holder.postText.text = post.caption
        holder.likesCount.text = post.likes.toString()
        holder.commentsCount.text = post.comments.toString()
        holder.repostsCount.text = post.reposts.toString()

        // ✅ If this is a music post
        if (post.songTitle != null && post.songImageUrl != null) {

            holder.musicContainer.visibility = View.VISIBLE
            holder.musicTitle.text = post.songTitle
            holder.musicArtist.text = post.songArtist

            Glide.with(holder.itemView.context)
                .load(post.songImageUrl)
                .into(holder.musicImage)

        } else {
            // ✅ Normal post
            holder.musicContainer.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = posts.size
}
