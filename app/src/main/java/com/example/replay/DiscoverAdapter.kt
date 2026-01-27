package com.example.replay

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class DiscoverAdapter(
    private val songs: MutableList<Music>
) : RecyclerView.Adapter<DiscoverAdapter.DiscoverViewHolder>() {

    inner class DiscoverViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val title: TextView = itemView.findViewById(R.id.musicTitle)
        val artist: TextView = itemView.findViewById(R.id.musicArtist)
        val cover: ImageView = itemView.findViewById(R.id.musicCover)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiscoverViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_music, parent, false)
        return DiscoverViewHolder(view)
    }

    override fun onBindViewHolder(holder: DiscoverViewHolder, position: Int) {
        val song = songs[position]

        holder.title.text = song.title
        holder.artist.text = song.artist

        Glide.with(holder.itemView.context)
            .load(song.coverUrl)
            .into(holder.cover)
    }

    override fun getItemCount(): Int = songs.size
}
