package com.example.replay

import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue

class ConversationActivity : AppCompatActivity() {

    private lateinit var backButton: ImageButton
    private lateinit var profileImage: ShapeableImageView
    private lateinit var userName: TextView
    private lateinit var messagesRecyclerView: RecyclerView
    private lateinit var messageInput: EditText
    private lateinit var sendButton: ImageButton
    private lateinit var messagesAdapter: ConversationMessagesAdapter

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private val currentUserId get() = auth.currentUser?.uid ?: ""

    private var otherUserId: String = ""
    private var otherUserName: String = ""
    private var conversationId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversation)

        otherUserId = intent.getStringExtra("other_user_id") ?: ""
        otherUserName = intent.getStringExtra("other_user_name") ?: ""
        conversationId = intent.getStringExtra("conversation_id")
            ?: listOf(currentUserId, otherUserId).sorted().joinToString("_")

        initializeViews()
        setupToolbar()
        setupRecyclerView()
        setupSendButton()
        listenToMessages()
    }

    private fun initializeViews() {
        backButton = findViewById(R.id.backButton)
        profileImage = findViewById(R.id.profileImage)
        userName = findViewById(R.id.userName)
        messagesRecyclerView = findViewById(R.id.messagesRecyclerView)
        messageInput = findViewById(R.id.messageInput)
        sendButton = findViewById(R.id.sendButton)
    }

    private fun setupToolbar() {
        userName.text = otherUserName
        backButton.setOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        messagesAdapter = ConversationMessagesAdapter(mutableListOf(), currentUserId)
        messagesRecyclerView.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        messagesRecyclerView.adapter = messagesAdapter
    }

    private fun setupSendButton() {
        sendButton.setOnClickListener { sendMessage() }

        // Also send on keyboard action
        messageInput.setOnEditorActionListener { _, _, _ ->
            sendMessage()
            true
        }
    }

    private fun sendMessage() {
        val text = messageInput.text.toString().trim()
        if (text.isEmpty()) return

        val messageId = database.getReference("messages").child(conversationId).push().key ?: return

        val message = mapOf(
            "messageId" to messageId,
            "senderId" to currentUserId,
            "receiverId" to otherUserId,
            "text" to text,
            "timestamp" to ServerValue.TIMESTAMP,
            "isRead" to false
        )

        messageInput.text.clear()

        database.getReference("messages").child(conversationId).child(messageId)
            .setValue(message)
            .addOnSuccessListener {
                updateConversationForBothUsers(text)
                Log.d("ConversationActivity", "Message sent: $messageId")
            }
            .addOnFailureListener { e ->
                Log.e("ConversationActivity", "Failed to send: ${e.message}")
            }
    }

    private fun updateConversationForBothUsers(lastMessage: String) {
        val timestamp = System.currentTimeMillis()

        database.getReference("users").child(currentUserId).get()
            .addOnSuccessListener { snapshot ->
                val myProfile = snapshot.getValue(UserProfile::class.java)
                val myName = myProfile?.username
                    ?: auth.currentUser?.email?.substringBefore("@") ?: "User"

                val myConversation = mapOf(
                    "conversationId" to conversationId,
                    "otherUserId" to otherUserId,
                    "otherUserName" to otherUserName,
                    "otherUserProfileImage" to "",
                    "lastMessage" to lastMessage,
                    "timestamp" to timestamp,
                    "unreadBadge" to 0
                )
                database.getReference("conversations")
                    .child(currentUserId).child(conversationId)
                    .setValue(myConversation)

                val theirConversation = mapOf(
                    "conversationId" to conversationId,
                    "otherUserId" to currentUserId,
                    "otherUserName" to myName,
                    "otherUserProfileImage" to (myProfile?.profileImage ?: ""),
                    "lastMessage" to lastMessage,
                    "timestamp" to timestamp,
                    "unreadBadge" to 1
                )
                database.getReference("conversations")
                    .child(otherUserId).child(conversationId)
                    .setValue(theirConversation)
            }
    }

    private fun listenToMessages() {
        database.getReference("messages").child(conversationId)
            .orderByChild("timestamp")
            .addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    // Manually map to handle both var fields and Firebase naming
                    val messageId = snapshot.child("messageId").getValue(String::class.java) ?: ""
                    val senderId = snapshot.child("senderId").getValue(String::class.java) ?: ""
                    val receiverId = snapshot.child("receiverId").getValue(String::class.java) ?: ""
                    val text = snapshot.child("text").getValue(String::class.java) ?: ""
                    val timestamp = snapshot.child("timestamp").getValue(Long::class.java) ?: 0L
                    val isRead = snapshot.child("isRead").getValue(Boolean::class.java) ?: false

                    val message = Message(
                        messageId = messageId,
                        senderId = senderId,
                        receiverId = receiverId,
                        text = text,
                        timestamp = timestamp,
                        isRead = isRead
                    )

                    messagesAdapter.addMessage(message)
                    messagesRecyclerView.scrollToPosition(messagesAdapter.itemCount - 1)
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onChildRemoved(snapshot: DataSnapshot) {}
                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onCancelled(error: DatabaseError) {
                    Log.e("ConversationActivity", "Failed to load messages: ${error.message}")
                }
            })
    }
}