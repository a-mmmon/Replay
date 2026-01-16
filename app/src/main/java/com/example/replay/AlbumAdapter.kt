package com.example.replay 

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ArtistAdapter(
    private var artists: List<Artist>
) : RecyclerView.Adapter<ArtistAdapter.ArtistViewHolder>() {

    inner class ArtistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgArtist: ImageView = itemView.findViewById(R.id.imgArtist)
        val tvArtistName: TextView = itemView.findViewById(R.id.tvArtistName)

        fun bind(artist: Artist) {
            tvArtistName.text = artist.name

            Glide.with(itemView.context)
                .load(artist.imageUrl)
                .placeholder(R.drawable.ic_artist_placeholder)
                .into(imgArtist)

            itemView.setOnClickListener {
                // Handle artist click
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtistViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_artist, parent, false)
        return ArtistViewHolder(view)
    }

    override fun onBindViewHolder(holder: ArtistViewHolder, position: Int) {
        holder.bind(artists[position])
    }

    override fun getItemCount(): Int = artists.size

    fun updateData(newArtists: List<Artist>) {
        artists = newArtists
        notifyDataSetChanged()
    }
}