package com.example.replay

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MessagesFragment : Fragment() {

    private lateinit var conversationsRecyclerView: RecyclerView
    private lateinit var conversationsAdapter: ConversationsAdapter
    private lateinit var newMessageFab: FloatingActionButton

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private val currentUserId get() = auth.currentUser?.uid ?: ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_messages, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeViews(view)
        setupRecyclerView()
        setupFab()
        loadConversationsFromFirebase()
    }

    private fun initializeViews(view: View) {
        conversationsRecyclerView = view.findViewById(R.id.conversationsRecyclerView)
        newMessageFab = view.findViewById(R.id.newMessageFab)
    }

    private fun setupRecyclerView() {
        conversationsAdapter = ConversationsAdapter(
            emptyList(),
            onConversationClick = { conversation ->
                openConversation(conversation)
            },
            onConversationMenuClick = { anchor, conversation ->
                showConversationMenu(anchor, conversation)
            }
        )
        conversationsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        conversationsRecyclerView.adapter = conversationsAdapter
    }

    private fun setupFab() {
        newMessageFab.setOnClickListener {
            openNewMessageDialog()
        }
    }

    private fun openNewMessageDialog() {
        val dialog = NewMessageBottomSheet()
        dialog.setOnUserSelectedListener { user ->
            openConversationWithUser(user)
        }
        dialog.show(parentFragmentManager, "NewMessage")
    }

    private fun loadConversationsFromFirebase() {
        if (currentUserId.isEmpty()) return

        database.getReference("conversations").child(currentUserId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!isAdded) return
                    val conversations = mutableListOf<Conversation>()

                    for (child in snapshot.children) {
                        val conversation = child.getValue(Conversation::class.java)
                        if (conversation != null &&
                            conversation.otherUserId.isNotBlank() &&
                            conversation.otherUserName.isNotBlank()
                        ) {
                            conversations.add(conversation)
                        }
                    }

                    conversations.sortByDescending { it.timestamp }
                    conversationsAdapter.updateConversations(conversations)
                }

                override fun onCancelled(error: DatabaseError) {
                    android.util.Log.e("MessagesFragment", "Failed to load conversations: ${error.message}")
                }
            })
    }

    private fun openConversation(conversation: Conversation) {
        // ✅ Reset unread badge when user opens the conversation
        if (currentUserId.isNotBlank() && conversation.conversationId.isNotBlank()) {
            database.getReference("conversations")
                .child(currentUserId)
                .child(conversation.conversationId)
                .child("unreadBadge")
                .setValue(0)
        }

        val intent = Intent(requireContext(), ConversationActivity::class.java)
        intent.putExtra("conversation_id", conversation.conversationId)
        intent.putExtra("other_user_id", conversation.otherUserId)
        intent.putExtra("other_user_name", conversation.otherUserName)
        intent.putExtra("other_user_image", conversation.otherUserProfileImage)
        startActivity(intent)
    }

    private fun openConversationWithUser(user: UserProfile) {
        val conversationId = listOf(currentUserId, user.userId).sorted().joinToString("_")

        // ✅ Reset unread badge here too
        if (currentUserId.isNotBlank() && conversationId.isNotBlank()) {
            database.getReference("conversations")
                .child(currentUserId)
                .child(conversationId)
                .child("unreadBadge")
                .setValue(0)
        }

        val intent = Intent(requireContext(), ConversationActivity::class.java)
        intent.putExtra("conversation_id", conversationId)
        intent.putExtra("other_user_id", user.userId)
        intent.putExtra("other_user_name", user.username)
        intent.putExtra("other_user_image", user.profileImage)
        startActivity(intent)
    }

    private fun showConversationMenu(anchor: View, conversation: Conversation) {
        val popup = PopupMenu(requireContext(), anchor)
        popup.menu.add("Delete chat")
        popup.setOnMenuItemClickListener {
            confirmDeleteConversation(conversation)
            true
        }
        popup.show()
    }

    private fun confirmDeleteConversation(conversation: Conversation) {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Delete chat")
            .setMessage("This will remove the chat box from your messages list.")
            .setPositiveButton("Delete") { _, _ ->
                deleteConversation(conversation)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteConversation(conversation: Conversation) {
        if (currentUserId.isBlank() || conversation.conversationId.isBlank()) return
        database.getReference("conversations")
            .child(currentUserId)
            .child(conversation.conversationId)
            .removeValue()
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to delete chat.", Toast.LENGTH_SHORT).show()
            }
    }
}