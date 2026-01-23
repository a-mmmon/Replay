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

    class ArtistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val artistImage: ImageView =
            itemView.findViewById(R.id.artist_image)
        val artistName: TextView =
            itemView.findViewById(R.id.artist_name)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ArtistViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_artist, parent, false)
        return ArtistViewHolder(view)
    }

    override fun onBindViewHolder(holder: ArtistViewHolder, position: Int) {
        val artist = artists[position]

        holder.artistName.text = artist.name

        Glide.with(holder.itemView.context)
            .load(artist.imageUrl)
            .circleCrop()
            .into(holder.artistImage)
    }

    override fun getItemCount(): Int = artists.size
}
