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
            selectedMusicList.add(Music("Demo Song", "Demo Artist"))
            selectedMusicAdapter.notifyItemInserted(selectedMusicList.size - 1)
        }

        btnPost.setOnClickListener {
            val post = Post(
                userName = "Demo User",
                content = etPostContent.text.toString(),
                musicList = selectedMusicList.toList()
            )
            onPostCreatedListener?.invoke(post)
            dismiss()
        }
    }
}
