package com.example.replay

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ITunesSong(
    val trackId: Long = 0L,           // ← added default
    val trackName: String = "",        // ← added default
    val artistName: String = "",       // ← added default
    val artworkUrl100: String = "",
    val previewUrl: String = "",
    val collectionName: String = "",
    val trackViewUrl: String = "",
    val releaseDate: String = ""
) : Parcelable