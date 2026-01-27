package com.example.replay

data class iTunesSong(
    val trackId: Long,
    val trackName: String,
    val artistName: String,
    val collectionName: String?,
    val artworkUrl100: String?,
    val previewUrl: String?
)
