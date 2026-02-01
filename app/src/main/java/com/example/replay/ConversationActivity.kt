package com.example.replay

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView

class ConversationActivity : AppCompatActivity() {

    private lateinit var backButton: ImageButton
    private lateinit var profileImage: ShapeableImageView
    private lateinit var userName: TextView
    private lateinit var messagesRecyclerView: RecyclerView
    private lateinit var messageInput: EditText
    private lateinit var sendButton: ImageButton
    private lateinit var messagesAdapter: ConversationMessagesAdapter

    private var otherUserId: String = ""
    private var otherUserName: String = ""
    private var conversationId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversation)

        // Get data from intent
        otherUserId = intent.getStringExtra("other_user_id") ?: ""
        otherUserName = intent.getStringExtra("other_user_name") ?: ""
        conversationId = intent.getStringExtra("conversation_id")
        val otherUserImage = intent.getStringExtra("other_user_image") ?: ""

        initializeViews()
        setupToolbar()
        setupRecyclerView()
        setupSendButton()
        loadMessages()
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

        backButton.setOnClickListener {
            finish()
        }

        // Load profile image if you're using Glide
        // Glide.with(this).load(otherUserImage).into(profileImage)
    }

    private fun setupRecyclerView() {
        messagesAdapter = ConversationMessagesAdapter(emptyList(), getCurrentUserId())
        messagesRecyclerView.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        messagesRecyclerView.adapter = messagesAdapter
    }

    private fun setupSendButton() {
        sendButton.setOnClickListener {
            sendMessage()
        }
    }

    private fun sendMessage() {
        val text = messageInput.text.toString().trim()

        if (text.isEmpty()) {
            return
        }

        val message = Message(
            messageId = System.currentTimeMillis().toString(),  // ✅ FIXED: Changed from 'id' to 'messageId'
            senderId = getCurrentUserId(),
            receiverId = otherUserId,
            text = text,
            timestamp = System.currentTimeMillis(),
            isRead = false
        )

        // Save message to database
        saveMessage(message)

        // Add to adapter
        messagesAdapter.addMessage(message)

        // Clear input
        messageInput.text.clear()

        // Scroll to bottom
        messagesRecyclerView.scrollToPosition(messagesAdapter.itemCount - 1)
    }

    private fun loadMessages() {
        // Load messages from your data source
        val messages = getMessagesFromDataSource()
        messagesAdapter.updateMessages(messages)

        if (messages.isNotEmpty()) {
            messagesRecyclerView.scrollToPosition(messages.size - 1)
        }
    }

    private fun getCurrentUserId(): String {
        // Get current user ID from your auth system
        return "current_user_id"
    }

    private fun saveMessage(message: Message) {
        // Save to your database/backend
        // This could be Room, Firebase, or REST API

        // If it's a new conversation, create conversation ID
        if (conversationId == null) {
            conversationId = createConversation(otherUserId)
        }
    }

    private fun createConversation(otherUserId: String): String {
        // Create a new conversation in your database
        // Return the conversation ID
        return "conversation_${System.currentTimeMillis()}"
    }

    private fun getMessagesFromDataSource(): List<Message> {
        // Replace with actual data loading logic
        // Load from Room database, Firebase, or REST API

        // Sample data for demonstration
        return if (conversationId != null) {
            listOf(
                Message(
                    messageId = "1",  // ✅ FIXED: Changed from 'id' to 'messageId'
                    senderId = otherUserId,
                    receiverId = getCurrentUserId(),
                    text = "Hey! How are you?",
                    timestamp = System.currentTimeMillis() - 3600000,
                    isRead = true
                ),
                Message(
                    messageId = "2",  // ✅ FIXED: Changed from 'id' to 'messageId'
                    senderId = getCurrentUserId(),
                    receiverId = otherUserId,
                    text = "I'm great! Just discovered some amazing new music!",
                    timestamp = System.currentTimeMillis() - 3500000,
                    isRead = true
                )
            )
        } else {
            emptyList()
        }
    }
}