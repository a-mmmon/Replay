package com.example.replay

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SelectedMusicAdapter(private var musicList: MutableList<Music>) :
    RecyclerView.Adapter<SelectedMusicAdapter.MusicViewHolder>() {

    inner class MusicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val coverImage: ImageView = itemView.findViewById(R.id.musicCover)
        val titleText: TextView = itemView.findViewById(R.id.musicTitle)
        val artistText: TextView = itemView.findViewById(R.id.musicArtist)
        val favoriteButton: ImageButton = itemView.findViewById(R.id.favoriteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_music, parent, false)
        return MusicViewHolder(view)
    }

    override fun onBindViewHolder(holder: MusicViewHolder, position: Int) {
        val music = musicList[position]
        holder.titleText.text = music.title
        holder.artistText.text = music.artist

        // Update favorite button state
        val isFavorite = FavoritesManager.isFavorite(holder.itemView.context, music.id)
        updateFavoriteButton(holder.favoriteButton, isFavorite)

        // Handle favorite button click
        holder.favoriteButton.setOnClickListener {
            if (FavoritesManager.isFavorite(holder.itemView.context, music.id)) {
                FavoritesManager.removeFavorite(holder.itemView.context, music.id)
                updateFavoriteButton(holder.favoriteButton, false)
            } else {
                FavoritesManager.addFavorite(holder.itemView.context, music)
                updateFavoriteButton(holder.favoriteButton, true)
            }
        }
    }

    private fun updateFavoriteButton(button: ImageButton, isFavorite: Boolean) {
        if (isFavorite) {
            button.setImageResource(android.R.drawable.star_big_on)
        } else {
            button.setImageResource(android.R.drawable.star_big_off)
        }
    }

    override fun getItemCount() = musicList.size

    fun updateData(newList: List<Music>) {
        musicList.clear()
        musicList.addAll(newList)
        notifyDataSetChanged()
    }
}