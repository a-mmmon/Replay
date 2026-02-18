package com.example.replay

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CommentActivity : AppCompatActivity() {

    private lateinit var postUsername: TextView
    private lateinit var postCaption: TextView
    private lateinit var postTimestamp: TextView
    private lateinit var commentsRecyclerView: RecyclerView
    private lateinit var commentInput: EditText
    private lateinit var sendButton: Button
    private lateinit var backButton: ImageButton

    private lateinit var commentAdapter: CommentAdapter
    private val comments = mutableListOf<Comment>()

    private var postId: String = ""
    private var commentCount: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment)

        initializeViews()
        loadPostData()
        setupRecyclerView()
        setupButtons()
        loadComments()
    }

    private fun initializeViews() {
        postUsername = findViewById(R.id.postUsername)
        postCaption = findViewById(R.id.postCaption)
        postTimestamp = findViewById(R.id.postTimestamp)
        commentsRecyclerView = findViewById(R.id.commentsRecyclerView)
        commentInput = findViewById(R.id.commentInput)
        sendButton = findViewById(R.id.sendButton)
        backButton = findViewById(R.id.backButton)
    }

    private fun loadPostData() {
        postId = intent.getStringExtra("POST_ID") ?: ""
        val username = intent.getStringExtra("POST_USERNAME") ?: ""
        val caption = intent.getStringExtra("POST_CAPTION") ?: ""
        val timestamp = intent.getLongExtra("POST_TIMESTAMP", 0L)
        commentCount = intent.getIntExtra("COMMENT_COUNT", 0)

        postUsername.text = username
        postCaption.text = caption
        postTimestamp.text = formatTimestamp(timestamp)
    }

    private fun setupRecyclerView() {
        commentAdapter = CommentAdapter(comments)
        commentsRecyclerView.layoutManager = LinearLayoutManager(this)
        commentsRecyclerView.adapter = commentAdapter
    }

    private fun setupButtons() {
        backButton.setOnClickListener {
            finish()
        }

        sendButton.setOnClickListener {
            val commentText = commentInput.text.toString().trim()
            if (commentText.isNotEmpty()) {
                addComment(commentText)
                commentInput.text.clear()
            } else {
                Toast.makeText(this, "Please enter a comment", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadComments() {
        val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val savedCommentCount = prefs.getInt("comment_count_$postId", 0)

        comments.clear()
        for (i in 0 until savedCommentCount) {
            val commentId = prefs.getString("comment_${postId}_${i}_id", "") ?: ""
            val username = prefs.getString("comment_${postId}_${i}_username", "") ?: ""
            val text = prefs.getString("comment_${postId}_${i}_text", "") ?: ""
            val timestamp = prefs.getLong("comment_${postId}_${i}_timestamp", 0L)

            if (commentId.isNotEmpty()) {
                comments.add(
                    Comment(
                        commentId = commentId,
                        postId = postId,
                        userId = "current_user",
                        username = username,
                        userProfileImage = "",
                        commentText = text,
                        timestamp = timestamp
                    )
                )
            }
        }

        commentAdapter.notifyDataSetChanged()
    }

    private fun addComment(text: String) {
        val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val currentUsername = prefs.getString("username", "User") ?: "User"
        val commentCount = prefs.getInt("comment_count_$postId", 0)

        val newComment = Comment(
            commentId = "comment_${System.currentTimeMillis()}",
            postId = postId,
            userId = "current_user",
            username = currentUsername,
            userProfileImage = "",
            commentText = text,
            timestamp = System.currentTimeMillis()
        )

        comments.add(newComment)
        commentAdapter.notifyItemInserted(comments.size - 1)
        commentsRecyclerView.scrollToPosition(comments.size - 1)

        // Save to SharedPreferences
        prefs.edit().apply {
            putString("comment_${postId}_${commentCount}_id", newComment.commentId)
            putString("comment_${postId}_${commentCount}_username", newComment.username)
            putString("comment_${postId}_${commentCount}_text", newComment.commentText)
            putLong("comment_${postId}_${commentCount}_timestamp", newComment.timestamp)
            putInt("comment_count_$postId", commentCount + 1)
            apply()
        }

        Toast.makeText(this, "Comment added!", Toast.LENGTH_SHORT).show()
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