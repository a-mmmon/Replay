package com.example.replay

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var feedAdapter: FeedAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.rvFeed)

        feedAdapter = FeedAdapter(SampleData.posts) { post ->
            // Optional: handle post click
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = feedAdapter
    }
}
