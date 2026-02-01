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
    private val songList = mutableListOf<ITunesSong>()

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
            .enqueue(object : Callback<ITunesResponse> {

                override fun onResponse(
                    call: Call<ITunesResponse>,
                    response: Response<ITunesResponse>
                ) {
                    if (response.isSuccessful) {
                        val results = response.body()?.results ?: emptyList()

                        songList.clear()
                        songList.addAll(results)
                        adapter.notifyDataSetChanged()

                        Toast.makeText(
                            requireContext(),
                            "Found ${results.size} songs",
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

                override fun onFailure(call: Call<ITunesResponse>, t: Throwable) {
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