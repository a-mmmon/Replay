package com.example.replay

data class Post(
    val userName: String,
    val userHandle: String,        // ADD THIS
    val userAvatarUrl: String,     // ADD THIS
    val content: String,
    val musicList: List<Music>,
    val timestamp: String,          // ADD THIS
    var likesCount: Int = 0        // ADD THIS
)