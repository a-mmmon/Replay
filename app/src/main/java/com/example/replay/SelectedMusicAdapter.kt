package com.example.replay

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class SelectedMusicAdapter(
    private val musicList: List<Music>,
    private val onRemoveClick: (Music) -> Unit
) : RecyclerView.Adapter<SelectedMusicAdapter.MusicViewHolder>() {

    inner class MusicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgMusicCover: ImageView = itemView.findViewById(R.id.imgMusicCover)
        val tvMusicTitle: TextView = itemView.findViewById(R.id.tvMusicTitle)
        val tvMusicArtist: TextView = itemView.findViewById(R.id.tvMusicArtist)
        val imgRemove: ImageView = itemView.findViewById(R.id.imgRemove)

        fun bind(music: Music) {
            tvMusicTitle.text = music.title
            tvMusicArtist.text = music.artist

            Glide.with(itemView.context)
                .load(music.albumArt)
                .placeholder(R.drawable.ic_music)
                .into(imgMusicCover)

            imgRemove.setOnClickListener {
                onRemoveClick(music)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_selected_music, parent, false)
        return MusicViewHolder(view)
    }

    override fun onBindViewHolder(holder: MusicViewHolder, position: Int) {
        holder.bind(musicList[position])
    }

    override fun getItemCount(): Int = musicList.size
}