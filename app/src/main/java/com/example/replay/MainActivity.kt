package com.example.replay

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var artistsRecyclerView: RecyclerView
    private lateinit var albumsRecyclerView: RecyclerView
    private lateinit var feedRecyclerView: RecyclerView
    private lateinit var chipGroup: ChipGroup
    private lateinit var fabFeed: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupRecyclerViews()
        setupListeners()
        loadData()
    }

    private fun initViews() {
        artistsRecyclerView = findViewById(R.id.artistsRecyclerView)
        albumsRecyclerView = findViewById(R.id.albumsRecyclerView)
        feedRecyclerView = findViewById(R.id.feedRecyclerView)
        chipGroup = findViewById(R.id.chipGroup)
        fabFeed = findViewById(R.id.fabFeed)
    }

    private fun setupRecyclerViews() {
        artistsRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        albumsRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        feedRecyclerView.layoutManager =
            LinearLayoutManager(this)
    }

    private fun setupListeners() {
        chipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            when {
                checkedIds.contains(R.id.chipArtists) -> {
                    artistsRecyclerView.visibility = RecyclerView.VISIBLE
                    albumsRecyclerView.visibility = RecyclerView.GONE
                }

                checkedIds.contains(R.id.chipAlbums) -> {
                    artistsRecyclerView.visibility = RecyclerView.GONE
                    albumsRecyclerView.visibility = RecyclerView.VISIBLE
                }

                else -> {
                    artistsRecyclerView.visibility = RecyclerView.VISIBLE
                    albumsRecyclerView.visibility = RecyclerView.VISIBLE
                }
            }
        }

        fabFeed.setOnClickListener {
            // TODO: Open CreatePostBottomSheet
        }
    }

    private fun loadData() {
        val artists = listOf(
            Artist("Taylor Swift", "https://i.imgur.com/9Xn4K0P.jpg"),
            Artist("Drake", "https://i.imgur.com/J5LVHEL.jpg"),
            Artist("The Weeknd", "https://i.imgur.com/ZcLLrkY.jpg")
        )

        artistsRecyclerView.adapter = ArtistAdapter(artists)
    }
}
