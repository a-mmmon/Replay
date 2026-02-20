package com.example.replay

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView

class LibraryActivity : BaseThemedActivity() {

    private lateinit var favoriteSongsRecyclerView: RecyclerView
    private lateinit var emptyStateText: TextView
    private lateinit var musicAdapter: SelectedMusicAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_library)

        favoriteSongsRecyclerView = findViewById(R.id.favoriteSongsRecyclerView)
        emptyStateText = findViewById(R.id.emptyStateText)

        setupRecyclerView()
        loadFavorites()
    }

    override fun onResume() {
        super.onResume()
        loadFavorites()
    }

    private fun setupRecyclerView() {
        musicAdapter = SelectedMusicAdapter(mutableListOf())
        favoriteSongsRecyclerView.layoutManager = LinearLayoutManager(this)
        favoriteSongsRecyclerView.adapter = musicAdapter
    }

    private fun loadFavorites() {
        val favorites = FavoriteManager.getFavorites()  // ✅ FIXED: FavoritesManager → FavoriteManager (removed 's')

        if (favorites.isEmpty()) {
            emptyStateText.visibility = TextView.VISIBLE
            favoriteSongsRecyclerView.visibility = RecyclerView.GONE
        } else {
            emptyStateText.visibility = TextView.GONE
            favoriteSongsRecyclerView.visibility = RecyclerView.VISIBLE
            musicAdapter.updateData(favorites)
        }
    }
}