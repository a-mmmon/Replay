package com.example.replay

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.tabs.TabLayout

class CreatePostBottomSheet : BottomSheetDialogFragment() {

    private lateinit var tabLayout: TabLayout
    private lateinit var imgUserProfile: ImageView
    private lateinit var etPostContent: EditText
    private lateinit var rvSelectedMusic: RecyclerView
    private lateinit var btnAddMusic: Button
    private lateinit var btnPost: Button

    private lateinit var selectedMusicAdapter: SelectedMusicAdapter
    private val selectedMusicList = mutableListOf<Music>()

    private var onPostCreatedListener: ((Post) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_create_post, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupTabs()
        setupRecyclerView()
        setupListeners()
    }

    private fun initViews(view: View) {
        tabLayout = view.findViewById(R.id.tabLayout)
        imgUserProfile = view.findViewById(R.id.imgUserProfile)
        etPostContent = view.findViewById(R.id.etPostContent)
        rvSelectedMusic = view.findViewById(R.id.rvSelectedMusic)
        btnAddMusic = view.findViewById(R.id.btnAddMusic)
        btnPost = view.findViewById(R.id.btnPost)
    }

    private fun setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("Cancel"))
        tabLayout.addTab(tabLayout.newTab().setText("Draft"))
        tabLayout.addTab(tabLayout.newTab().setText("Post"))

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> dismiss() // Cancel
                    1 -> saveDraft() // Draft
                    2 -> createPost() // Post
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupRecyclerView() {
        rvSelectedMusic.layoutManager = LinearLayoutManager(
            context,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        selectedMusicAdapter = SelectedMusicAdapter(selectedMusicList) { music ->
            removeMusic(music)
        }
        rvSelectedMusic.adapter = selectedMusicAdapter

        updateMusicVisibility()
    }

    private fun setupListeners() {
        btnAddMusic.setOnClickListener {
            showMusicPicker()
        }

        btnPost.setOnClickListener {
            createPost()
        }

        etPostContent.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                etPostContent.hint = ""
            } else {
                etPostContent.hint = "What's happening"
            }
        }
    }

    private fun showMusicPicker() {
        // Show music picker dialog or activity
        // For demo, add sample music
        val sampleMusic = Music(
            id = "m${selectedMusicList.size + 1}",
            title = "Song ${selectedMusicList.size + 1}",
            artist = "Artist Name",
            albumArt = "https://picsum.photos/20${selectedMusicList.size}"
        )
        selectedMusicList.add(sampleMusic)
        selectedMusicAdapter.notifyItemInserted(selectedMusicList.size - 1)
        updateMusicVisibility()
    }

    private fun removeMusic(music: Music) {
        val position = selectedMusicList.indexOf(music)
        if (position != -1) {
            selectedMusicList.removeAt(position)
            selectedMusicAdapter.notifyItemRemoved(position)
            updateMusicVisibility()
        }
    }

    private fun updateMusicVisibility() {
        rvSelectedMusic.visibility = if (selectedMusicList.isEmpty()) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }

    private fun saveDraft() {
        // Save draft logic
        dismiss()
    }

    private fun createPost() {
        val content = etPostContent.text.toString()

        if (content.isBlank() && selectedMusicList.isEmpty()) {
            // Show error - post is empty
            return
        }

        val post = Post(
            id = System.currentTimeMillis().toString(),
            userId = "currentUser",
            userName = "Current User",
            userAvatar = "https://picsum.photos/100",
            content = content,
            type = if (selectedMusicList.isNotEmpty()) PostType.MUSIC else PostType.TEXT,
            musicAttachment = selectedMusicList.firstOrNull(),
            timestamp = System.currentTimeMillis(),
            likes = 0,
            comments = 0
        )

        onPostCreatedListener?.invoke(post)
        dismiss()
    }

    fun setOnPostCreatedListener(listener: (Post) -> Unit) {
        onPostCreatedListener = listener
    }

    override fun getTheme(): Int = R.style.BottomSheetDialogTheme
}