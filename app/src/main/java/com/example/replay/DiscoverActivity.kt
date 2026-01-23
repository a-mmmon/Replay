package com.example.replay

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class DiscoverActivity : AppCompatActivity() {

    private lateinit var rvDiscover: RecyclerView
    private lateinit var discoverAdapter: DiscoverAdapter

    private val musicList = mutableListOf<Music>()
    private val albumList = mutableListOf<Album>()
    private val artistList = mutableListOf<Artist>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_discover)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Discover"

        rvDiscover = findViewById(R.id.rvDiscover)
        rvDiscover.layoutManager = LinearLayoutManager(this)

        // In DiscoverActivity.kt, inside the onCreate method

        // In DiscoverActivity.kt, inside the onCreate method

        // In DiscoverActivity.kt, inside the onCreate method

        discoverAdapter = DiscoverAdapter(
            songs = musicList,   // Pass the musicList here
            albums = albumList,
            artists = artistList,
        )

        rvDiscover.adapter = discoverAdapter

        loadDiscoverData()




        rvDiscover.adapter = discoverAdapter

        loadDiscoverData()
    }

    private fun loadDiscoverData() {
        musicList.addAll(SampleData.music)
        albumList.addAll(SampleData.albums)
        artistList.addAll(SampleData.artists)
        discoverAdapter.notifyDataSetChanged()
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
