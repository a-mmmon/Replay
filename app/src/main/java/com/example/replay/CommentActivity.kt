package com.example.replay

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CommentActivity : BaseThemedActivity() {

    private lateinit var postUsername: TextView
    private lateinit var postCaption: TextView
    private lateinit var postTimestamp: TextView
    private lateinit var commentsRecyclerView: RecyclerView
    private lateinit var commentInput: EditText
    private lateinit var sendButton: Button
    private lateinit var backButton: ImageButton

    private lateinit var commentAdapter: CommentAdapter
    private val comments = mutableListOf<Comment>()

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()

    private var postId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment)

        initializeViews()
        loadPostData()
        setupRecyclerView()
        setupButtons()
        loadCommentsFromFirebase()
    }

    private fun initializeViews() {
        postUsername         = findViewById(R.id.postUsername)
        postCaption          = findViewById(R.id.postCaption)
        postTimestamp        = findViewById(R.id.postTimestamp)
        commentsRecyclerView = findViewById(R.id.commentsRecyclerView)
        commentInput         = findViewById(R.id.commentInput)
        sendButton           = findViewById(R.id.sendButton)
        backButton           = findViewById(R.id.backButton)
    }

    private fun loadPostData() {
        postId = intent.getStringExtra("POST_ID") ?: ""
        postUsername.text  = intent.getStringExtra("POST_USERNAME") ?: ""
        postCaption.text   = intent.getStringExtra("POST_CAPTION") ?: ""
        postTimestamp.text = formatTimestamp(intent.getLongExtra("POST_TIMESTAMP", 0L))
    }

    private fun setupRecyclerView() {
        commentAdapter = CommentAdapter(comments)
        commentsRecyclerView.layoutManager = LinearLayoutManager(this)
        commentsRecyclerView.adapter = commentAdapter
    }

    private fun setupButtons() {
        backButton.setOnClickListener { finish() }

        sendButton.setOnClickListener {
            val text = commentInput.text.toString().trim()
            if (text.isNotEmpty()) {
                addCommentToFirebase(text)
                commentInput.text.clear()
            } else {
                Toast.makeText(this, "Please enter a comment", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ─── Load comments from Firebase ─────────────────────────────────────────
    private fun loadCommentsFromFirebase() {
        if (postId.isBlank()) return

        database.getReference("comments").child(postId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    comments.clear()
                    for (child in snapshot.children) {
                        val comment = child.getValue(Comment::class.java)
                        if (comment != null) comments.add(comment)
                    }
                    comments.sortBy { it.timestamp }
                    commentAdapter.notifyDataSetChanged()
                    if (comments.isNotEmpty()) {
                        commentsRecyclerView.scrollToPosition(comments.size - 1)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("CommentActivity", "Failed to load comments: ${error.message}")
                }
            })
    }

    // ─── Add comment to Firebase + update post comment count ─────────────────
    private fun addCommentToFirebase(text: String) {
        if (postId.isBlank()) return

        val currentUser = auth.currentUser ?: run {
            Toast.makeText(this, "Please log in to comment", Toast.LENGTH_SHORT).show()
            return
        }

        val prefs = getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
        val username = prefs.getString("username", currentUser.email?.substringBefore("@") ?: "User") ?: "User"

        val commentId = "comment_${System.currentTimeMillis()}"
        val newComment = Comment(
            commentId        = commentId,
            postId           = postId,
            userId           = currentUser.uid,
            username         = username,
            userProfileImage = "",
            commentText      = text,
            timestamp        = System.currentTimeMillis()
        )

        // Save comment to Firebase under comments/postId/commentId
        database.getReference("comments").child(postId).child(commentId)
            .setValue(newComment)
            .addOnSuccessListener {
                // Increment comment count on the post
                updatePostCommentCount()
                Toast.makeText(this, "Comment added!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.e("CommentActivity", "Failed to add comment: ${e.message}")
                Toast.makeText(this, "Failed to add comment", Toast.LENGTH_SHORT).show()
            }
    }

    // ─── Atomically increment commentCount on the post ────────────────────────
    private fun updatePostCommentCount() {
        val postRef = database.getReference("posts").child(postId).child("commentCount")
        postRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val current = snapshot.getValue(Int::class.java) ?: 0
                postRef.setValue(current + 1)
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("CommentActivity", "Failed to update count: ${error.message}")
            }
        })
    }

    private fun formatTimestamp(timestamp: Long): String {
        val diff = System.currentTimeMillis() - timestamp
        return when {
            diff < 60000    -> "Just now"
            diff < 3600000  -> "${diff / 60000}m ago"
            diff < 86400000 -> "${diff / 3600000}h ago"
            diff < 604800000 -> "${diff / 86400000}d ago"
            else            -> "${diff / 604800000}w ago"
        }
    }
}