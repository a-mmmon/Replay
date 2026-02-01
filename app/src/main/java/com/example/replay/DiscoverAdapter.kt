package com.example.replay

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class DiscoverAdapter(
    private val songs: MutableList<ITunesSong>
) : RecyclerView.Adapter<DiscoverAdapter.SongViewHolder>() {

    inner class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val albumArt: ImageView = itemView.findViewById(R.id.albumArt)
        val trackName: TextView = itemView.findViewById(R.id.trackName)
        val artistName: TextView = itemView.findViewById(R.id.artistName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_discover_song, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]

        holder.trackName.text = song.trackName
        holder.artistName.text = song.artistName

        // Load album art
        if (song.artworkUrl100.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(song.artworkUrl100)
                .placeholder(R.drawable.ic_music_note)
                .error(R.drawable.ic_music_note)
                .into(holder.albumArt)
        } else {
            holder.albumArt.setImageResource(R.drawable.ic_music_note)
        }

        // Click listener to select song
        holder.itemView.setOnClickListener {
            // Handle song selection - open detail or add to post
            onSongClicked(song)
        }
    }

    override fun getItemCount(): Int = songs.size

    private fun onSongClicked(song: ITunesSong) {
        // Implement song selection logic
        // For example, open a bottom sheet with options to add to favorites or create post
    }
}