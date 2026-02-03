package com.example.replay

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class LibraryFragment : Fragment() {

    private lateinit var favoriteSongsRecycler: RecyclerView
    private lateinit var favoriteSongsAdapter: DiscoverSongAdapter
    private val favoriteSongs = mutableListOf<ITunesSong>()
    private lateinit var emptyStateText: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_library, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        favoriteSongsRecycler = view.findViewById(R.id.favoriteSongsRecycler)
        emptyStateText = view.findViewById(R.id.emptyStateText)

        setupRecyclerView()
        loadFavoriteSongs()
    }

    private fun setupRecyclerView() {
        favoriteSongsAdapter = DiscoverSongAdapter(favoriteSongs) { song ->
            showMusicBottomSheet(song)
        }
        favoriteSongsRecycler.layoutManager = LinearLayoutManager(requireContext())
        favoriteSongsRecycler.adapter = favoriteSongsAdapter
    }

    private fun loadFavoriteSongs() {
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

            favoriteSongsAdapter.notifyDataSetChanged()

            // Show/hide empty state
            if (favoriteSongs.isEmpty()) {
                emptyStateText.visibility = View.VISIBLE
                favoriteSongsRecycler.visibility = View.GONE
            } else {
                emptyStateText.visibility = View.GONE
                favoriteSongsRecycler.visibility = View.VISIBLE
            }

        } catch (e: Exception) {
            Log.e("LibraryFragment", "Error loading favorites", e)
        }
    }

    private fun showMusicBottomSheet(song: ITunesSong) {
        val bottomSheet = MusicPlayerBottomSheet(song) { action ->
            when (action) {
                MusicPlayerBottomSheet.Action.ADD_TO_FAVORITES -> {
                    // Already in favorites, could implement remove here
                }
                MusicPlayerBottomSheet.Action.OPEN_SPOTIFY -> openMusicApp("spotify", song)
                MusicPlayerBottomSheet.Action.OPEN_APPLE_MUSIC -> openMusicApp("apple_music", song)
                MusicPlayerBottomSheet.Action.OPEN_YOUTUBE_MUSIC -> openMusicApp("youtube_music", song)
            }
        }
        bottomSheet.show(parentFragmentManager, "MusicPlayerBottomSheet")
    }

    private fun openMusicApp(app: String, song: ITunesSong) {
        try {
            val intent = when (app) {
                "spotify" -> {
                    android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                        data = android.net.Uri.parse("https://open.spotify.com/search/${song.trackName} ${song.artistName}")
                    }
                }
                "apple_music" -> {
                    android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                        data = android.net.Uri.parse("https://music.apple.com/search?term=${song.trackName} ${song.artistName}")
                    }
                }
                "youtube_music" -> {
                    android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                        data = android.net.Uri.parse("https://music.youtube.com/search?q=${song.trackName} ${song.artistName}")
                    }
                }
                else -> return
            }
            startActivity(intent)
        } catch (e: Exception) {
            Log.e("LibraryFragment", "Error opening music app", e)
        }
    }

    override fun onResume() {
        super.onResume()
        // Reload favorites when returning to this fragment
        loadFavoriteSongs()
    }
}