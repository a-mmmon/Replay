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
import com.bumptech.glide.Glide
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MusicSelectionBottomSheet : BottomSheetDialogFragment() {

    private var onMusicSelectedListener: ((ITunesSong) -> Unit)? = null

    private lateinit var etSearchSong: EditText
    private lateinit var ivClearSearch: ImageView
    private lateinit var tabLayout: TabLayout
    private lateinit var rvMusic: RecyclerView
    private lateinit var llEmptyState: LinearLayout
    private lateinit var tvEmptyMessage: TextView

    private val allSongs = mutableListOf<ITunesSong>()
    private val favoriteSongs = mutableListOf<ITunesSong>()
    private val recentSongs = mutableListOf<ITunesSong>()
    private val displayedSongs = mutableListOf<ITunesSong>()

    private lateinit var adapter: MusicSelectionAdapter
    private var searchJob: Job? = null
    private var isSearching = false
    private var allSongsLoaded = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.bottom_sheet_music_selection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvMusic        = view.findViewById(R.id.rvMusicList)
        etSearchSong   = view.findViewById(R.id.etSearchSong)
        ivClearSearch  = view.findViewById(R.id.ivClearSearch)
        tabLayout      = view.findViewById(R.id.tabLayout)
        llEmptyState   = view.findViewById(R.id.llEmptyState)
        tvEmptyMessage = view.findViewById(R.id.tvEmptyMessage)

        view.findViewById<TextView>(R.id.tvMusicSelectionTitle).text = "Select Music"

        loadFavoriteSongs()
        loadRecentSongs()

        // Start on Favorites tab
        displayedSongs.addAll(favoriteSongs)

        setupRecyclerView()
        setupSearchBar()
        setupTabs()
        updateEmptyState()

        // Pre-load All Songs from iTunes in background
        loadAllSongsFromItunes()
    }

    // ─── Load a rich list of songs from iTunes API ────────────────────────────
    private fun loadAllSongsFromItunes() {
        allSongs.clear()

        val queries = listOf(
            "top hits 2024",
            "pop 2024",
            "trending music",
            "best songs 2023",
            "hits billboard"
        )

        var completedRequests = 0
        val totalRequests = queries.size

        for (query in queries) {
            RetrofitClient.api.searchSongs(query, limit = 20)
                .enqueue(object : Callback<ITunesResponse> {
                    override fun onResponse(
                        call: Call<ITunesResponse>,
                        response: Response<ITunesResponse>
                    ) {
                        completedRequests++
                        if (!isAdded) return

                        if (response.isSuccessful) {
                            val results = response.body()?.results ?: emptyList()
                            // Add unique songs with real artwork only.
                            val existingIds = allSongs.map { it.trackId }.toSet()
                            val newSongs = results.filter { song ->
                                song.trackId !in existingIds &&
                                    song.artworkUrl100.isNotBlank() &&
                                    !song.artworkUrl100.contains("example.com", ignoreCase = true)
                            }
                            allSongs.addAll(newSongs)
                        }

                        // When all requests done, mark as loaded
                        if (completedRequests == totalRequests) {
                            loadFallbackAllSongsIfNeeded()
                        }
                    }

                    override fun onFailure(call: Call<ITunesResponse>, t: Throwable) {
                        completedRequests++
                        if (completedRequests == totalRequests && isAdded) {
                            loadFallbackAllSongsIfNeeded()
                        }
                    }
                })
        }
    }

    private fun loadFallbackAllSongsIfNeeded() {
        if (allSongs.isNotEmpty()) {
            allSongsLoaded = true
            if (tabLayout.selectedTabPosition == 2) showAllSongs()
            return
        }

        RetrofitClient.api.searchSongs("popular songs", limit = 40)
            .enqueue(object : Callback<ITunesResponse> {
                override fun onResponse(call: Call<ITunesResponse>, response: Response<ITunesResponse>) {
                    if (!isAdded) return
                    if (response.isSuccessful) {
                        val fallback = response.body()?.results.orEmpty().filter {
                            it.artworkUrl100.isNotBlank() &&
                                !it.artworkUrl100.contains("example.com", ignoreCase = true)
                        }
                        allSongs.clear()
                        allSongs.addAll(fallback.distinctBy { it.trackId })
                    }
                    allSongsLoaded = true
                    if (tabLayout.selectedTabPosition == 2) showAllSongs()
                }

                override fun onFailure(call: Call<ITunesResponse>, t: Throwable) {
                    if (!isAdded) return
                    allSongsLoaded = true
                    if (tabLayout.selectedTabPosition == 2) showAllSongs()
                }
            })
    }

    private fun loadFavoriteSongs() {
        try {
            val prefs = requireContext().getSharedPreferences("favorites", android.content.Context.MODE_PRIVATE)
            val favoritesSet = prefs.getStringSet("favorite_songs", emptySet()) ?: emptySet()
            favoriteSongs.clear()
            for (songData in favoritesSet) {
                val parts = songData.split("|")
                if (parts.size >= 4) {
                    favoriteSongs.add(ITunesSong(
                        trackId = parts[0].toLongOrNull() ?: 0L,
                        trackName = parts[1],
                        artistName = parts[2],
                        artworkUrl100 = parts[3],
                        previewUrl = "",
                        collectionName = "",
                        trackViewUrl = "",
                        releaseDate = ""
                    ))
                }
            }
        } catch (e: Exception) { e.printStackTrace() }
    }

    private fun loadRecentSongs() {
        try {
            val prefs = requireContext().getSharedPreferences("recent_songs", android.content.Context.MODE_PRIVATE)
            val recentSet = prefs.getStringSet("recent_songs_list", emptySet()) ?: emptySet()
            recentSongs.clear()
            if (recentSet.isEmpty()) {
                recentSongs.addAll(SampleData.sampleSongs.take(3))
            } else {
                for (songData in recentSet.toList().takeLast(10)) {
                    val parts = songData.split("|")
                    if (parts.size >= 4) {
                        recentSongs.add(ITunesSong(
                            trackId = parts[0].toLongOrNull() ?: 0L,
                            trackName = parts[1],
                            artistName = parts[2],
                            artworkUrl100 = parts[3],
                            previewUrl = "",
                            collectionName = "",
                            trackViewUrl = "",
                            releaseDate = ""
                        ))
                    }
                }
                recentSongs.reverse()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            recentSongs.addAll(SampleData.sampleSongs.take(3))
        }
    }

    private fun setupRecyclerView() {
        adapter = MusicSelectionAdapter(displayedSongs) { music ->
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
        ivClearSearch.setOnClickListener { etSearchSong.text.clear() }
    }

    private fun setupTabs() {
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                etSearchSong.text.clear()
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
    }

    private fun showRecentSongs() {
        displayedSongs.clear()
        displayedSongs.addAll(recentSongs)
        adapter.notifyDataSetChanged()
        updateEmptyState()
    }

    private fun showAllSongs() {
        displayedSongs.clear()
        displayedSongs.addAll(allSongs)
        if (!allSongsLoaded) tvEmptyMessage.text = "Loading songs..."
        adapter.notifyDataSetChanged()
        updateEmptyState()
    }

    private fun filterSongs(query: String) {
        searchJob?.cancel()

        if (query.isEmpty()) {
            isSearching = false
            when (tabLayout.selectedTabPosition) {
                0 -> showFavoriteSongs()
                1 -> showRecentSongs()
                2 -> showAllSongs()
            }
            return
        }

        // Search locally first across ALL songs (not just current tab)
        val searchPool = when (tabLayout.selectedTabPosition) {
            0 -> favoriteSongs
            1 -> recentSongs
            else -> allSongs
        }

        val filtered = searchPool.filter { song ->
            song.trackName.contains(query, ignoreCase = true) ||
                    song.artistName.contains(query, ignoreCase = true)
        }

        displayedSongs.clear()
        displayedSongs.addAll(filtered)
        adapter.notifyDataSetChanged()

        // If few local results and query is long enough, search iTunes
        if (filtered.size < 5 && query.length >= 2) {
            isSearching = true
            tvEmptyMessage.text = "Searching..."
            llEmptyState.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
            rvMusic.visibility = if (filtered.isEmpty()) View.GONE else View.VISIBLE

            searchJob = lifecycleScope.launch {
                delay(400)
                searchITunes(query)
            }
        } else {
            isSearching = false
            updateEmptyState()
        }
    }

    private suspend fun searchITunes(query: String) {
        try {
            val response = RetrofitClient.api.searchSongsAsync(query, limit = 25)
            if (response.isSuccessful) {
                val songs = response.body()?.results ?: emptyList()
                displayedSongs.clear()
                displayedSongs.addAll(songs)
                requireActivity().runOnUiThread {
                    adapter.notifyDataSetChanged()
                    isSearching = false
                    updateEmptyState()
                }
            } else {
                requireActivity().runOnUiThread { isSearching = false; updateEmptyState() }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            requireActivity().runOnUiThread { isSearching = false; updateEmptyState() }
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
                else -> "Loading songs..."
            }
        } else {
            rvMusic.visibility = View.VISIBLE
            llEmptyState.visibility = View.GONE
        }
    }

    private fun saveRecentSong(song: ITunesSong) {
        try {
            val prefs = requireContext().getSharedPreferences("recent_songs", android.content.Context.MODE_PRIVATE)
            val recentSet = prefs.getStringSet("recent_songs_list", mutableSetOf())
                ?.toMutableSet() ?: mutableSetOf()
            val songData = "${song.trackId}|${song.trackName}|${song.artistName}|${song.artworkUrl100}"
            recentSet.add(songData)
            if (recentSet.size > 20) {
                val list = recentSet.toMutableList()
                recentSet.clear()
                recentSet.addAll(list.takeLast(20))
            }
            prefs.edit().putStringSet("recent_songs_list", recentSet).apply()
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun setOnMusicSelectedListener(listener: (ITunesSong) -> Unit) {
        onMusicSelectedListener = listener
    }
}

class MusicSelectionAdapter(
    private val musicList: List<ITunesSong>,
    private val onMusicClick: (ITunesSong) -> Unit
) : RecyclerView.Adapter<MusicSelectionAdapter.MusicViewHolder>() {

    inner class MusicViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivCover: ImageView = view.findViewById(R.id.ivMusicCover)
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
        if (music.artworkUrl100.isNotBlank()) {
            Glide.with(holder.itemView.context)
                .load(music.artworkUrl100)
                .placeholder(R.drawable.ic_music_note)
                .error(R.drawable.ic_music_note)
                .into(holder.ivCover)
        } else {
            holder.ivCover.setImageResource(R.drawable.ic_music_note)
        }
        holder.itemView.setOnClickListener { onMusicClick(music) }
    }

    override fun getItemCount() = musicList.size
}
