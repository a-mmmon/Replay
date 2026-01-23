package com.example.replay

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class DiscoverAdapter(
    private val albums: List<Album>,
    private val artists: List<Artist>,
    private val songs: List<Music>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_ALBUMS = 0
        private const val TYPE_ARTISTS = 1
        private const val TYPE_SONGS = 2
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> TYPE_ALBUMS
            1 -> TYPE_ARTISTS
            2 -> TYPE_SONGS
            else -> TYPE_ALBUMS
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_discover_section, parent, false)
        return DiscoverSectionViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DiscoverSectionViewHolder) {
            when (position) {
                0 -> holder.bind("Trending Albums", albums)
                1 -> holder.bind("Popular Artists", artists)
                2 -> holder.bind("Top Songs", songs)
            }
        }
    }

    override fun getItemCount(): Int = 3

    class DiscoverSectionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvSectionTitle: TextView = itemView.findViewById(R.id.tvSectionTitle)
        private val rvSection: RecyclerView = itemView.findViewById(R.id.rvSection)

        fun bind(title: String, items: List<Any>) {
            tvSectionTitle.text = title

            when {
                items.firstOrNull() is Album -> {
                    val adapter = AlbumAdapter(items as List<Album>)
                    rvSection.layoutManager = LinearLayoutManager(
                        itemView.context,
                        LinearLayoutManager.HORIZONTAL,
                        false
                    )
                    rvSection.adapter = adapter
                }
                items.firstOrNull() is Artist -> {
                    val adapter = ArtistAdapter(items as List<Artist>)
                    rvSection.layoutManager = LinearLayoutManager(
                        itemView.context,
                        LinearLayoutManager.HORIZONTAL,
                        false
                    )
                    rvSection.adapter = adapter
                }
                // ... inside DiscoverSectionViewHolder class
                items.firstOrNull() is Music -> {
                    val adapter = SelectedMusicAdapter((items as List<Music>).toMutableList())
                    rvSection.layoutManager = LinearLayoutManager(itemView.context)
                    rvSection.adapter = adapter
                }
// ...

            }
        }
    }
}