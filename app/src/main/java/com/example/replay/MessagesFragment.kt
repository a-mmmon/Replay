package com.example.replay

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        loadConversationsFromFirebase()  // ← Firebase instead of hardcoded
    }

    private fun initializeViews(view: View) {
        conversationsRecyclerView = view.findViewById(R.id.conversationsRecyclerView)
        newMessageFab = view.findViewById(R.id.newMessageFab)
    }

    private fun setupRecyclerView() {
        conversationsAdapter = ConversationsAdapter(emptyList()) { conversation ->
            openConversation(conversation)
        }
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

    // ─── FIREBASE: Load real conversations ────────────────────────────────────
    private fun loadConversationsFromFirebase() {
        if (currentUserId.isEmpty()) return

        database.getReference("conversations").child(currentUserId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!isAdded) return
                    val conversations = mutableListOf<Conversation>()

                    for (child in snapshot.children) {
                        val conversation = child.getValue(Conversation::class.java)
                        conversation?.let { conversations.add(it) }
                    }

                    // Sort by timestamp descending (newest first)
                    conversations.sortByDescending { it.timestamp }
                    conversationsAdapter.updateConversations(conversations)
                }

                override fun onCancelled(error: DatabaseError) {
                    android.util.Log.e("MessagesFragment", "Failed to load conversations: ${error.message}")
                }
            })
    }

    private fun openConversation(conversation: Conversation) {
        val intent = Intent(requireContext(), ConversationActivity::class.java)
        intent.putExtra("conversation_id", conversation.conversationId)
        intent.putExtra("other_user_id", conversation.otherUserId)
        intent.putExtra("other_user_name", conversation.otherUserName)
        intent.putExtra("other_user_image", conversation.otherUserProfileImage)
        startActivity(intent)
    }

    private fun openConversationWithUser(user: UserProfile) {
        // Create conversation ID from both user IDs (sorted so it's consistent)
        val conversationId = listOf(currentUserId, user.userId).sorted().joinToString("_")

        val intent = Intent(requireContext(), ConversationActivity::class.java)
        intent.putExtra("conversation_id", conversationId)
        intent.putExtra("other_user_id", user.userId)
        intent.putExtra("other_user_name", user.username)
        intent.putExtra("other_user_image", user.profileImage)
        startActivity(intent)
    }
}