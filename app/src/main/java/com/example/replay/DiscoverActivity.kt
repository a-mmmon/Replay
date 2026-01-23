package com.example.replay

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText

class DiscoverActivity : AppCompatActivity() {

    private lateinit var searchInput: TextInputEditText
    private lateinit var artistsRecyclerView: RecyclerView
    private lateinit var songsRecyclerView: RecyclerView

    private lateinit var artistAdapter: ArtistAdapter
    private lateinit var musicAdapter: SelectedMusicAdapter

    private val allArtists = mutableListOf<Artist>()
    private val allSongs = mutableListOf<Music>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_discover)

        searchInput = findViewById(R.id.searchInput)
        artistsRecyclerView = findViewById(R.id.searchArtistsRecyclerView)
        songsRecyclerView = findViewById(R.id.searchSongsRecyclerView)

        setupRecyclerViews()
        loadData()
        setupSearch()
    }

    private fun setupRecyclerViews() {
        artistAdapter = ArtistAdapter(mutableListOf())
        artistsRecyclerView.layoutManager = LinearLayoutManager(this)
        artistsRecyclerView.adapter = artistAdapter

        musicAdapter = SelectedMusicAdapter(mutableListOf())
        songsRecyclerView.layoutManager = LinearLayoutManager(this)
        songsRecyclerView.adapter = musicAdapter
    }

    private fun loadData() {
        // Load your artists and songs data here
        allArtists.addAll(listOf(
            Artist("1", "Taylor Swift", "url1"),
            Artist("2", "Ariana Grande", "url2"),
            Artist("3", "Black Pink", "url3")
        ))

        allSongs.addAll(listOf(
            Music("1", "Song 1", "Artist 1", "album1", "cover1"),
            Music("2", "Song 2", "Artist 2", "album2", "cover2")
        ))

        artistAdapter.updateData(allArtists)
        musicAdapter.updateData(allSongs)
    }

    private fun setupSearch() {
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().lowercase()
                filterData(query)
            }
        })
    }

    private fun filterData(query: String) {
        if (query.isEmpty()) {
            artistAdapter.updateData(allArtists)
            musicAdapter.updateData(allSongs)
        } else {
            val filteredArtists = allArtists.filter {
                it.name.lowercase().contains(query)
            }
            val filteredSongs = allSongs.filter {
                it.title.lowercase().contains(query) ||
                        it.artist.lowercase().contains(query)
            }

            artistAdapter.updateData(filteredArtists)
            musicAdapter.updateData(filteredSongs)
        }
    }
}