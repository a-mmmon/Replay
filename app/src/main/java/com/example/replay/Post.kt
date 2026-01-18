package com.example.replay

data class Post(
    val userName: String,
    val content: String,
    val musicList: List<Music> = emptyList(),
    val likeCount: Int = 0,
    val time: String = "Just now"
)
