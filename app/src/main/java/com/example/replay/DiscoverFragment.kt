package com.example.replay

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DiscoverFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DiscoverAdapter
    private val songList = mutableListOf<Music>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_discover, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.rvDiscover)
        val searchView = view.findViewById<SearchView>(R.id.searchView)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // ✅ USE THE CORRECT ADAPTER
        adapter = DiscoverAdapter(songList)
        recyclerView.adapter = adapter

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrBlank()) {
                    searchSongs(query)
                }
                searchView.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
    }

    private fun searchSongs(query: String) {
        RetrofitClient.api.searchSongs(query)
            .enqueue(object : Callback<iTunesResponse> {

                override fun onResponse(
                    call: Call<iTunesResponse>,
                    response: Response<iTunesResponse>
                ) {
                    if (response.isSuccessful) {
                        val results = response.body()?.results ?: emptyList()

                        val mappedSongs = results.map {
                            Music(
                                id = it.trackId.toString(),
                                title = it.trackName,
                                artist = it.artistName,
                                album = it.collectionName ?: "Unknown Album",
                                coverUrl = it.artworkUrl100 ?: ""
                            )
                        }

                        songList.clear()
                        songList.addAll(mappedSongs)
                        adapter.notifyDataSetChanged()

                        Toast.makeText(
                            requireContext(),
                            "Found ${mappedSongs.size} songs",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Failed to load songs",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<iTunesResponse>, t: Throwable) {
                    t.printStackTrace()
                    Toast.makeText(
                        requireContext(),
                        t.localizedMessage ?: "Network error",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }
}

