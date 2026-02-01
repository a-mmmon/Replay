package com.example.replay

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class MusicSelectionBottomSheet : BottomSheetDialogFragment() {

    private var onMusicSelectedListener: ((ITunesSong) -> Unit)? = null  // ✅ FIXED: Music → ITunesSong

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.bottom_sheet_music_selection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rvMusic = view.findViewById<RecyclerView>(R.id.rvMusicList)
        val tvTitle = view.findViewById<TextView>(R.id.tvMusicSelectionTitle)

        tvTitle.text = "Select Music"

        // Sample music list - using ITunesSong from SampleData
        val musicList = listOf(
            SampleData.sampleMusic1,
            SampleData.sampleMusic2,
            SampleData.sampleMusic3
        )

        val adapter = MusicSelectionAdapter(musicList) { music ->
            onMusicSelectedListener?.invoke(music)
            dismiss()
        }

        rvMusic.layoutManager = LinearLayoutManager(requireContext())
        rvMusic.adapter = adapter
    }

    fun setOnMusicSelectedListener(listener: (ITunesSong) -> Unit) {  // ✅ FIXED: Music → ITunesSong
        onMusicSelectedListener = listener
    }
}

// Simple adapter for music selection
class MusicSelectionAdapter(
    private val musicList: List<ITunesSong>,  // ✅ FIXED: Music → ITunesSong
    private val onMusicClick: (ITunesSong) -> Unit  // ✅ FIXED: Music → ITunesSong
) : RecyclerView.Adapter<MusicSelectionAdapter.MusicViewHolder>() {

    inner class MusicViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvMusicTitle)
        val tvArtist: TextView = view.findViewById(R.id.tvMusicArtist)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_music_selection, parent, false)
        return MusicViewHolder(view)
    }

    override fun onBindViewHolder(holder: MusicViewHolder, position: Int) {
        val music = musicList[position]
        holder.tvTitle.text = music.trackName      // ✅ FIXED: title → trackName
        holder.tvArtist.text = music.artistName    // ✅ FIXED: artist → artistName

        holder.itemView.setOnClickListener {
            onMusicClick(music)
        }
    }

    override fun getItemCount() = musicList.size
}