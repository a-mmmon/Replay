package com.example.replay

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class DiscoverFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_discover, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.rvDiscover)

        // 🔥 MUST HAVE THESE
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Dummy data (so you SEE something)
        val albums = listOf(
            Album("1", "Album One", "Artist A", ""),
            Album("2", "Album Two", "Artist B", "")
        )

        val artists = listOf(
            Artist("1", "Artist A", ""),
            Artist("2", "Artist B", "")
        )

        val songs = listOf(
            Music("1", "Song One", "Artist A", "Album One", ""),
            Music("2", "Song Two", "Artist B", "Album Two", "")
        )

        recyclerView.adapter = DiscoverAdapter(albums, artists, songs)
    }
}
