package com.example.replay

data class UserProfile(
    val userId: String,
    val userName: String,
    val userHandle: String,
    val avatarUrl: String,
    var followersCount: Int = 0,
    var followingCount: Int = 0,
    var postsCount: Int = 0,
    var likesCount: Int = 0,
    var streakCount: Int = 0,
    val userPosts: MutableList<Post> = mutableListOf()
)