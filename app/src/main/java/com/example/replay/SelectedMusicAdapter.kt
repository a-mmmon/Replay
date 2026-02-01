package com.example.replay

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class SelectedMusicAdapter(
    private var songs: MutableList<ITunesSong>  // ✅ FIXED: Music → ITunesSong
) : RecyclerView.Adapter<SelectedMusicAdapter.MusicViewHolder>() {

    inner class MusicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val musicCover: ImageView = itemView.findViewById(R.id.musicCover)
        val musicTitle: TextView = itemView.findViewById(R.id.musicTitle)
        val musicArtist: TextView = itemView.findViewById(R.id.musicArtist)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_music, parent, false)
        return MusicViewHolder(view)
    }

    override fun onBindViewHolder(holder: MusicViewHolder, position: Int) {
        val song = songs[position]

        holder.musicTitle.text = song.trackName      // ✅ FIXED: title → trackName
        holder.musicArtist.text = song.artistName    // ✅ FIXED: artist → artistName

        // Load album artwork
        if (song.artworkUrl100.isNotEmpty()) {       // ✅ FIXED: coverUrl → artworkUrl100
            Glide.with(holder.itemView.context)
                .load(song.artworkUrl100)
                .placeholder(R.drawable.ic_android_placeholder)
                .into(holder.musicCover)
        } else {
            holder.musicCover.setImageResource(R.drawable.ic_android_placeholder)
        }
    }

    override fun getItemCount(): Int = songs.size

    fun updateData(newSongs: List<ITunesSong>) {  // ✅ FIXED: Music → ITunesSong, added this method
        songs.clear()
        songs.addAll(newSongs)
        notifyDataSetChanged()
    }
}