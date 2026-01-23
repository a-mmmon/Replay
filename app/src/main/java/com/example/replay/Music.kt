// Likely located at: app/src/main/java/com/example/replay/Music.kt

package com.example.replay

data class Music(
    val id: String,
    val title: String,
    val artist: String,
    val album: String, // Add this line
    val coverUrl: String
)
