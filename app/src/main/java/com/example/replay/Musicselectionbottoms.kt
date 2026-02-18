package com.example.replay

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MusicSelectionBottomSheet : BottomSheetDialogFragment() {

    private var onMusicSelectedListener: ((ITunesSong) -> Unit)? = null

    // NEW: UI Elements
    private lateinit var etSearchSong: EditText
    private lateinit var ivClearSearch: ImageView
    private lateinit var tabLayout: TabLayout
    private lateinit var rvMusic: RecyclerView
    private lateinit var llEmptyState: LinearLayout
    private lateinit var tvEmptyMessage: TextView

    // NEW: Data Lists
    private val allSongs = mutableListOf<ITunesSong>()
    private val favoriteSongs = mutableListOf<ITunesSong>()
    private val recentSongs = mutableListOf<ITunesSong>()
    private val displayedSongs = mutableListOf<ITunesSong>()
    private val searchResults = mutableListOf<ITunesSong>()

    private lateinit var adapter: MusicSelectionAdapter
    private var searchJob: Job? = null
    private var isSearching = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.bottom_sheet_music_selection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        rvMusic = view.findViewById(R.id.rvMusicList)
        val tvTitle = view.findViewById<TextView>(R.id.tvMusicSelectionTitle)
        etSearchSong = view.findViewById(R.id.etSearchSong)
        ivClearSearch = view.findViewById(R.id.ivClearSearch)
        tabLayout = view.findViewById(R.id.tabLayout)
        llEmptyState = view.findViewById(R.id.llEmptyState)
        tvEmptyMessage = view.findViewById(R.id.tvEmptyMessage)

        tvTitle.text = "Select Music"

        // Load data
        loadAllSongs()
        loadFavoriteSongs()
        loadRecentSongs()

        // Initially show favorites
        displayedSongs.addAll(favoriteSongs)

        // Setup
        setupRecyclerView()
        setupSearchBar()
        setupTabs()
        updateEmptyState()
    }

    private fun loadAllSongs() {
        // Load all songs from SampleData
        allSongs.clear()
        allSongs.addAll(SampleData.sampleSongs)

        // TODO: If you want to add songs from iTunes API or other sources, add them here
        // Example: allSongs.addAll(songsFromAPI)
    }

    private fun loadFavoriteSongs() {
        // Load favorites from SharedPreferences (same way as LibraryFragment)
        try {
            val prefs = requireContext().getSharedPreferences("favorites", android.content.Context.MODE_PRIVATE)
            val favoritesSet = prefs.getStringSet("favorite_songs", emptySet()) ?: emptySet()

            favoriteSongs.clear()

            for (songData in favoritesSet) {
                val parts = songData.split("|")
                if (parts.size >= 4) {
                    val song = ITunesSong(
                        trackId = parts[0].toLongOrNull() ?: 0L,
                        trackName = parts[1],
                        artistName = parts[2],
                        artworkUrl100 = parts[3],
                        previewUrl = "",
                        collectionName = "",
                        trackViewUrl = "",
                        releaseDate = ""
                    )
                    favoriteSongs.add(song)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadRecentSongs() {
        // Load recent songs from SharedPreferences
        try {
            val prefs = requireContext().getSharedPreferences("recent_songs", android.content.Context.MODE_PRIVATE)
            val recentSet = prefs.getStringSet("recent_songs_list", emptySet()) ?: emptySet()

            recentSongs.clear()

            if (recentSet.isEmpty()) {
                // If no recent songs, show sample songs as default
                recentSongs.addAll(SampleData.sampleSongs.take(3))
            } else {
                // Convert Set to List to maintain order
                val recentList = recentSet.toList().takeLast(10) // Last 10 recent songs

                for (songData in recentList) {
                    val parts = songData.split("|")
                    if (parts.size >= 4) {
                        val song = ITunesSong(
                            trackId = parts[0].toLongOrNull() ?: 0L,
                            trackName = parts[1],
                            artistName = parts[2],
                            artworkUrl100 = parts[3],
                            previewUrl = "",
                            collectionName = "",
                            trackViewUrl = "",
                            releaseDate = ""
                        )
                        recentSongs.add(song)
                    }
                }

                // Reverse to show most recent first
                recentSongs.reverse()
            }

        } catch (e: Exception) {
            e.printStackTrace()
            // On error, show sample songs
            recentSongs.addAll(SampleData.sampleSongs.take(3))
        }
    }

    private fun setupRecyclerView() {
        adapter = MusicSelectionAdapter(displayedSongs) { music ->
            // Save as recent song when selected
            saveRecentSong(music)

            onMusicSelectedListener?.invoke(music)
            dismiss()
        }

        rvMusic.layoutManager = LinearLayoutManager(requireContext())
        rvMusic.adapter = adapter
    }

    private fun setupSearchBar() {
        etSearchSong.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                ivClearSearch.visibility = if (s.isNullOrEmpty()) View.GONE else View.VISIBLE
                filterSongs(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        ivClearSearch.setOnClickListener {
            etSearchSong.text.clear()
        }
    }

    private fun setupTabs() {
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> showFavoriteSongs()
                    1 -> showRecentSongs()
                    2 -> showAllSongs()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun showFavoriteSongs() {
        displayedSongs.clear()
        displayedSongs.addAll(favoriteSongs)
        adapter.notifyDataSetChanged()
        updateEmptyState()
        etSearchSong.text.clear()
    }

    private fun showRecentSongs() {
        displayedSongs.clear()
        displayedSongs.addAll(recentSongs)
        adapter.notifyDataSetChanged()
        updateEmptyState()
        etSearchSong.text.clear()
    }

    private fun showAllSongs() {
        displayedSongs.clear()
        displayedSongs.addAll(allSongs)
        adapter.notifyDataSetChanged()
        updateEmptyState()
        etSearchSong.text.clear()
    }

    private fun filterSongs(query: String) {
        // Cancel previous search
        searchJob?.cancel()

        if (query.isEmpty()) {
            isSearching = false
            // Restore based on current tab
            when (tabLayout.selectedTabPosition) {
                0 -> showFavoriteSongs()
                1 -> showRecentSongs()
                2 -> showAllSongs()
            }
            return
        }

        // First, search in current tab locally
        val currentList = when (tabLayout.selectedTabPosition) {
            0 -> favoriteSongs
            1 -> recentSongs
            else -> allSongs
        }

        val filtered = currentList.filter { song ->
            song.trackName.contains(query, ignoreCase = true) ||
                    song.artistName.contains(query, ignoreCase = true)
        }

        displayedSongs.clear()
        displayedSongs.addAll(filtered)
        adapter.notifyDataSetChanged()

        // If no local results and query is long enough, search iTunes API
        if (filtered.isEmpty() && query.length >= 3) {
            isSearching = true
            tvEmptyMessage.text = "Searching..."
            llEmptyState.visibility = View.VISIBLE
            rvMusic.visibility = View.GONE

            searchJob = lifecycleScope.launch {
                delay(500) // Debounce
                searchITunes(query)
            }
        } else {
            isSearching = false
            updateEmptyState()
        }
    }

    private suspend fun searchITunes(query: String) {
        try {
            val service = RetrofitClient.api
            val response = service.searchSongsAsync(query, limit = 20)

            if (response.isSuccessful) {
                val songs = response.body()?.results ?: emptyList()

                searchResults.clear()
                searchResults.addAll(songs)

                displayedSongs.clear()
                displayedSongs.addAll(songs)

                requireActivity().runOnUiThread {
                    adapter.notifyDataSetChanged()
                    isSearching = false
                    updateEmptyState()
                }
            } else {
                requireActivity().runOnUiThread {
                    isSearching = false
                    updateEmptyState()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            requireActivity().runOnUiThread {
                isSearching = false
                updateEmptyState()
            }
        }
    }

    private fun updateEmptyState() {
        if (displayedSongs.isEmpty()) {
            rvMusic.visibility = View.GONE
            llEmptyState.visibility = View.VISIBLE

            tvEmptyMessage.text = when {
                isSearching -> "Searching iTunes..."
                etSearchSong.text.isNotEmpty() -> "No songs found\nTry a different search"
                tabLayout.selectedTabPosition == 0 -> "No favorite songs yet\nAdd favorites from Discover!"
                tabLayout.selectedTabPosition == 1 -> "No recent songs\nSelect a song to get started!"
                else -> "No songs available"
            }
        } else {
            rvMusic.visibility = View.VISIBLE
            llEmptyState.visibility = View.GONE
        }
    }

    private fun saveRecentSong(song: ITunesSong) {
        try {
            val prefs = requireContext().getSharedPreferences("recent_songs", android.content.Context.MODE_PRIVATE)
            val recentSet = prefs.getStringSet("recent_songs_list", mutableSetOf())?.toMutableSet() ?: mutableSetOf()

            val songData = "${song.trackId}|${song.trackName}|${song.artistName}|${song.artworkUrl100}"

            // Add to recent (will keep last 20)
            recentSet.add(songData)

            // Keep only last 20 songs
            if (recentSet.size > 20) {
                val list = recentSet.toMutableList()
                recentSet.clear()
                recentSet.addAll(list.takeLast(20))
            }

            prefs.edit().putStringSet("recent_songs_list", recentSet).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setOnMusicSelectedListener(listener: (ITunesSong) -> Unit) {
        onMusicSelectedListener = listener
    }
}

// Keep your existing adapter - NO CHANGES NEEDED
class MusicSelectionAdapter(
    private val musicList: List<ITunesSong>,
    private val onMusicClick: (ITunesSong) -> Unit
) : RecyclerView.Adapter<MusicSelectionAdapter.MusicViewHolder>() {

    inner class MusicViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvMusicTitle)
        val tvArtist: TextView = view.findViewById(R.id.tvMusicArtist)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_music_selection, parent, false)
        return MusicViewHolder(view)
    }

    override fun onBindViewHolder(holder: MusicViewHolder, position: Int) {
        val music = musicList[position]
        holder.tvTitle.text = music.trackName
        holder.tvArtist.text = music.artistName

        holder.itemView.setOnClickListener {
            onMusicClick(music)
        }
    }

    override fun getItemCount() = musicList.size
}