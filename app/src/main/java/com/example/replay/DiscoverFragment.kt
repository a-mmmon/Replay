package com.example.replay

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DiscoverFragment : Fragment() {

    // Search results
    private lateinit var searchRecyclerView: RecyclerView
    private lateinit var searchAdapter: DiscoverSongAdapter
    private val searchResults = mutableListOf<ITunesSong>()

    // Featured Artists
    private lateinit var featuredArtistsRecycler: RecyclerView
    private lateinit var featuredArtistsAdapter: FeaturedArtistAdapter
    private val featuredArtists = mutableListOf<Artist>()

    // Trending Songs
    private lateinit var trendingSongsRecycler: RecyclerView
    private lateinit var trendingSongsAdapter: TrendingSongAdapter
    private val trendingSongs = mutableListOf<ITunesSong>()

    // Popular Songs
    private lateinit var popularSongsRecycler: RecyclerView
    private lateinit var popularSongsAdapter: PopularSongAdapter
    private val popularSongs = mutableListOf<ITunesSong>()

    // Section headers
    private lateinit var searchResultsHeader: TextView
    private lateinit var featuredArtistsHeader: TextView
    private lateinit var trendingSongsHeader: TextView
    private lateinit var popularSongsHeader: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_discover, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)
        setupRecyclerViews()
        setupSearchView(view)
        loadInitialContent()
    }

    private fun initializeViews(view: View) {
        searchRecyclerView = view.findViewById(R.id.searchResultsRecycler)
        searchResultsHeader = view.findViewById(R.id.searchResultsHeader)

        featuredArtistsRecycler = view.findViewById(R.id.featuredArtistsRecycler)
        featuredArtistsHeader = view.findViewById(R.id.featuredArtistsHeader)

        trendingSongsRecycler = view.findViewById(R.id.trendingSongsRecycler)
        trendingSongsHeader = view.findViewById(R.id.trendingSongsHeader)

        popularSongsRecycler = view.findViewById(R.id.popularSongsRecycler)
        popularSongsHeader = view.findViewById(R.id.popularSongsHeader)
    }

    private fun setupRecyclerViews() {

        // Search Results
        searchAdapter = DiscoverSongAdapter(searchResults) { song ->
            showMusicBottomSheet(song)
        }
        searchRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        searchRecyclerView.adapter = searchAdapter

        // Featured Artists
        featuredArtistsAdapter = FeaturedArtistAdapter(featuredArtists) { artistName ->
            searchSongs(artistName)
        }
        featuredArtistsRecycler.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL,
            false
        )
        featuredArtistsRecycler.adapter = featuredArtistsAdapter

        // Trending Songs
        trendingSongsAdapter = TrendingSongAdapter(trendingSongs) { song ->
            showMusicBottomSheet(song)
        }
        trendingSongsRecycler.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL,
            false
        )
        trendingSongsRecycler.adapter = trendingSongsAdapter

        // Popular Songs
        popularSongsAdapter = PopularSongAdapter(popularSongs) { song ->
            showMusicBottomSheet(song)
        }
        popularSongsRecycler.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL,
            false
        )
        popularSongsRecycler.adapter = popularSongsAdapter
    }

    private fun setupSearchView(view: View) {
        val searchView = view.findViewById<SearchView>(R.id.searchView)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrBlank()) {
                    searchSongs(query)
                }
                searchView.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrEmpty()) {
                    searchResultsHeader.visibility = View.GONE
                    searchRecyclerView.visibility = View.GONE
                }
                return false
            }
        })
    }

    private fun loadInitialContent() {
        loadFeaturedArtists()
        loadTrendingSongs()
        loadPopularSongs()
    }

    // ✅ NEW: Load Featured Artists with real images
    private fun loadFeaturedArtists() {

        val artistNames = listOf(
            "Taylor Swift",
            "Ed Sheeran",
            "Ariana Grande",
            "The Weeknd",
            "Billie Eilish",
            "Drake"
        )

        for (name in artistNames) {
            RetrofitClient.api.searchSongs(name, limit = 1)
                .enqueue(object : Callback<ITunesResponse> {
                    override fun onResponse(
                        call: Call<ITunesResponse>,
                        response: Response<ITunesResponse>
                    ) {
                        if (response.isSuccessful) {
                            val result = response.body()?.results?.firstOrNull()
                            if (result != null) {
                                featuredArtists.add(
                                    Artist(
                                        name = result.artistName,
                                        imageUrl = result.artworkUrl100
                                    )
                                )
                                featuredArtistsAdapter.notifyDataSetChanged()
                            }
                        }
                    }

                    override fun onFailure(call: Call<ITunesResponse>, t: Throwable) {
                        Log.e("DiscoverFragment", "Failed to load artist image", t)
                    }
                })
        }
    }

    private fun loadTrendingSongs() {
        RetrofitClient.api.searchSongs("trending 2024", limit = 20)
            .enqueue(object : Callback<ITunesResponse> {
                override fun onResponse(
                    call: Call<ITunesResponse>,
                    response: Response<ITunesResponse>
                ) {
                    val results = response.body()?.results ?: emptyList()
                    trendingSongs.clear()
                    trendingSongs.addAll(results)
                    trendingSongsAdapter.notifyDataSetChanged()
                }

                override fun onFailure(call: Call<ITunesResponse>, t: Throwable) {
                    Log.e("DiscoverFragment", "Failed to load trending songs", t)
                }
            })
    }

    private fun loadPopularSongs() {
        RetrofitClient.api.searchSongs("pop hits 2024", limit = 20)
            .enqueue(object : Callback<ITunesResponse> {
                override fun onResponse(
                    call: Call<ITunesResponse>,
                    response: Response<ITunesResponse>
                ) {
                    val results = response.body()?.results ?: emptyList()
                    popularSongs.clear()
                    popularSongs.addAll(results)
                    popularSongsAdapter.notifyDataSetChanged()
                }

                override fun onFailure(call: Call<ITunesResponse>, t: Throwable) {
                    Log.e("DiscoverFragment", "Failed to load popular songs", t)
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
                    val results = response.body()?.results ?: emptyList()
                    searchResults.clear()
                    searchResults.addAll(results)
                    searchAdapter.notifyDataSetChanged()

                    searchResultsHeader.visibility = View.VISIBLE
                    searchRecyclerView.visibility = View.VISIBLE

                    Toast.makeText(
                        requireContext(),
                        "Found ${results.size} songs",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onFailure(call: Call<ITunesResponse>, t: Throwable) {
                    Toast.makeText(
                        requireContext(),
                        "Network error",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    private fun showMusicBottomSheet(song: ITunesSong) {
        val bottomSheet = MusicPlayerBottomSheet(song) {}
        bottomSheet.show(parentFragmentManager, "MusicPlayerBottomSheet")
    }
}
