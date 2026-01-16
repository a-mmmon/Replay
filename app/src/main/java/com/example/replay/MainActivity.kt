package com.example.replay

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.chip.ChipGroup

class MainActivity : AppCompatActivity() {

    private lateinit var artistsRecyclerView: RecyclerView
    private lateinit var albumsRecyclerView: RecyclerView
    private lateinit var chipGroup: ChipGroup
    private lateinit var fabFeed: FloatingActionButton

    private lateinit var artistAdapter: ArtistAdapter
    private lateinit var albumAdapter: AlbumAdapter

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
        chipGroup = findViewById(R.id.chipGroup)
        fabFeed = findViewById(R.id.fabFeed)
    }

    private fun setupRecyclerViews() {
        // Artists RecyclerView (Horizontal)
        artistsRecyclerView.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        artistAdapter = ArtistAdapter(emptyList())
        artistsRecyclerView.adapter = artistAdapter

        // Albums RecyclerView (Grid - 2 columns)
        albumsRecyclerView.layoutManager = GridLayoutManager(this, 2)
        albumAdapter = AlbumAdapter(emptyList())
        albumsRecyclerView.adapter = albumAdapter
    }

    private fun setupListeners() {
        fabFeed.setOnClickListener {
            val intent = Intent(this, FeedActivity::class.java)
            startActivity(intent)
        }

        chipGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.chipAll -> loadAllContent()
                R.id.chipMusic -> loadMusicOnly()
                R.id.chipPosts -> loadPostsOnly()
            }
        }
    }

    private fun loadData() {
        // Load sample data
        val artists = getSampleArtists()
        val albums = getSampleAlbums()

        artistAdapter.updateData(artists)
        albumAdapter.updateData(albums)
    }

    private fun loadAllContent() {
        // Filter to show all content
        loadData()
    }

    private fun loadMusicOnly() {
        // Filter to show only music
    }

    private fun loadPostsOnly() {
        // Filter to show only posts
    }

    private fun getSampleArtists(): List<Artist> {
        return listOf(
            Artist("1", "Artist 1", "https://picsum.photos/200"),
            Artist("2", "Artist 2", "https://picsum.photos/201"),
            Artist("3", "Artist 3", "https://picsum.photos/202"),
            Artist("4", "Artist 4", "https://picsum.photos/203")
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
}