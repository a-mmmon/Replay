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
        val artists = listOf(
            Artist("1", "Taylor Swift", "https://picsum.photos/200/200?random=1"),
            Artist("2", "Ariana Grande", "https://picsum.photos/200/200?random=2"),
            Artist("3", "Black Pink", "https://picsum.photos/200/200?random=3"),
            Artist("4", "BTS", "https://picsum.photos/200/200?random=4")
        )
        artistsRecyclerView.adapter = ArtistAdapter(artists)

        // Albums
        albumsRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val albums = listOf(
            Album("01", "locket", "Madison Beer", "https://picsum.photos/200/200?random=5"),
            Album("02", "locket", "Madison Beer", "https://picsum.photos/200/200?random=5"),
            Album("03", "locket", "Madison Beer", "https://picsum.photos/200/200?random=5"),
            Album("04", "locket", "Madison Beer", "https://picsum.photos/200/200?random=5")
        )

        albumsRecyclerView.adapter = AlbumAdapter(albums)

        // Feed with Songs
        feedRecyclerView.layoutManager = LinearLayoutManager(this)

        val musicList = listOf(
            Music("1", "Anti-Hero", "Taylor Swift", "Midnights", "https://picsum.photos/200/200?random=9"),
            Music("2", "7 rings", "Ariana Grande", "thank u, next", "https://picsum.photos/200/200?random=10"),
            Music("3", "How You Like That", "BLACKPINK", "THE ALBUM", "https://picsum.photos/200/200?random=11"),
            Music("4", "Dynamite", "BTS", "BE", "https://picsum.photos/200/200?random=12"),
            Music("5", "High On Heaven", "Nessa Barrett", "Pretty Poison", "https://picsum.photos/200/200?random=13"),
            Music("6", "THE SIN: VANISH", "Enhypen", "Album Name", "https://picsum.photos/200/200?random=14")
        )

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
