package com.example.replay

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class DiscoverAdapter(
    private val musicList: List<Music>,
    private val albumList: List<Album>,
    private val artistList: List<Artist>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_ARTIST = 0
        private const val TYPE_ALBUM = 1
        private const val TYPE_MUSIC = 2
    }

    // Combined list
    private val items: List<Any> =
        artistList + albumList + musicList

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is Artist -> TYPE_ARTIST
            is Album -> TYPE_ALBUM
            else -> TYPE_MUSIC
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {

        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            TYPE_ARTIST -> ArtistViewHolder(
                inflater.inflate(R.layout.item_artist, parent, false)
            )
            TYPE_ALBUM -> AlbumViewHolder(
                inflater.inflate(R.layout.item_album, parent, false)
            )
            else -> MusicViewHolder(
                inflater.inflate(R.layout.item_music, parent, false)
            )
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        when (val item = items[position]) {
            is Artist -> (holder as ArtistViewHolder).bind(item)
            is Album -> (holder as AlbumViewHolder).bind(item)
            is Music -> (holder as MusicViewHolder).bind(item)
        }
    }

    override fun getItemCount(): Int = items.size

    // =====================
    // VIEW HOLDERS
    // =====================

    class ArtistViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        private val name: TextView =
            itemView.findViewById(R.id.artistName)

        fun bind(artist: Artist) {
            name.text = artist.name
        }
    }

    class AlbumViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        private val albumName: TextView =
            itemView.findViewById(R.id.albumName)
        private val artistName: TextView =
            itemView.findViewById(R.id.artistName)
        private val albumImage: ImageView =
            itemView.findViewById(R.id.albumImage)

        fun bind(album: Album) {
            albumName.text = album.name
            artistName.text = album.artist

            Glide.with(itemView.context)
                .load(album.coverUrl)
                .into(albumImage)
        }
    }

    class MusicViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        private val title: TextView =
            itemView.findViewById(R.id.musicTitle)
        private val artist: TextView =
            itemView.findViewById(R.id.musicArtist)
        // Remove or comment out the favorite ImageView if it doesn't exist in your layout
        // private val favorite: ImageView =
        //     itemView.findViewById(R.id.musicFavorite)

        fun bind(music: Music) {
            title.text = music.title
            artist.text = music.artist
        }
    }
}