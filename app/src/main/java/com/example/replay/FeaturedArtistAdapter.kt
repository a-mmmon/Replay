package com.example.replay

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class FeaturedArtistAdapter(
    private val artists: MutableList<ITunesSong>,
    private val onClick: (String) -> Unit
) : RecyclerView.Adapter<FeaturedArtistAdapter.ArtistViewHolder>() {

    inner class ArtistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val artistImage: ImageView = itemView.findViewById(R.id.artistImage)
        val artistName: TextView = itemView.findViewById(R.id.artistName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtistViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_featured_artist, parent, false)
        return ArtistViewHolder(view)
    }

    override fun onBindViewHolder(holder: ArtistViewHolder, position: Int) {
        val artist = artists[position]

        holder.artistName.text = artist.artistName

        Glide.with(holder.itemView.context)
            .load(artist.artworkUrl100)
            .circleCrop()
            .into(holder.artistImage)

        holder.itemView.setOnClickListener {
            onClick(artist.artistName)
        }
    }

    override fun getItemCount(): Int = artists.size
}
