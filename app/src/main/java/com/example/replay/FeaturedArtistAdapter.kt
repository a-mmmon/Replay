package com.example.replay

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView

class FeaturedArtistAdapter(
    private val artists: List<String>,
    private val onArtistClick: (String) -> Unit
) : RecyclerView.Adapter<FeaturedArtistAdapter.ArtistViewHolder>() {

    inner class ArtistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val artistImage: ShapeableImageView = itemView.findViewById(R.id.artist_image)  // ✅ FIXED: artistImage → artist_image
        val artistName: TextView = itemView.findViewById(R.id.artist_name)  // ✅ FIXED: artistName → artist_name
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtistViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_artist, parent, false)
        return ArtistViewHolder(view)
    }

    override fun onBindViewHolder(holder: ArtistViewHolder, position: Int) {
        val artist = artists[position]
        holder.artistName.text = artist

        // Set placeholder image (you can customize per artist if needed)
        holder.artistImage.setImageResource(R.drawable.ic_android_placeholder)

        holder.itemView.setOnClickListener {
            onArtistClick(artist)
        }
    }

    override fun getItemCount(): Int = artists.size
}