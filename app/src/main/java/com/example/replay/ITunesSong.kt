package com.example.replay

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ITunesSong(
    val trackId: Long,
    val trackName: String,
    val artistName: String,
    val artworkUrl100: String,
    val previewUrl: String = "",
    val collectionName: String = "",
    val trackViewUrl: String = "",
    val releaseDate: String = ""
) : Parcelable