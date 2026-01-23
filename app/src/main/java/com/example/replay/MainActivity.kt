package com.example.replay

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.chip.Chip

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var chipAll: Chip
    private lateinit var chipArtists: Chip
    private lateinit var chipAlbums: Chip

    private lateinit var artistsRecyclerView: RecyclerView
    private lateinit var albumsRecyclerView: RecyclerView
    private lateinit var feedRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeViews()
        setupBottomNavigation()
        setupChips()
        setupRecyclerViews() // This will now resolve correctly
    }

    private fun initializeViews() {
        bottomNavigation = findViewById(R.id.bottomNavigation)
        chipAll = findViewById(R.id.chipAll)
        chipArtists = findViewById(R.id.chipArtists)
        chipAlbums = findViewById(R.id.chipAlbums)
        artistsRecyclerView = findViewById(R.id.artistsRecyclerView)
        albumsRecyclerView = findViewById(R.id.albumsRecyclerView)
        feedRecyclerView = findViewById(R.id.feedRecyclerView)
    }

    private fun setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // Already in MainActivity
                    true
                }
                R.id.nav_discover -> {
                    startActivity(Intent(this, DiscoverActivity::class.java))
                    true
                }
                R.id.nav_post -> {
                    // Handle post action
                    true
                }
                R.id.nav_library -> {
                    startActivity(Intent(this, LibraryActivity::class.java))
                    true
                }
                R.id.nav_profile -> {
                    // Handle profile navigation
                    true
                }
                else -> false
            }
        }
    }

    private fun setupChips() {
        chipAll.setOnClickListener {
            showAllContent()
        }

        chipArtists.setOnClickListener {
            showArtistsOnly()
        }

        chipAlbums.setOnClickListener {
            showAlbumsOnly()
        }
    }

    // Moved setupRecyclerViews out of setupChips to the class level
    private fun setupRecyclerViews() {
        // Artists
        artistsRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        // NOTE: You'll need to define the 'Artist' data class for this to compile
        val artists = listOf(
            Artist("1", "Taylor Swift", ""),
            Artist("2", "Ariana Grande", ""),
            Artist("3", "Black Pink", ""),
            Artist("4", "BTS", "")
        )
        // NOTE: You'll need to create the 'ArtistAdapter' class
        artistsRecyclerView.adapter = ArtistAdapter(artists)

        // Albums
        albumsRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        // NOTE: You'll need to define the 'Album' data class for this to compile
        val albums = listOf(
            Album("locket", "Madison Beer", "data:image/jpeg;base64,..."),
            Album("THE SIN: VANISH", "Enhypen", "data:image/jpeg;base64,/9j/...")
        )

        // NOTE: You'll need to create the 'AlbumAdapter' class
        albumsRecyclerView.adapter = AlbumAdapter(albums)

        // Feed with Songs
        feedRecyclerView.layoutManager = LinearLayoutManager(this)

        // Create sample music/songs list
        // NOTE: You'll need to define the 'Music' data class
        val musicList = listOf(
            Music("1", "Anti-Hero", "Taylor Swift", "Midnights", "image1"),
            Music("2", "7 rings", "Ariana Grande", "thank u, next", "image2"),
            Music("3", "How You Like That", "BLACKPINK", "THE ALBUM", "image3"),
            Music("4", "Dynamite", "BTS", "BE", "image4"),
            Music("5", "High On Heaven", "Nessa Barrett", "Pretty Poison", "image5"),
            Music("6", "THE SIN: VANISH", "Enhypen", "Album Name", "image6")
        )

        // NOTE: You'll need to create the 'SelectedMusicAdapter' class
        feedRecyclerView.adapter = SelectedMusicAdapter(musicList.toMutableList())
    }

    private fun showAllContent() {
        artistsRecyclerView.visibility = RecyclerView.VISIBLE
        albumsRecyclerView.visibility = RecyclerView.VISIBLE
        feedRecyclerView.visibility = RecyclerView.VISIBLE
    }

    private fun showArtistsOnly() {
        artistsRecyclerView.visibility = RecyclerView.VISIBLE
        albumsRecyclerView.visibility = RecyclerView.GONE
        feedRecyclerView.visibility = RecyclerView.GONE
    }

    private fun showAlbumsOnly() {
        artistsRecyclerView.visibility = RecyclerView.GONE
        albumsRecyclerView.visibility = RecyclerView.VISIBLE
        feedRecyclerView.visibility = RecyclerView.GONE
    }
}
