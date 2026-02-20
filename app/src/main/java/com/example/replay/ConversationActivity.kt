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
import com.google.firebase.database.MutableData
import com.google.firebase.database.ServerValue
import com.google.firebase.database.Transaction

class ConversationActivity : BaseThemedActivity() {

    private lateinit var backButton: ImageButton
    private lateinit var profileImage: ShapeableImageView
    private lateinit var userName: TextView
    private lateinit var conversationMenuButton: ImageButton
    private lateinit var messagesRecyclerView: RecyclerView
    private lateinit var messageInput: EditText
    private lateinit var sendButton: ImageButton
    private lateinit var messagesAdapter: ConversationMessagesAdapter

    private val auth     = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private val currentUserId get() = auth.currentUser?.uid ?: ""

    private var otherUserId    = ""
    private var otherUserName  = ""
    private var otherUserImage = ""
    private var conversationId = ""
    private var canMessage     = false
    private var myUsername     = ""
    private var myProfileImage = ""
    private var pendingBadgeCount = 0  // tracks unread to write for recipient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversation)

        otherUserId    = intent.getStringExtra("other_user_id") ?: ""
        otherUserName  = intent.getStringExtra("other_user_name") ?: ""
        otherUserImage = intent.getStringExtra("other_user_image") ?: ""
        conversationId = intent.getStringExtra("conversation_id")
            ?: listOf(currentUserId, otherUserId).sorted().joinToString("_")

        initializeViews()
        setupToolbar()
        setupRecyclerView()
        setupSendButton()

        // Load my profile info first, then validate messaging
        loadMyProfile()

