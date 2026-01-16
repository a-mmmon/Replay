package com.example.replay

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

// RecyclerView Adapter for displaying album list
class AlbumAdapter(
    private var albums: List<Album>
) : RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder>() {

    // ViewHolder holds album item views
    inner class AlbumViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val albumImage: ImageView = itemView.findViewById(R.id.albumImage)
        val albumName: TextView = itemView.findViewById(R.id.albumName)
        val artistName: TextView = itemView.findViewById(R.id.artistName)

        // Bind album data to views
        fun bind(album: Album) {
            albumName.text = album.name
            artistName.text = album.artistName

            // Load album image using Glide
            Glide.with(itemView.context)
                .load(album.imageUrl)
                .placeholder(R.drawable.ic_album_placeholder)
                .into(albumImage)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_album, parent, false)
        return AlbumViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
        holder.bind(albums[position])
    }

    override fun getItemCount(): Int = albums.size

    // Update album list and refresh RecyclerView
    fun updateData(newAlbums: List<Album>) {
        albums = newAlbums
        notifyDataSetChanged()
    }
}
