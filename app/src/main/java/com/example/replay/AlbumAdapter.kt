// In app/src/main/java/com/example/replay/AlbumAdapter.kt

package com.example.replay

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

// FIX: Remove the data class declaration from this file
// data class Album(val title: String, val artistName: String, val coverUrl: String)

class AlbumAdapter(private var albums: List<Album>) :
    RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder>() {

    class AlbumViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Tip: I noticed your IDs in onBindViewHolder are different. Ensure these IDs match your item_album.xml layout.
        val albumCover: ImageView = itemView.findViewById(R.id.albumImage)
        val albumTitle: TextView = itemView.findViewById(R.id.albumName)
        val albumArtist: TextView = itemView.findViewById(R.id.artistName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_album, parent, false)
        return AlbumViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
        val album = albums[position]
        holder.albumTitle.text = album.title
        holder.albumArtist.text = album.artistName

        Glide.with(holder.itemView.context)
            .load(album.coverUrl)
            .into(holder.albumCover)
    }

    override fun getItemCount() = albums.size

    fun updateData(newAlbums: List<Album>) {
        this.albums = newAlbums
        notifyDataSetChanged()
    }
}
