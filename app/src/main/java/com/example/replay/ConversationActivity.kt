package com.example.replay

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue

class ConversationActivity : BaseThemedActivity() {

    private lateinit var backButton: ImageButton
    private lateinit var profileImage: ShapeableImageView
    private lateinit var userName: TextView
    private lateinit var conversationMenuButton: ImageButton
    private lateinit var messagesRecyclerView: RecyclerView
    private lateinit var messageInput: EditText
    private lateinit var sendButton: ImageButton
    private lateinit var messagesAdapter: ConversationMessagesAdapter

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private val currentUserId get() = auth.currentUser?.uid ?: ""

    private var otherUserId: String = ""
    private var otherUserName: String = ""
    private var otherUserImage: String = ""
    private var conversationId: String = ""
    private var canMessage = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversation)

        otherUserId = intent.getStringExtra("other_user_id") ?: ""
        otherUserName = intent.getStringExtra("other_user_name") ?: ""
        otherUserImage = intent.getStringExtra("other_user_image") ?: ""
        conversationId = intent.getStringExtra("conversation_id")
            ?: listOf(currentUserId, otherUserId).sorted().joinToString("_")

        initializeViews()
        setupToolbar()
        setupRecyclerView()
        setupSendButton()
        validateMessagingAccess()
    }

    private fun initializeViews() {
        backButton = findViewById(R.id.backButton)
        profileImage = findViewById(R.id.profileImage)
        userName = findViewById(R.id.userName)
        conversationMenuButton = findViewById(R.id.conversationMenuButton)
        messagesRecyclerView = findViewById(R.id.messagesRecyclerView)
        messageInput = findViewById(R.id.messageInput)
        sendButton = findViewById(R.id.sendButton)
    }

    private fun setupToolbar() {
        userName.text = otherUserName
        if (otherUserImage.isNotBlank()) {
            Glide.with(this)
                .load(otherUserImage)
                .placeholder(ThemeManager.getDefaultAvatarRes(this))
                .error(ThemeManager.getDefaultAvatarRes(this))
                .into(profileImage)
        } else {
            profileImage.setImageResource(ThemeManager.getDefaultAvatarRes(this))
            loadOtherUserImageFromProfile()
        }
        userName.setOnClickListener { openOtherUserProfile() }
        profileImage.setOnClickListener { openOtherUserProfile() }
        conversationMenuButton.setOnClickListener { showConversationMenu() }
        backButton.setOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        messagesAdapter = ConversationMessagesAdapter(
            mutableListOf(),
            currentUserId
        ) { message ->
            onMessageLongPressed(message)
        }
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
        if (!canMessage) {
            Toast.makeText(
                this,
                "Messaging unlocks after you follow each other.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

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
                override fun onChildRemoved(snapshot: DataSnapshot) {
                    val messageId = snapshot.child("messageId").getValue(String::class.java) ?: return
                    messagesAdapter.removeMessage(messageId)
                }
                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onCancelled(error: DatabaseError) {
                    Log.e("ConversationActivity", "Failed to load messages: ${error.message}")
                }
            })
    }

    private fun validateMessagingAccess() {
        FollowManager.canMessage(currentUserId, otherUserId) { allowed ->
            canMessage = allowed
            sendButton.isEnabled = allowed
            sendButton.alpha = if (allowed) 1f else 0.4f
            messageInput.isEnabled = allowed
            messageInput.hint = if (allowed) {
                "Type a message"
            } else {
                "Follow each other to message"
            }

            if (!allowed) {
                Toast.makeText(
                    this,
                    "You can only message users who follow you back.",
                    Toast.LENGTH_SHORT
                ).show()
            }
            listenToMessages()
        }
    }

    private fun loadOtherUserImageFromProfile() {
        if (otherUserId.isBlank()) return
        database.getReference("users").child(otherUserId).get()
            .addOnSuccessListener { snapshot ->
                val profile = snapshot.getValue(UserProfile::class.java)
                val imageUrl = profile?.profileImage.orEmpty()
                if (imageUrl.isNotBlank()) {
                    Glide.with(this)
                        .load(imageUrl)
                        .placeholder(ThemeManager.getDefaultAvatarRes(this))
                        .error(ThemeManager.getDefaultAvatarRes(this))
                        .into(profileImage)
                }
            }
    }

    private fun onMessageLongPressed(message: Message) {
        if (message.senderId != currentUserId) {
            Toast.makeText(this, "You can only delete your own messages.", Toast.LENGTH_SHORT).show()
            return
        }

        android.app.AlertDialog.Builder(this)
            .setTitle("Message options")
            .setItems(arrayOf("Delete message")) { _, _ ->
                deleteMessage(message)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteMessage(message: Message) {
        if (message.messageId.isBlank()) return
        database.getReference("messages")
            .child(conversationId)
            .child(message.messageId)
            .removeValue()
            .addOnFailureListener {
                Toast.makeText(this, "Failed to delete message.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun openOtherUserProfile() {
        if (otherUserId.isBlank()) return
        startActivity(Intent(this, UserProfileActivity::class.java).apply {
            putExtra("user_id", otherUserId)
            putExtra("username", otherUserName)
        })
    }

    private fun showConversationMenu() {
        val popup = PopupMenu(this, conversationMenuButton)
        popup.menu.add("Delete chat")
        popup.setOnMenuItemClickListener {
            confirmDeleteConversation()
            true
        }
        popup.show()
    }

    private fun confirmDeleteConversation() {
        android.app.AlertDialog.Builder(this)
            .setTitle("Delete chat")
            .setMessage("This will remove the chat box from your messages list.")
            .setPositiveButton("Delete") { _, _ ->
                deleteConversationForCurrentUser()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteConversationForCurrentUser() {
        if (currentUserId.isBlank() || conversationId.isBlank()) return
        database.getReference("conversations")
            .child(currentUserId)
            .child(conversationId)
            .removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "Chat removed.", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to delete chat.", Toast.LENGTH_SHORT).show()
            }
    }
}
