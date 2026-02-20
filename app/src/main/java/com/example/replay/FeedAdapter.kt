package com.example.replay

import android.content.Intent
import android.content.Context
import android.content.ContextWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FeedAdapter(
    private val posts: MutableList<Post>,
    private val onPostClick: ((Post) -> Unit)? = null,
    private val onUsernameClick: ((Post) -> Unit)? = null
) : RecyclerView.Adapter<FeedAdapter.PostViewHolder>() {

    private val likedPosts    = mutableSetOf<String>()
    private val likeCounts    = mutableMapOf<String, Int>()
    private val repostedPosts = mutableSetOf<String>()
    private val repostCounts  = mutableMapOf<String, Int>()
    private val commentCounts = mutableMapOf<String, Int>()
    private val auth     = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImage: ShapeableImageView   = itemView.findViewById(R.id.profileImage)
        val userName: TextView                 = itemView.findViewById(R.id.userName)
        val timestamp: TextView                = itemView.findViewById(R.id.timestamp)
        val postMenuButton: ImageView          = itemView.findViewById(R.id.postMenuButton)
        val postText: TextView                 = itemView.findViewById(R.id.postText)
        val musicContainer: CardView           = itemView.findViewById(R.id.musicContainer)
        val musicImage: ImageView              = itemView.findViewById(R.id.musicImage)
        val musicTitle: TextView               = itemView.findViewById(R.id.musicTitle)
        val musicArtist: TextView              = itemView.findViewById(R.id.musicArtist)
        val likesCount: TextView               = itemView.findViewById(R.id.likesCount)
        val likeButton: ImageView              = itemView.findViewById(R.id.likeButton)
        val likeButtonContainer: LinearLayout  = itemView.findViewById(R.id.likeButtonContainer)
        val repostIndicator: TextView          = itemView.findViewById(R.id.repostIndicator)
        val commentsCount: TextView            = itemView.findViewById(R.id.commentsCount)
        val commentButton: ImageView           = itemView.findViewById(R.id.commentButton)
        val commentButtonContainer: LinearLayout = itemView.findViewById(R.id.commentButtonContainer)
        val repostsCount: TextView             = itemView.findViewById(R.id.repostsCount)
        val repostButton: ImageView            = itemView.findViewById(R.id.repostButton)
        val repostButtonContainer: LinearLayout = itemView.findViewById(R.id.repostButtonContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post  = posts[position]
        val music = resolveSong(post)

        if (!likeCounts.containsKey(post.postId))    likeCounts[post.postId]    = post.likes
        if (!repostCounts.containsKey(post.postId))  repostCounts[post.postId]  = post.reposts
        if (!commentCounts.containsKey(post.postId)) commentCounts[post.postId] = post.comments

        // Repost indicator
        if (post.isRepost && post.repostedByUsername.isNotEmpty()) {
            holder.repostIndicator.visibility = View.VISIBLE
            holder.repostIndicator.text = "${post.repostedByUsername} reposted"
        } else {
            holder.repostIndicator.visibility = View.GONE
        }

        holder.userName.text     = post.username
        holder.postText.text     = post.caption
        holder.likesCount.text   = likeCounts[post.postId].toString()
        holder.commentsCount.text = commentCounts[post.postId].toString()
        holder.repostsCount.text = repostCounts[post.postId].toString()
        holder.timestamp.text    = formatTimestamp(post.timestamp)

        // ─── Live comment count from Firebase ────────────────────────────────
        loadCommentCount(post.postId, holder)

        val profileClickListener = View.OnClickListener { onUsernameClick?.invoke(post) }
        holder.userName.setOnClickListener(profileClickListener)
        holder.profileImage.setOnClickListener(profileClickListener)

        if (post.userProfileImage.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(post.userProfileImage)
                .placeholder(ThemeManager.getDefaultAvatarRes(holder.itemView.context))
                .into(holder.profileImage)
        } else {
            holder.profileImage.setImageResource(
                ThemeManager.getDefaultAvatarRes(holder.itemView.context)
            )
        }

        if (music != null) {
            holder.musicContainer.visibility = View.VISIBLE
            holder.musicTitle.text  = music.trackName
            holder.musicArtist.text = music.artistName
            if (music.artworkUrl100.isNotEmpty()) {
                Glide.with(holder.itemView.context).load(music.artworkUrl100).into(holder.musicImage)
            }
            holder.musicContainer.setOnClickListener {
                showMusicBottomSheet(holder.itemView.context, music)
            }
        } else {
            holder.musicContainer.visibility = View.GONE
            holder.musicContainer.setOnClickListener(null)
        }

        // Like state
        PostHelper.isPostLiked(post.postId) { isLiked ->
            if (isLiked) likedPosts.add(post.postId) else likedPosts.remove(post.postId)
            holder.likeButton.setImageResource(
                if (isLiked) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline
            )
        }

        // Repost state
        val context = holder.itemView.context
        val prefs = context.getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
        if (isPostReposted(prefs, post.postId)) repostedPosts.add(post.postId)
        val isReposted = repostedPosts.contains(post.postId)
        holder.repostButton.setColorFilter(
            if (isReposted) context.getColor(android.R.color.holo_green_dark)
            else context.getColor(android.R.color.darker_gray)
        )

        holder.likeButtonContainer.setOnClickListener {
            val p = holder.bindingAdapterPosition
            if (p != RecyclerView.NO_POSITION) toggleLike(posts[p], p, holder)
        }

        holder.commentButtonContainer.setOnClickListener {
            val intent = Intent(context, CommentActivity::class.java).apply {
                putExtra("POST_ID", post.postId)
                putExtra("POST_USERNAME", post.username)
                putExtra("POST_CAPTION", post.caption)
                putExtra("POST_TIMESTAMP", post.timestamp)
                putExtra("COMMENT_COUNT", commentCounts[post.postId] ?: 0)
            }
            context.startActivity(intent)
        }

        holder.repostButtonContainer.setOnClickListener {
            val p = holder.bindingAdapterPosition
            if (p != RecyclerView.NO_POSITION) toggleRepost(posts[p], p, context)
        }

        holder.itemView.setOnClickListener { onPostClick?.invoke(post) }

        val isOwnPost = post.userId == auth.currentUser?.uid
        holder.postMenuButton.visibility = if (isOwnPost) View.VISIBLE else View.GONE
        holder.postMenuButton.setOnClickListener {
            maybeShowOwnPostOptions(holder.bindingAdapterPosition, holder.itemView.context)
        }
        holder.itemView.setOnLongClickListener {
            maybeShowOwnPostOptions(holder.bindingAdapterPosition, holder.itemView.context)
            true
        }
    }

    override fun getItemCount(): Int = posts.size

    // ─── Load live comment count from Firebase ────────────────────────────────
    private fun loadCommentCount(postId: String, holder: PostViewHolder) {
        database.getReference("posts").child(postId).child("commentCount")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val count = snapshot.getValue(Int::class.java) ?: 0
                    commentCounts[postId] = count
                    holder.commentsCount.text = count.toString()
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

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

    private fun toggleRepost(post: Post, position: Int, context: android.content.Context) {
        val currentCount = repostCounts[post.postId] ?: post.reposts
        val prefs = context.getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)

        if (repostedPosts.contains(post.postId)) {
            repostedPosts.remove(post.postId)
            repostCounts[post.postId] = currentCount - 1
            removeRepostFromProfile(prefs, post.postId)
            Toast.makeText(context, "Repost removed", Toast.LENGTH_SHORT).show()
        } else {
            repostedPosts.add(post.postId)
            repostCounts[post.postId] = currentCount + 1
            saveRepostToProfile(prefs, post)
            Toast.makeText(context, "Reposted!", Toast.LENGTH_SHORT).show()
        }
        notifyItemChanged(position)
    }

    private fun isPostReposted(prefs: android.content.SharedPreferences, postId: String): Boolean {
        val repostCount = prefs.getInt("repost_count", 0)
        for (i in 0 until repostCount) {
            if (prefs.getString("repost_${i}_original_post_id", "") == postId) return true
        }
        return false
    }

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
            if (prefs.getString("repost_${i}_original_post_id", "") == postId) {
                foundIndex = i; break
            }
        }
        if (foundIndex == -1) return
        prefs.edit().apply {
            for (i in foundIndex until repostCount - 1) {
                putString("repost_${i}_id", prefs.getString("repost_${i+1}_id", ""))
                putString("repost_${i}_original_post_id", prefs.getString("repost_${i+1}_original_post_id", ""))
                putString("repost_${i}_username", prefs.getString("repost_${i+1}_username", ""))
                putString("repost_${i}_caption", prefs.getString("repost_${i+1}_caption", ""))
                putLong("repost_${i}_timestamp", prefs.getLong("repost_${i+1}_timestamp", 0L))
                putInt("repost_${i}_likes", prefs.getInt("repost_${i+1}_likes", 0))
                putInt("repost_${i}_comments", prefs.getInt("repost_${i+1}_comments", 0))
                putString("repost_${i}_reposted_by", prefs.getString("repost_${i+1}_reposted_by", ""))
            }
            remove("repost_${repostCount-1}_id")
            remove("repost_${repostCount-1}_original_post_id")
            remove("repost_${repostCount-1}_username")
            remove("repost_${repostCount-1}_caption")
            putInt("repost_count", repostCount - 1)
            apply()
        }
    }

    private fun formatTimestamp(timestamp: Long): String {
        val diff = System.currentTimeMillis() - timestamp
        return when {
            diff < 60000     -> "Just now"
            diff < 3600000   -> "${diff / 60000}m ago"
            diff < 86400000  -> "${diff / 3600000}h ago"
            diff < 604800000 -> "${diff / 86400000}d ago"
            else             -> "${diff / 604800000}w ago"
        }
    }

    private fun showMusicBottomSheet(context: android.content.Context, song: ITunesSong?) {
        val activity  = findFragmentActivity(context) ?: return
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
            trackId      = if (post.postId.isNotBlank()) post.postId.hashCode().toLong() else 0L,
            trackName    = post.songTitle ?: "",
            artistName   = post.songArtist ?: "",
            artworkUrl100 = post.songImageUrl ?: ""
        )
    }

    private fun maybeShowOwnPostOptions(position: Int, context: Context) {
        if (position == RecyclerView.NO_POSITION || position >= posts.size) return
        val post = posts[position]
        val currentUserId = auth.currentUser?.uid ?: return
        if (post.userId != currentUserId) return

        val activity = findFragmentActivity(context) ?: return
        android.app.AlertDialog.Builder(activity)
            .setTitle("Post options")
            .setItems(arrayOf("Edit post", "Delete post")) { _, which ->
                when (which) {
                    0 -> showEditPostDialog(activity, post, position)
                    1 -> confirmDeletePost(activity, post, position)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEditPostDialog(activity: FragmentActivity, post: Post, position: Int) {
        val input = EditText(activity).apply {
            setText(post.caption)
            setSelection(text.length)
        }
        android.app.AlertDialog.Builder(activity)
            .setTitle("Edit post")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val newCaption = input.text.toString().trim()
                if (newCaption.isBlank()) {
                    Toast.makeText(activity, "Caption cannot be empty.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                database.getReference("posts").child(post.postId).child("caption")
                    .setValue(newCaption)
                    .addOnSuccessListener {
                        posts[position] = post.copy(caption = newCaption)
                        notifyItemChanged(position)
                    }
                    .addOnFailureListener {
                        Toast.makeText(activity, "Failed to update post.", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun confirmDeletePost(activity: FragmentActivity, post: Post, position: Int) {
        android.app.AlertDialog.Builder(activity)
            .setTitle("Delete post")
            .setMessage("This cannot be undone.")
            .setPositiveButton("Delete") { _, _ -> deletePost(activity, post, position) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deletePost(activity: FragmentActivity, post: Post, position: Int) {
        if (post.postId.isBlank()) return
        database.getReference("posts").child(post.postId).removeValue()
            .addOnSuccessListener {
                posts.removeAt(position)
                notifyItemRemoved(position)
                database.getReference("likes").child(post.postId).removeValue()
                database.getReference("comments").child(post.postId).removeValue()
            }
            .addOnFailureListener {
                Toast.makeText(activity, "Failed to delete post.", Toast.LENGTH_SHORT).show()
            }
    }
}