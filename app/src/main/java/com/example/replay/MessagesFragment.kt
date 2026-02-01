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

class MessagesFragment : Fragment() {

    private lateinit var conversationsRecyclerView: RecyclerView
    private lateinit var conversationsAdapter: ConversationsAdapter
    private lateinit var newMessageFab: FloatingActionButton

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
        loadConversations()
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
            // Open conversation with selected user
            openConversationWithUser(user)
        }
        dialog.show(parentFragmentManager, "NewMessage")
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
        val intent = Intent(requireContext(), ConversationActivity::class.java)
        intent.putExtra("other_user_id", user.userId)
        intent.putExtra("other_user_name", user.username)
        intent.putExtra("other_user_image", user.profileImage)
        startActivity(intent)
    }

    private fun loadConversations() {
        // Load conversations from your data source
        val conversations = getConversationsFromDataSource()
        conversationsAdapter.updateConversations(conversations)
    }

    private fun getConversationsFromDataSource(): List<Conversation> {
        // Replace with actual data loading logic
        // This is sample data
        return listOf(
            Conversation(
                conversationId = "1",
                otherUserId = "user1",
                otherUserName = "Taylor Swift",
                otherUserProfileImage = "",
                lastMessage = "Thanks for sharing that song!",
                timestamp = System.currentTimeMillis() - 7200000,
                unreadBadge = 2,
                messages = emptyList()
            ),
            Conversation(
                conversationId = "2",
                otherUserId = "user2",
                otherUserName = "Ariana Grande",
                otherUserProfileImage = "",
                lastMessage = "Let's collaborate sometime",
                timestamp = System.currentTimeMillis() - 18000000,
                unreadBadge = 0,
                messages = emptyList()
            ),
            Conversation(
                conversationId = "3",
                otherUserId = "user3",
                otherUserName = "BTS",
                otherUserProfileImage = "",
                lastMessage = "Check out our new album!",
                timestamp = System.currentTimeMillis() - 86400000,
                unreadBadge = 1,
                messages = emptyList()
            ),
            Conversation(
                conversationId = "4",
                otherUserId = "user4",
                otherUserName = "Ed Sheeran",
                otherUserProfileImage = "",
                lastMessage = "See you at the concert!",
                timestamp = System.currentTimeMillis() - 172800000,
                unreadBadge = 0,
                messages = emptyList()
            )
        )
    }
}