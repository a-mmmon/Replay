package com.example.replay

import android.content.Intent
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
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DiscoverFragment : Fragment() {

    private lateinit var searchRecyclerView: RecyclerView
    private lateinit var searchAdapter: DiscoverSongAdapter
    private val searchResults = mutableListOf<ITunesSong>()

    private lateinit var userSearchRecycler: RecyclerView
    private lateinit var userSearchAdapter: UsersAdapter
    private val userResults = mutableListOf<UserProfile>()

    private lateinit var featuredArtistsRecycler: RecyclerView
    private lateinit var featuredArtistsAdapter: FeaturedArtistAdapter

    private lateinit var trendingSongsRecycler: RecyclerView
    private lateinit var trendingSongsAdapter: TrendingSongAdapter
    private val trendingSongs = mutableListOf<ITunesSong>()

    private val featuredArtists = mutableListOf<ITunesSong>()


    private lateinit var popularSongsRecycler: RecyclerView
    private lateinit var popularSongsAdapter: PopularSongAdapter
    private val popularSongs = mutableListOf<ITunesSong>()

    private lateinit var searchResultsHeader: TextView
    private lateinit var featuredArtistsHeader: TextView
    private lateinit var trendingSongsHeader: TextView
    private lateinit var popularSongsHeader: TextView
    private lateinit var filterTabLayout: TabLayout

    private val database = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var currentFilter = "Songs"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_discover, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeViews(view)
        setupRecyclerViews()
        setupFilterTabs()
        setupSearchView(view)
        loadInitialContent()
    }

    private fun initializeViews(view: View) {
        searchRecyclerView = view.findViewById(R.id.searchResultsRecycler)
        searchResultsHeader = view.findViewById(R.id.searchResultsHeader)
        userSearchRecycler = view.findViewById(R.id.userSearchRecycler)
        featuredArtistsRecycler = view.findViewById(R.id.featuredArtistsRecycler)
        featuredArtistsHeader = view.findViewById(R.id.featuredArtistsHeader)
        trendingSongsRecycler = view.findViewById(R.id.trendingSongsRecycler)
        trendingSongsHeader = view.findViewById(R.id.trendingSongsHeader)
        popularSongsRecycler = view.findViewById(R.id.popularSongsRecycler)
        popularSongsHeader = view.findViewById(R.id.popularSongsHeader)
        filterTabLayout = view.findViewById(R.id.filterTabLayout)
    }

    private fun setupFilterTabs() {
        listOf("Songs", "Artists", "Playlists", "Profiles").forEach { label ->
            filterTabLayout.addTab(filterTabLayout.newTab().setText(label))
        }

        filterTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentFilter = tab?.text.toString()
                val query = view?.findViewById<SearchView>(R.id.searchView)?.query?.toString() ?: ""
                if (query.isNotBlank()) performSearch(query)
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupRecyclerViews() {
        searchAdapter = DiscoverSongAdapter(searchResults) { song -> showMusicBottomSheet(song) }
        searchRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        searchRecyclerView.adapter = searchAdapter

        userSearchAdapter = UsersAdapter(userResults) { user ->
            val intent = Intent(requireContext(), UserProfileActivity::class.java)
            intent.putExtra("user_id", user.userId)
            intent.putExtra("username", user.username)
            startActivity(intent)
        }
        userSearchRecycler.layoutManager = LinearLayoutManager(requireContext())
        userSearchRecycler.adapter = userSearchAdapter

        featuredArtistsAdapter = FeaturedArtistAdapter(featuredArtists) { artistName ->
            searchSongs(artistName, isArtistSearch = true)
        }

        featuredArtistsRecycler.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        featuredArtistsRecycler.adapter = featuredArtistsAdapter

        featuredArtistsRecycler.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        featuredArtistsRecycler.adapter = featuredArtistsAdapter

        trendingSongsAdapter = TrendingSongAdapter(trendingSongs) { song -> showMusicBottomSheet(song) }
        trendingSongsRecycler.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        trendingSongsRecycler.adapter = trendingSongsAdapter

        popularSongsAdapter = PopularSongAdapter(popularSongs) { song -> showMusicBottomSheet(song) }
        popularSongsRecycler.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        popularSongsRecycler.adapter = popularSongsAdapter
    }

    private fun loadFeaturedArtists() {

        val artistQueries = listOf(
            "Taylor Swift",
            "Ed Sheeran",
            "Ariana Grande",
            "The Weeknd",
            "Billie Eilish",
            "Drake"
        )

        featuredArtists.clear()

        for (name in artistQueries) {
            RetrofitClient.api.searchSongs(name, limit = 1)
                .enqueue(object : Callback<ITunesResponse> {

                    override fun onResponse(
                        call: Call<ITunesResponse>,
                        response: Response<ITunesResponse>
                    ) {
                        if (!isAdded) return

                        if (response.isSuccessful) {
                            val result = response.body()?.results?.firstOrNull()
                            if (result != null) {
                                featuredArtists.add(result)
                                featuredArtistsAdapter.notifyDataSetChanged()
                            }
                        }
                    }

                    override fun onFailure(call: Call<ITunesResponse>, t: Throwable) {
                        Log.e("DiscoverFragment", "Failed to load artist", t)
                    }
                })
        }
    }



    private fun setupSearchView(view: View) {
        val searchView = view.findViewById<SearchView>(R.id.searchView)
        searchView.queryHint = "Search songs, artists, people..."

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrBlank()) performSearch(query)
                searchView.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrEmpty()) {
                    filterTabLayout.visibility = View.GONE
                    searchResultsHeader.visibility = View.GONE
                    searchRecyclerView.visibility = View.GONE
                    userSearchRecycler.visibility = View.GONE
                    showBrowseContent(true)
                } else {
                    filterTabLayout.visibility = View.VISIBLE
                    showBrowseContent(false)
                    if (newText.length >= 2) performSearch(newText)
                }
                return false
            }
        })
    }

    private fun performSearch(query: String) {
        when (currentFilter) {
            "Profiles" -> searchUsers(query)
            "Artists" -> searchSongs(query, isArtistSearch = true)
            else -> searchSongs(query)
        }
    }

    private fun searchUsers(query: String) {
        searchRecyclerView.visibility = View.GONE
        userSearchRecycler.visibility = View.VISIBLE
        searchResultsHeader.visibility = View.VISIBLE
        searchResultsHeader.text = "People"

        val currentUserId = auth.currentUser?.uid ?: ""

        database.getReference("users")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!isAdded) return
                    userResults.clear()
                    val normalizedQuery = query.trim().lowercase().removePrefix("@")

                    for (child in snapshot.children) {
                        val user = child.getValue(UserProfile::class.java) ?: continue
                        if (user.userId.isBlank() || user.userId == currentUserId) continue

                        val username = user.username.trim().lowercase()
                        val handle = user.handle.trim().lowercase().removePrefix("@")
                        val email = user.email.trim().lowercase()

                        val matches =
                            username.contains(normalizedQuery) ||
                            handle.contains(normalizedQuery) ||
                            email.contains(normalizedQuery)

                        if (matches) {
                            userResults.add(user)
                        }
                    }
                    userResults.sortBy { it.username.lowercase() }
                    userSearchAdapter.updateUsers(userResults)
                    searchResultsHeader.text = "People (${userResults.size})"
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e("DiscoverFragment", "User search failed: ${error.message}")
                }
            })
    }

    private fun searchSongs(query: String, isArtistSearch: Boolean = false) {
        userSearchRecycler.visibility = View.GONE
        searchRecyclerView.visibility = View.VISIBLE
        searchResultsHeader.visibility = View.VISIBLE
        searchResultsHeader.text = if (isArtistSearch) "Artists" else "Songs"

        RetrofitClient.api.searchSongs(query)
            .enqueue(object : Callback<ITunesResponse> {
                override fun onResponse(call: Call<ITunesResponse>, response: Response<ITunesResponse>) {
                    if (!isAdded) return
                    if (response.isSuccessful) {
                        val results = response.body()?.results ?: emptyList()
                        searchResults.clear()
                        searchResults.addAll(results)
                        searchAdapter.notifyDataSetChanged()
                        searchResultsHeader.text = "${if (isArtistSearch) "Artists" else "Songs"} (${results.size})"
                    }
                }
                override fun onFailure(call: Call<ITunesResponse>, t: Throwable) {
                    if (!isAdded) return
                    Toast.makeText(requireContext(), "Network error: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun showBrowseContent(show: Boolean) {
        val visibility = if (show) View.VISIBLE else View.GONE
        featuredArtistsHeader.visibility = visibility
        featuredArtistsRecycler.visibility = visibility
        trendingSongsHeader.visibility = visibility
        trendingSongsRecycler.visibility = visibility
        popularSongsHeader.visibility = visibility
        popularSongsRecycler.visibility = visibility
    }

    private fun loadInitialContent() {
        loadTrendingSongsFromFirebase()
        loadPopularSongsFromFirebase()
        loadFeaturedArtists()
    }

    private fun loadTrendingSongsFromFirebase() {
        database.getReference("trending-songs").limitToFirst(20)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val songs = snapshot.children.mapNotNull { it.getValue(ITunesSong::class.java) }
                    if (songs.isNotEmpty()) {
                        trendingSongs.clear()
                        trendingSongs.addAll(songs)
                        trendingSongsAdapter.notifyDataSetChanged()
                    } else loadTrendingSongsFromAPI()
                }
                override fun onCancelled(error: DatabaseError) { loadTrendingSongsFromAPI() }
            })
    }

    private fun loadTrendingSongsFromAPI() {
        RetrofitClient.api.searchSongs("trending 2024", limit = 20).enqueue(object : Callback<ITunesResponse> {
            override fun onResponse(call: Call<ITunesResponse>, response: Response<ITunesResponse>) {
                val results = response.body()?.results ?: return
                if (results.isNotEmpty()) {
                    trendingSongs.clear()
                    trendingSongs.addAll(results)
                    trendingSongsAdapter.notifyDataSetChanged()
                    cacheSongsToFirebase("trending-songs", results)
                } else loadFallbackTrendingSongs()
            }
            override fun onFailure(call: Call<ITunesResponse>, t: Throwable) { loadFallbackTrendingSongs() }
        })
    }

    private fun loadPopularSongsFromFirebase() {
        database.getReference("popular-songs").limitToFirst(20)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val songs = snapshot.children.mapNotNull { it.getValue(ITunesSong::class.java) }
                    if (songs.isNotEmpty()) {
                        popularSongs.clear()
                        popularSongs.addAll(songs)
                        popularSongsAdapter.notifyDataSetChanged()
                    } else loadPopularSongsFromAPI()
                }
                override fun onCancelled(error: DatabaseError) { loadPopularSongsFromAPI() }
            })
    }

    private fun loadPopularSongsFromAPI() {
        RetrofitClient.api.searchSongs("pop hits 2024", limit = 20).enqueue(object : Callback<ITunesResponse> {
            override fun onResponse(call: Call<ITunesResponse>, response: Response<ITunesResponse>) {
                val results = response.body()?.results ?: return
                if (results.isNotEmpty()) {
                    popularSongs.clear()
                    popularSongs.addAll(results)
                    popularSongsAdapter.notifyDataSetChanged()
                    cacheSongsToFirebase("popular-songs", results)
                } else loadFallbackPopularSongs()
            }
            override fun onFailure(call: Call<ITunesResponse>, t: Throwable) { loadFallbackPopularSongs() }
        })
    }

    private fun cacheSongsToFirebase(category: String, songs: List<ITunesSong>) {
        val ref = database.getReference(category)
        ref.removeValue().addOnSuccessListener {
            songs.forEach { ref.child(it.trackId.toString()).setValue(it) }
        }
    }

    private fun loadFallbackTrendingSongs() {
        trendingSongs.clear()
        trendingSongs.addAll(SampleData.sampleSongs)
        trendingSongsAdapter.notifyDataSetChanged()
    }

    private fun loadFallbackPopularSongs() {
        popularSongs.clear()
        popularSongs.addAll(SampleData.sampleSongs.reversed())
        popularSongsAdapter.notifyDataSetChanged()
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
        FavoriteManager.addToFavorites(song)
        val userId = auth.currentUser?.uid
        if (userId != null) {
            database.getReference("user-library").child(userId).child("favorites")
                .child(song.trackId.toString()).setValue(song)
                .addOnSuccessListener { Toast.makeText(requireContext(), "Added to favorites!", Toast.LENGTH_SHORT).show() }
                .addOnFailureListener { Toast.makeText(requireContext(), "Added locally", Toast.LENGTH_SHORT).show() }
        } else {
            Toast.makeText(requireContext(), "Added to favorites!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openMusicApp(app: String, song: ITunesSong) {
        try {
            val uri = when (app) {
                "spotify" -> "https://open.spotify.com/search/${song.trackName} ${song.artistName}"
                "apple_music" -> song.trackViewUrl
                "youtube_music" -> "https://music.youtube.com/search?q=${song.trackName} ${song.artistName}"
                else -> return
            }
            startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                data = android.net.Uri.parse(uri)
            })
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Could not open app", Toast.LENGTH_SHORT).show()
        }
    }
}
