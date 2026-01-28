package com.example.replay

data class Conversation(
    val userId: String,
    val userName: String,
    val userAvatarUrl: String,
    val lastMessage: String,
    val timestamp: String,
    val unreadCount: Int = 0
)