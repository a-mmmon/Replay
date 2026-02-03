package com.example.replay

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class CreatePostBottomSheet : BottomSheetDialogFragment() {

    private lateinit var postInput: EditText
    private lateinit var addMusicButton: Button
    private lateinit var postButton: Button
    private lateinit var musicContainer: View
    private lateinit var musicImage: ImageView
    private lateinit var musicTitle: TextView
    private lateinit var musicArtist: TextView
    private lateinit var favoriteButton: ImageButton
    private lateinit var removeMusicButton: ImageButton

    private var selectedMusic: ITunesSong? = null
    private var isFavorite: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_create_post, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)
        setupClickListeners()

        // Load pre-selected music if any
        arguments?.getParcelable<ITunesSong>("selected_music")?.let {
            selectMusic(it)
        }
    }

    private fun initializeViews(view: View) {
        postInput = view.findViewById(R.id.postInput)
        addMusicButton = view.findViewById(R.id.addMusicButton)
        postButton = view.findViewById(R.id.postButton)
        musicContainer = view.findViewById(R.id.musicContainer)
        musicImage = view.findViewById(R.id.musicImage)
        musicTitle = view.findViewById(R.id.musicTitle)
        musicArtist = view.findViewById(R.id.musicArtist)
        favoriteButton = view.findViewById(R.id.favoriteButton)
        removeMusicButton = view.findViewById(R.id.removeMusicButton)
    }

    private fun setupClickListeners() {
        addMusicButton.setOnClickListener {
            openMusicSelector()
        }

        favoriteButton.setOnClickListener {
            toggleFavorite()
        }

        removeMusicButton.setOnClickListener {
            removeMusic()
        }

        postButton.setOnClickListener {
            createPost()
        }
    }

    private fun openMusicSelector() {
        // Open music selection bottom sheet or activity
        val musicSelectionSheet = MusicSelectionBottomSheet()
        musicSelectionSheet.setOnMusicSelectedListener { music ->
            selectMusic(music)
        }
        musicSelectionSheet.show(parentFragmentManager, "MusicSelection")
    }

    private fun selectMusic(music: ITunesSong) {
        selectedMusic = music
        musicContainer.visibility = View.VISIBLE
        addMusicButton.text = "Change Music"

        musicTitle.text = music.trackName
        musicArtist.text = music.artistName

        // Load album art
        if (music.artworkUrl100.isNotEmpty()) {
            Glide.with(this)
                .load(music.artworkUrl100)
                .placeholder(R.drawable.ic_music_note)
                .into(musicImage)
        }

        updateFavoriteButton()
    }

    private fun removeMusic() {
        selectedMusic = null
        musicContainer.visibility = View.GONE
        addMusicButton.text = "Add Music"
        isFavorite = false
    }

    private fun toggleFavorite() {
        isFavorite = !isFavorite
        updateFavoriteButton()

        selectedMusic?.let { music ->
            if (isFavorite) {
                FavoriteManager.addToFavorites(music)
                Toast.makeText(requireContext(), "Added to favorites", Toast.LENGTH_SHORT).show()
            } else {
                FavoriteManager.removeFromFavorites(music)
                Toast.makeText(requireContext(), "Removed from favorites", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateFavoriteButton() {
        selectedMusic?.let { music ->
            isFavorite = FavoriteManager.isFavorite(music)
            favoriteButton.setImageResource(
                if (isFavorite) R.drawable.ic_star_filled
                else R.drawable.ic_star_outline
            )
        }
    }

    private fun createPost() {
        val text = postInput.text.toString().trim()

        if (text.isEmpty() && selectedMusic == null) {
            Toast.makeText(requireContext(), "Please add text or music", Toast.LENGTH_SHORT).show()
            return
        }

        // Get current user info
        val prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val username = prefs.getString("username", "uri") ?: "uri"

        // Create post object
        val post = Post(
            postId = "post_${System.currentTimeMillis()}",
            userId = "current_user",
            username = username,
            userProfileImage = "",
            caption = text,
            imageUrl = "",
            likes = 0,
            comments = 0,
            timestamp = System.currentTimeMillis(),
            music = selectedMusic
        )

        // ✅ SAVE POST TO SHAREDPREFERENCES WITH MUSIC
        savePost(post)

        // Show success message
        Toast.makeText(requireContext(), "Post created!", Toast.LENGTH_SHORT).show()

        // Close bottom sheet
        dismiss()
    }

    private fun savePost(post: Post) {
        val prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val editor = prefs.edit()

        // Get current post count
        val postCount = prefs.getInt("post_count", 0)

        // Save new post
        editor.putString("post_${postCount}_id", post.postId)
        editor.putString("post_${postCount}_caption", post.caption)
        editor.putLong("post_${postCount}_timestamp", post.timestamp)
        editor.putInt("post_${postCount}_likes", post.likes)

        // ✅ FIXED: Save music if present
        if (post.music != null) {
            editor.putLong("post_${postCount}_music_trackId", post.music.trackId)
            editor.putString("post_${postCount}_music_trackName", post.music.trackName)
            editor.putString("post_${postCount}_music_artistName", post.music.artistName)
            editor.putString("post_${postCount}_music_artworkUrl", post.music.artworkUrl100)
            editor.putString("post_${postCount}_music_previewUrl", post.music.previewUrl)
            editor.putString("post_${postCount}_music_collectionName", post.music.collectionName)
            editor.putString("post_${postCount}_music_trackViewUrl", post.music.trackViewUrl)
            editor.putString("post_${postCount}_music_releaseDate", post.music.releaseDate)
        }

        // Increment post count
        editor.putInt("post_count", postCount + 1)

        editor.apply()
    }

    companion object {
        fun newInstance(music: ITunesSong? = null): CreatePostBottomSheet {
            return CreatePostBottomSheet().apply {
                music?.let {
                    arguments = Bundle().apply {
                        putParcelable("selected_music", it)
                    }
                }
            }
        }
    }
}