        // Load recipient's badge base FIRST (before any clearing)
        // Then start messaging access check
        loadRecipientBadgeBase()
        // Clear MY OWN unread (separate path from recipient's)
        clearUnreadBadge()
    }

    private fun initializeViews() {
        backButton             = findViewById(R.id.backButton)
        profileImage           = findViewById(R.id.profileImage)
        userName               = findViewById(R.id.userName)
        conversationMenuButton = findViewById(R.id.conversationMenuButton)
        messagesRecyclerView   = findViewById(R.id.messagesRecyclerView)
        messageInput           = findViewById(R.id.messageInput)
        sendButton             = findViewById(R.id.sendButton)
    }

    private fun setupToolbar() {
        userName.text = otherUserName
        if (otherUserImage.isNotBlank()) {
            Glide.with(this).load(otherUserImage)
                .placeholder(ThemeManager.getDefaultAvatarRes(this))
                .into(profileImage)
        } else {
            profileImage.setImageResource(ThemeManager.getDefaultAvatarRes(this))
            loadOtherUserImage()
        }
        userName.setOnClickListener { openOtherUserProfile() }
        profileImage.setOnClickListener { openOtherUserProfile() }
        conversationMenuButton.setOnClickListener { showConversationMenu() }
        backButton.setOnClickListener { finish() }
    }

    // Load MY username and profileImage so we can correctly populate their conversation node
    private fun loadMyProfile() {
        if (currentUserId.isBlank()) {
            validateMessagingAccess()
            return
        }
        database.getReference("users").child(currentUserId).get()
            .addOnSuccessListener { snapshot ->
                val profile = snapshot.getValue(UserProfile::class.java)
                myUsername     = profile?.username ?: auth.currentUser?.email?.substringBefore("@") ?: "User"
                myProfileImage = profile?.profileImage ?: ""
                validateMessagingAccess()
            }
            .addOnFailureListener {
                myUsername = auth.currentUser?.email?.substringBefore("@") ?: "User"
                validateMessagingAccess()
            }
    }

    private fun setupRecyclerView() {
        messagesAdapter = ConversationMessagesAdapter(
            mutableListOf(), currentUserId
        ) { message -> onMessageLongPressed(message) }

        messagesRecyclerView.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        messagesRecyclerView.adapter = messagesAdapter
    }

    private fun setupSendButton() {
        sendButton.setOnClickListener { sendMessage() }
        messageInput.setOnEditorActionListener { _, _, _ ->
            sendMessage(); true
        }
    }

    private fun sendMessage() {
        if (!canMessage) {
            Toast.makeText(this, "Messaging unlocks after you follow each other.", Toast.LENGTH_SHORT).show()
            return
        }
        val text = messageInput.text.toString().trim()
        if (text.isEmpty()) return

        val messageId = database.getReference("messages").child(conversationId).push().key ?: return
        val message = mapOf(
            "messageId"  to messageId,
            "senderId"   to currentUserId,
            "receiverId" to otherUserId,
            "text"       to text,
            "timestamp"  to ServerValue.TIMESTAMP,
            "isRead"     to false
        )
        messageInput.text.clear()

        database.getReference("messages").child(conversationId).child(messageId)
            .setValue(message)
            .addOnSuccessListener { updateConversations(text) }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateConversations(lastMessage: String) {
        val timestamp = System.currentTimeMillis()

        // ── MY side: reset unread to 0 ──────────────────────────────────────
        database.getReference("conversations")
            .child(currentUserId).child(conversationId)
            .setValue(mapOf(
                "conversationId"        to conversationId,
                "otherUserId"           to otherUserId,
                "otherUserName"         to otherUserName,
                "otherUserProfileImage" to otherUserImage,
                "lastMessage"           to lastMessage,
                "timestamp"             to timestamp,
                "unreadBadge"           to 0
            ))

        // ── THEIR side: in-memory counter = no race condition ────────────────
        val theirConvRef = database.getReference("conversations")
            .child(otherUserId).child(conversationId)

        // Each call to this function = one new unread message for recipient
        pendingBadgeCount++
        Log.d("ConversationActivity", "Writing badge = $pendingBadgeCount to ${otherUserId}")

        // Write metadata and badge separately so rapid sends don't get batched
        theirConvRef.updateChildren(mapOf(
            "conversationId"        to conversationId,
            "otherUserId"           to currentUserId,
            "otherUserName"         to myUsername,
            "otherUserProfileImage" to myProfileImage,
            "lastMessage"           to lastMessage,
            "timestamp"             to timestamp
        ))
        // Write badge on its own path — separate write ensures each message counts
        theirConvRef.child("unreadBadge").setValue(pendingBadgeCount)
        Log.d("ConversationActivity", "Badge written: $pendingBadgeCount for $otherUserId")
    }

    // ── Load recipient's current badge so we can increment from correct base ──
    private fun loadRecipientBadgeBase() {
        if (otherUserId.isBlank() || conversationId.isBlank()) return
        database.getReference("conversations")
            .child(otherUserId).child(conversationId)
            .child("unreadBadge")
            .get()
            .addOnSuccessListener { snap ->
                val existingBadge = snap.getValue(Int::class.java) ?: 0
                pendingBadgeCount = existingBadge
                Log.d("ConversationActivity", "Base badge loaded = $pendingBadgeCount")
            }
            .addOnFailureListener {
                pendingBadgeCount = 0
            }
    }

    // ── Clear MY unread badge when I open the conversation ────────────────────
    private fun clearUnreadBadge() {
        if (currentUserId.isBlank() || conversationId.isBlank()) return
        database.getReference("conversations")
            .child(currentUserId).child(conversationId)
            .child("unreadBadge").setValue(0)
    }

    private fun listenToMessages() {
        database.getReference("messages").child(conversationId)
            .orderByChild("timestamp")
            .addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val message = Message(
                        messageId  = snapshot.child("messageId").getValue(String::class.java) ?: "",
                        senderId   = snapshot.child("senderId").getValue(String::class.java) ?: "",
                        receiverId = snapshot.child("receiverId").getValue(String::class.java) ?: "",
                        text       = snapshot.child("text").getValue(String::class.java) ?: "",
                        timestamp  = snapshot.child("timestamp").getValue(Long::class.java) ?: 0L,
                        isRead     = snapshot.child("isRead").getValue(Boolean::class.java) ?: false
                    )
                    messagesAdapter.addMessage(message)
                    messagesRecyclerView.scrollToPosition(messagesAdapter.itemCount - 1)

                    // Mark incoming as read
                    if (message.senderId != currentUserId) {
                        snapshot.ref.child("isRead").setValue(true)
                    }
                }
                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onChildRemoved(snapshot: DataSnapshot) {
                    val id = snapshot.child("messageId").getValue(String::class.java) ?: return
                    messagesAdapter.removeMessage(id)
                }
                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onCancelled(error: DatabaseError) {
                    Log.e("ConversationActivity", "Messages listener cancelled: ${error.message}")
                }
            })
    }

    private fun validateMessagingAccess() {
        FollowManager.canMessage(currentUserId, otherUserId) { allowed ->
            canMessage             = allowed
            sendButton.isEnabled   = allowed
            sendButton.alpha       = if (allowed) 1f else 0.4f
            messageInput.isEnabled = allowed
            messageInput.hint      = if (allowed) "Type a message" else "Follow each other to message"
            if (!allowed) {
                Toast.makeText(this, "You can only message users who follow you back.", Toast.LENGTH_SHORT).show()
            }
            listenToMessages()
        }
    }

    private fun loadOtherUserImage() {
        if (otherUserId.isBlank()) return
        database.getReference("users").child(otherUserId).get()
            .addOnSuccessListener { snapshot ->
                val url = snapshot.getValue(UserProfile::class.java)?.profileImage.orEmpty()
                if (url.isNotBlank()) {
                    Glide.with(this).load(url)
                        .placeholder(ThemeManager.getDefaultAvatarRes(this))
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
            .setItems(arrayOf("Delete message")) { _, _ -> deleteMessage(message) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteMessage(message: Message) {
        if (message.messageId.isBlank()) return
        database.getReference("messages").child(conversationId).child(message.messageId)
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
        popup.setOnMenuItemClickListener { confirmDeleteConversation(); true }
        popup.show()
    }

    private fun confirmDeleteConversation() {
        android.app.AlertDialog.Builder(this)
            .setTitle("Delete chat")
            .setMessage("This will remove the chat from your messages list.")
            .setPositiveButton("Delete") { _, _ -> deleteConversation() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteConversation() {
        if (currentUserId.isBlank() || conversationId.isBlank()) return
        database.getReference("conversations").child(currentUserId).child(conversationId)
            .removeValue()
            .addOnSuccessListener { Toast.makeText(this, "Chat removed.", Toast.LENGTH_SHORT).show(); finish() }
            .addOnFailureListener { Toast.makeText(this, "Failed to delete chat.", Toast.LENGTH_SHORT).show() }
    }
}