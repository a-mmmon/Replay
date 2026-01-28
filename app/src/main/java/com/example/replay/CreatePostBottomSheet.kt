package com.example.replay

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.util.UUID

class CreatePostBottomSheet : BottomSheetDialogFragment() {

    private lateinit var btnAddMusic: Button
    private lateinit var btnPost: Button
    private lateinit var rvSelectedMusic: RecyclerView
    private lateinit var etPostContent: EditText
    private lateinit var imgUserProfile: ImageView

    private val selectedMusicList = mutableListOf<Music>()
    private lateinit var selectedMusicAdapter: SelectedMusicAdapter

    var onPostCreatedListener: ((Post) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(
            R.layout.bottom_sheet_create_post,
            container,
            false
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        imgUserProfile = view.findViewById(R.id.imgUserProfile)
        etPostContent = view.findViewById(R.id.etPostContent)
        rvSelectedMusic = view.findViewById(R.id.rvSelectedMusic)
        btnAddMusic = view.findViewById(R.id.btnAddMusic)
        btnPost = view.findViewById(R.id.btnPost)

        selectedMusicAdapter = SelectedMusicAdapter(selectedMusicList)

        rvSelectedMusic.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvSelectedMusic.adapter = selectedMusicAdapter

        btnAddMusic.setOnClickListener {
            val newMusic = Music(
                id = UUID.randomUUID().toString(),
                title = "Demo Song",
                artist = "Demo Artist",
                album = "",
                coverUrl = "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQABAAD/2wCEAAkGBxITEhUTExIVFhUX"
            )
            selectedMusicList.add(newMusic)
            selectedMusicAdapter.notifyItemInserted(selectedMusicList.size - 1)
        }

        btnPost.setOnClickListener {
            // ✅ UPDATED: Create post with new format using current user profile
            val newPost = Post(
                userName = SampleData.currentUserProfile.userName,
                userHandle = SampleData.currentUserProfile.userHandle,
                userAvatarUrl = SampleData.currentUserProfile.avatarUrl,
                content = etPostContent.text.toString(),
                musicList = selectedMusicList.toList(),
                timestamp = "Just now",
                likesCount = 0
            )

            // ✅ Add post to home feed
            SampleData.posts.add(0, newPost)

            // ✅ Add post to user's profile
            SampleData.currentUserProfile.userPosts.add(0, newPost)

            // ✅ Update user's post count
            SampleData.currentUserProfile.postsCount = SampleData.currentUserProfile.userPosts.size

            onPostCreatedListener?.invoke(newPost)
            dismiss()
        }
    }
}