package com.example.replay

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class MusicPlayerBottomSheet(
    private val song: ITunesSong,
    private val onActionClick: (Action) -> Unit
) : BottomSheetDialogFragment() {

    enum class Action {
        ADD_TO_FAVORITES,
        OPEN_SPOTIFY,
        OPEN_APPLE_MUSIC,
        OPEN_YOUTUBE_MUSIC
    }

    private var isFavorite = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_music, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val albumArt = view.findViewById<ImageView>(R.id.albumArt)
        val trackName = view.findViewById<TextView>(R.id.trackName)
        val artistName = view.findViewById<TextView>(R.id.artistName)
        val favoriteButton = view.findViewById<ImageButton>(R.id.favoriteButton)
        val spotifyButton = view.findViewById<Button>(R.id.spotifyButton)
        val appleMusicButton = view.findViewById<Button>(R.id.appleMusicButton)
        val youtubeMusicButton = view.findViewById<Button>(R.id.youtubeMusicButton)

        // Display song info
        trackName.text = song.trackName
        artistName.text = song.artistName

        // Load album art
        if (song.artworkUrl100.isNotEmpty()) {
            Glide.with(this)
                .load(song.artworkUrl100)
                .placeholder(R.drawable.ic_music_note)
                .into(albumArt)
        }

        // Check if song is already favorited
        checkIfFavorite()
        updateFavoriteButton(favoriteButton)

        // Favorite button click
        favoriteButton.setOnClickListener {
            toggleFavorite()
            updateFavoriteButton(favoriteButton)
            onActionClick(Action.ADD_TO_FAVORITES)
        }

        // Platform buttons
        spotifyButton.setOnClickListener {
            onActionClick(Action.OPEN_SPOTIFY)
            openMusicApp("spotify")
        }

        appleMusicButton.setOnClickListener {
            onActionClick(Action.OPEN_APPLE_MUSIC)
            openMusicApp("apple_music")
        }

        youtubeMusicButton.setOnClickListener {
            onActionClick(Action.OPEN_YOUTUBE_MUSIC)
            openMusicApp("youtube_music")
        }
    }

    private fun checkIfFavorite() {
        try {
            val prefs = requireContext().getSharedPreferences("favorites", Context.MODE_PRIVATE)
            val favorites = prefs.getStringSet("favorite_songs", mutableSetOf()) ?: mutableSetOf()
            val songData = "${song.trackId}|${song.trackName}|${song.artistName}|${song.artworkUrl100}"
            isFavorite = favorites.contains(songData)
        } catch (e: Exception) {
            Log.e("MusicPlayerBottomSheet", "Error checking favorite", e)
        }
    }

    private fun toggleFavorite() {
        try {
            val prefs = requireContext().getSharedPreferences("favorites", Context.MODE_PRIVATE)
            val favorites = prefs.getStringSet("favorite_songs", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
            val songData = "${song.trackId}|${song.trackName}|${song.artistName}|${song.artworkUrl100}"

            if (isFavorite) {
                // Remove from favorites
                favorites.remove(songData)
                isFavorite = false
                Toast.makeText(requireContext(), "Removed from favorites", Toast.LENGTH_SHORT).show()
            } else {
                // Add to favorites
                favorites.add(songData)
                isFavorite = true
                Toast.makeText(requireContext(), "Added to favorites!", Toast.LENGTH_SHORT).show()
            }

            prefs.edit().putStringSet("favorite_songs", favorites).apply()
        } catch (e: Exception) {
            Log.e("MusicPlayerBottomSheet", "Error toggling favorite", e)
        }
    }

    private fun updateFavoriteButton(button: ImageButton) {
        if (isFavorite) {
            button.setImageResource(R.drawable.ic_star_filled)
        } else {
            button.setImageResource(R.drawable.ic_star_outline)
        }
    }

    private fun openMusicApp(app: String) {
        try {
            val intent = when (app) {
                "spotify" -> {
                    android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                        data = android.net.Uri.parse("https://open.spotify.com/search/${song.trackName} ${song.artistName}")
                    }
                }
                "apple_music" -> {
                    android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                        data = android.net.Uri.parse(song.trackViewUrl.ifEmpty {
                            "https://music.apple.com/search?term=${song.trackName} ${song.artistName}"
                        })
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
            Log.e("MusicPlayerBottomSheet", "Error opening music app", e)
            Toast.makeText(requireContext(), "Could not open app", Toast.LENGTH_SHORT).show()
        }
    }
}