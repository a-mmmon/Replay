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
        val music = resolveSong(post)

        holder.userName.text = post.username
        holder.postText.text = post.caption
        holder.likesCount.text = post.likes.toString()
        holder.commentsCount.text = post.comments.toString()
        holder.repostsCount.text = post.reposts.toString()

        if (music != null) {

            holder.musicContainer.visibility = View.VISIBLE
            holder.musicTitle.text = music.trackName
            holder.musicArtist.text = music.artistName

            Glide.with(holder.itemView.context)
                .load(music.artworkUrl100)
                .into(holder.musicImage)

            holder.musicContainer.setOnClickListener {
                showMusicBottomSheet(holder.itemView.context, music)
            }

        } else {
            holder.musicContainer.visibility = View.GONE
            holder.musicContainer.setOnClickListener(null)
        }
    }

    override fun getItemCount(): Int = posts.size

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
