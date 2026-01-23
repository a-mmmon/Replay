package com.example.replay

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.replay.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var artistAdapter: ArtistAdapter
    private lateinit var albumAdapter: AlbumAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerViews()
        setupChips()
        setupBottomNavigation()
        loadData()
    }

    // ---------------- RecyclerViews ----------------

    private fun setupRecyclerViews() {
        // Artists RecyclerView (Horizontal)
        binding.artistsRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        artistAdapter = ArtistAdapter(emptyList())
        binding.artistsRecyclerView.adapter = artistAdapter

        // Albums RecyclerView (Grid - 2 columns)
        binding.albumsRecyclerView.layoutManager = GridLayoutManager(this, 2)

        albumAdapter = AlbumAdapter(emptyList())
        binding.albumsRecyclerView.adapter = albumAdapter
    }

    // ---------------- Chips ----------------

    private fun setupChips() {
        binding.chipGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.chipAll -> loadAllContent()
                R.id.chipArtists -> loadArtistsOnly()
                R.id.chipAlbums -> loadAlbumsOnly()
            }
        }
    }

    // ---------------- Bottom Navigation ----------------

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {

                R.id.nav_home -> {
                    // Already on Home
                    true
                }

                R.id.nav_discover -> {
                    // TODO: open Discover fragment/activity
                    true
                }

                R.id.nav_post -> {
                    // TODO: open CreatePostBottomSheet
                    showCreatePostBottomSheet()
                    true
                }

                R.id.nav_library -> {
                    // TODO: open Library
                    true
                }

                R.id.nav_profile -> {
                    // TODO: open Profile
                    true
                }

                else -> false
            }
        }
    }

    // ---------------- Data ----------------

    private fun loadData() {
        artistAdapter.updateData(getSampleArtists())
        albumAdapter.updateData(getSampleAlbums())
    }

    private fun loadAllContent() {
        loadData()
    }

    private fun loadArtistsOnly() {
        artistAdapter.updateData(getSampleArtists())
        albumAdapter.updateData(emptyList())
    }

    private fun loadAlbumsOnly() {
        artistAdapter.updateData(emptyList())
        albumAdapter.updateData(getSampleAlbums())
    }

    // ---------------- Sample Data ----------------

    private fun getSampleArtists(): List<Artist> {
        return listOf(
            Artist("1", "Artist 1", "https://picsum.photos/200"),
            Artist("2", "Artist 2", "https://picsum.photos/201"),
            Artist("3", "Artist 3", "https://picsum.photos/202"),
            Artist("4", "Artist 4", "https://picsum.photos/203"),
            Artist("5", "Artist 5", "https://picsum.photos/204"),
            Artist("6", "Artist 6", "https://picsum.photos/205"),
            Artist("7", "Artist 7", "https://picsum.photos/206"),
            Artist("8", "Artist 8", "https://picsum.photos/207")
        )
    }

    private fun getSampleAlbums(): List<Album> {
        return listOf(
            Album("Trending Album 1", "Artist A", "https://picsum.photos/300"),
            Album("Trending Album 2", "Artist B", "https://picsum.photos/301"),
            Album("Trending Album 3", "Artist C", "https://picsum.photos/302"),
            Album("Trending Album 4", "Artist D", "https://picsum.photos/303")
        )
    }

    // ---------------- Post ----------------

    private fun showCreatePostBottomSheet() {
        val bottomSheet = CreatePostBottomSheet()
        bottomSheet.show(supportFragmentManager, "CreatePost")
    }
}
