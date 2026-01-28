package com.example.replay

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MessagesFragment : Fragment(R.layout.fragment_messages) {

    private lateinit var messagesAdapter: ConversationsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.rvConversations)

        // Sample conversations data
        messagesAdapter = ConversationsAdapter(SampleData.conversations) { conversation ->
            // TODO: Navigate to chat detail screen
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = messagesAdapter
    }
}