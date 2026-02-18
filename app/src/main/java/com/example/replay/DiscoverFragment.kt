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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
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

    // Firebase
    private val database = FirebaseDatabase.getInstance()

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
        // Search results
        searchRecyclerView = view.findViewById(R.id.searchResultsRecycler)
        searchResultsHeader = view.findViewById(R.id.searchResultsHeader)

        // Featured Artists
        featuredArtistsRecycler = view.findViewById(R.id.featuredArtistsRecycler)
        featuredArtistsHeader = view.findViewById(R.id.featuredArtistsHeader)

        // Trending Songs
        trendingSongsRecycler = view.findViewById(R.id.trendingSongsRecycler)
        trendingSongsHeader = view.findViewById(R.id.trendingSongsHeader)

        // Popular Songs
        popularSongsRecycler = view.findViewById(R.id.popularSongsRecycler)
        popularSongsHeader = view.findViewById(R.id.popularSongsHeader)
    }

    private fun setupRecyclerViews() {
        // Search Results (Vertical)
        searchAdapter = DiscoverSongAdapter(searchResults) { song ->
            showMusicBottomSheet(song)
        }
        searchRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        searchRecyclerView.adapter = searchAdapter

        // Featured Artists (Horizontal)
        val artistNames = listOf(
            "Taylor Swift", "Ed Sheeran", "Ariana Grande",
            "The Weeknd", "Billie Eilish", "Drake"
        )
        featuredArtistsAdapter = FeaturedArtistAdapter(artistNames) { artistName ->
            searchSongs(artistName)
        }
        featuredArtistsRecycler.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL,
            false
        )
        featuredArtistsRecycler.adapter = featuredArtistsAdapter

        // Trending Songs (Horizontal)
        trendingSongsAdapter = TrendingSongAdapter(trendingSongs) { song ->
            showMusicBottomSheet(song)
        }
        trendingSongsRecycler.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL,
            false
        )
        trendingSongsRecycler.adapter = trendingSongsAdapter

        // Popular Songs (Horizontal)
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
        // Try loading from Firebase first, then from API
        loadTrendingSongsFromFirebase()
        loadPopularSongsFromFirebase()
    }

    // ✅ NEW: Load trending songs from Firebase (cached from previous searches)
    private fun loadTrendingSongsFromFirebase() {
        database.getReference("trending-songs")
            .limitToFirst(20)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val firebaseSongs = mutableListOf<ITunesSong>()
                    for (child in snapshot.children) {
                        val song = child.getValue(ITunesSong::class.java)
                        song?.let { firebaseSongs.add(it) }
                    }

                    if (firebaseSongs.isNotEmpty()) {
                        trendingSongs.clear()
                        trendingSongs.addAll(firebaseSongs)
                        trendingSongsAdapter.notifyDataSetChanged()
                        Log.d("DiscoverFragment", "Loaded ${firebaseSongs.size} trending songs from Firebase")
                    } else {
                        // No cached songs, fetch from API
                        loadTrendingSongsFromAPI()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("DiscoverFragment", "Firebase error: ${error.message}")
                    loadTrendingSongsFromAPI()
                }
            })
    }

    private fun loadTrendingSongsFromAPI() {
        RetrofitClient.api.searchSongs("trending 2024", limit = 20)
            .enqueue(object : Callback<ITunesResponse> {
                override fun onResponse(
                    call: Call<ITunesResponse>,
                    response: Response<ITunesResponse>
                ) {
                    if (response.isSuccessful) {
                        val results = response.body()?.results ?: emptyList()
                        if (results.isNotEmpty()) {
                            trendingSongs.clear()
                            trendingSongs.addAll(results)
                            trendingSongsAdapter.notifyDataSetChanged()

                            // Cache to Firebase
                            cacheSongsToFirebase("trending-songs", results)
                            Log.d("DiscoverFragment", "Loaded ${results.size} trending songs from API")
                        } else {
                            loadFallbackTrendingSongs()
                        }
                    } else {
                        loadFallbackTrendingSongs()
                    }
                }

                override fun onFailure(call: Call<ITunesResponse>, t: Throwable) {
                    Log.e("DiscoverFragment", "Failed to load trending songs from API", t)
                    loadFallbackTrendingSongs()
                }
            })
    }

    // ✅ NEW: Load popular songs from Firebase
    private fun loadPopularSongsFromFirebase() {
        database.getReference("popular-songs")
            .limitToFirst(20)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val firebaseSongs = mutableListOf<ITunesSong>()
                    for (child in snapshot.children) {
                        val song = child.getValue(ITunesSong::class.java)
                        song?.let { firebaseSongs.add(it) }
                    }

                    if (firebaseSongs.isNotEmpty()) {
                        popularSongs.clear()
                        popularSongs.addAll(firebaseSongs)
                        popularSongsAdapter.notifyDataSetChanged()
                        Log.d("DiscoverFragment", "Loaded ${firebaseSongs.size} popular songs from Firebase")
                    } else {
                        loadPopularSongsFromAPI()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("DiscoverFragment", "Firebase error: ${error.message}")
                    loadPopularSongsFromAPI()
                }
            })
    }

    private fun loadPopularSongsFromAPI() {
        RetrofitClient.api.searchSongs("pop hits 2024", limit = 20)
            .enqueue(object : Callback<ITunesResponse> {
                override fun onResponse(
                    call: Call<ITunesResponse>,
                    response: Response<ITunesResponse>
                ) {
                    if (response.isSuccessful) {
                        val results = response.body()?.results ?: emptyList()
                        if (results.isNotEmpty()) {
                            popularSongs.clear()
                            popularSongs.addAll(results)
                            popularSongsAdapter.notifyDataSetChanged()

                            // Cache to Firebase
                            cacheSongsToFirebase("popular-songs", results)
                            Log.d("DiscoverFragment", "Loaded ${results.size} popular songs from API")
                        } else {
                            loadFallbackPopularSongs()
                        }
                    } else {
                        loadFallbackPopularSongs()
                    }
                }

                override fun onFailure(call: Call<ITunesResponse>, t: Throwable) {
                    Log.e("DiscoverFragment", "Failed to load popular songs from API", t)
                    loadFallbackPopularSongs()
                }
            })
    }

    // ✅ NEW: Cache songs to Firebase for offline access
    private fun cacheSongsToFirebase(category: String, songs: List<ITunesSong>) {
        val ref = database.getReference(category)

        // Clear old cache
        ref.removeValue().addOnSuccessListener {
            // Add new songs
            songs.forEach { song ->
                ref.child(song.trackId.toString()).setValue(song)
            }
            Log.d("DiscoverFragment", "Cached ${songs.size} songs to Firebase/$category")
        }
    }

    private fun loadFallbackTrendingSongs() {
        trendingSongs.clear()
        trendingSongs.addAll(SampleData.sampleSongs)
        trendingSongsAdapter.notifyDataSetChanged()
        Log.d("DiscoverFragment", "Loaded ${trendingSongs.size} trending songs from fallback data")
    }

    private fun loadFallbackPopularSongs() {
        popularSongs.clear()
        popularSongs.addAll(SampleData.sampleSongs.reversed())
        popularSongsAdapter.notifyDataSetChanged()
        Log.d("DiscoverFragment", "Loaded ${popularSongs.size} popular songs from fallback data")
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
                        searchResults.clear()
                        searchResults.addAll(results)
                        searchAdapter.notifyDataSetChanged()

                        // Show search results section
                        searchResultsHeader.visibility = View.VISIBLE
                        searchRecyclerView.visibility = View.VISIBLE

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
                        "Network error: ${t.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    private fun showMusicBottomSheet(song: ITunesSong) {
        val bottomSheet = MusicPlayerBottomSheet(song) { action ->
            when (action) {
                MusicPlayerBottomSheet.Action.ADD_TO_FAVORITES -> addToFavorites(song)
                MusicPlayerBottomSheet.Action.OPEN_SPOTIFY -> openMusicApp("spotify", song)
                MusicPlayerBottomSheet.Action.OPEN_APPLE_MUSIC -> openMusicApp("apple_music", song)
                MusicPlayerBottomSheet.Action.OPEN_YOUTUBE_MUSIC -> openMusicApp("youtube_music", song)
            }
        }
        bottomSheet.show(parentFragmentManager, "MusicPlayerBottomSheet")
    }

    private fun addToFavorites(song: ITunesSong) {
        // Save to local favorites manager
        FavoriteManager.addToFavorites(song)

        // Also save to Firebase for cloud sync
        val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            database.getReference("user-library")
                .child(userId)
                .child("favorites")
                .child(song.trackId.toString())
                .setValue(song)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Added to favorites!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Log.e("DiscoverFragment", "Failed to save to Firebase", e)
                    Toast.makeText(requireContext(), "Added to local favorites", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(requireContext(), "Added to favorites!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openMusicApp(app: String, song: ITunesSong) {
        try {
            val intent = when (app) {
                "spotify" -> {
                    android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                        data = android.net.Uri.parse("https://open.spotify.com/search/${song.trackName} ${song.artistName}")
                    }
                }
                "apple_music" -> {
                    android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                        data = android.net.Uri.parse(song.trackViewUrl)
                    }
                }
                "youtube_music" -> {
                    android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                        data = android.net.Uri.parse("https://music.youtube.com/search?q=${song.trackName} ${song.artistName}")
                    }
                }
                else -> return
            }
            startActivity(intent)
        } catch (e: Exception) {
            Log.e("DiscoverFragment", "Error opening music app", e)
            Toast.makeText(requireContext(), "Could not open app", Toast.LENGTH_SHORT).show()
        }
    }
}