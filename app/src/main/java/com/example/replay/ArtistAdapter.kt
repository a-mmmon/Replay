// In app/src/main/java/com/example/replay/ArtistAdapter.kt

package com.example.replay

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.replay.Artist

// Define the Artist data class if you haven't already

class ArtistAdapter(private var artists: List<Artist>) :
    RecyclerView.Adapter<ArtistAdapter.ArtistViewHolder>() {

    // The ViewHolder class that holds the views for each item
    class ArtistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val artistImage: ImageView = itemView.findViewById(R.id.artist_image) // Make sure you have these IDs in your item layout XML
        val artistName: TextView = itemView.findViewById(R.id.artist_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtistViewHolder {
        // You need to create a layout file e.g., 'item_artist.xml'
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_artist, parent, false)
        return ArtistViewHolder(view)
    }

    override fun onBindViewHolder(holder: ArtistViewHolder, position: Int) {
        val artist = artists[position]
        holder.artistName.text = artist.name
        Glide.with(holder.itemView.context)
            .load(artist.imageUrl)
            .circleCrop() // To make the image circular
            .into(holder.artistImage)
    }

    override fun getItemCount(): Int = artists.size

    // --- FIX: Add this function ---
    fun updateData(newArtists: List<Artist>) {
        this.artists = newArtists
        notifyDataSetChanged() // This tells the RecyclerView to refresh
    }
}
