package com.example.replay

object SampleData {

    // Sample music data
    val sampleMusic1 = Music(
        id = "1",
        title = "Blinding Lights",
        artist = "The Weeknd",
        album = "After Hours",
        coverUrl = ""
    )

    val sampleMusic2 = Music(
        id = "2",
        title = "Levitating",
        artist = "Dua Lipa",
        album = "Future Nostalgia",
        coverUrl = ""
    )

    val sampleMusic3 = Music(
        id = "3",
        title = "Dynamite",
        artist = "BTS",
        album = "BE",
        coverUrl = ""
    )

    // Sample posts for home feed
    val posts = mutableListOf(
        Post(
            userName = "Taylor Swift",
            userHandle = "@taylorswift",      // ADD THIS
            userAvatarUrl = "",                // ADD THIS
            content = "Such a fun night making music! ✨",
            musicList = listOf(sampleMusic1),
            timestamp = "Just now",            // ADD THIS
            likesCount = 5                   // ADD THIS (optional, defaults to 0)
        ),
        Post(
            userName = "BTS",
            userHandle = "@bts",      // ADD THIS
            userAvatarUrl = "",                // ADD THIS
            content = "Have a wonderful concert! ✨",
            musicList = listOf(sampleMusic1),
            timestamp = "An hour ago",            // ADD THIS
            likesCount = 1000                   // ADD THIS (optional, defaults to 0)
        ),
        Post(
            userName = "Black Pink",
            userHandle = "@blackpink",      // ADD THIS
            userAvatarUrl = "",                // ADD THIS
            content = "How amazing is this new album! ✨",
            musicList = listOf(sampleMusic1),
            timestamp = "Yesterday",            // ADD THIS
            likesCount = 2000                    // ADD THIS (optional, defaults to 0)
        )
    )

    // Current user profile
    val currentUserProfile = UserProfile(
        userId = "user123",
        userName = "Uri",
        userHandle = "@uri",
        avatarUrl = "",
        followersCount = 245,
        followingCount = 189,
        postsCount = 0,
        likesCount = 12,
        streakCount = 5,
        userPosts = mutableListOf() // User's posts will be added here when they create posts
    )

    // Sample conversations for messages
    val conversations = listOf(
        Conversation(
            userId = "user1",
            userName = "Taylor Swift",
            userAvatarUrl = "",
            lastMessage = "Thanks for sharing that song!",
            timestamp = "2h",
            unreadCount = 2
        ),
        Conversation(
            userId = "user2",
            userName = "Ariana Grande",
            userAvatarUrl = "",
            lastMessage = "Let's collaborate sometime",
            timestamp = "5h",
            unreadCount = 0
        ),
        Conversation(
            userId = "user3",
            userName = "BTS",
            userAvatarUrl = "",
            lastMessage = "Check out our new album!",
            timestamp = "1d",
            unreadCount = 1
        ),
        Conversation(
            userId = "user4",
            userName = "Ed Sheeran",
            userAvatarUrl = "",
            lastMessage = "See you at the concert!",
            timestamp = "2d",
            unreadCount = 0
        )
    )

    // Sample albums for discover
    val albums = listOf(
        Album(
            id = "album1",
            name = "After Hours",
            artist = "The Weeknd",
            coverUrl = ""
        ),
        Album(
            id = "album2",
            name = "Future Nostalgia",
            artist = "Dua Lipa",
            coverUrl = ""
        ),
        Album(
            id = "album3",
            name = "BE",
            artist = "BTS",
            coverUrl = ""
        )
    )

    // Sample artists for discover
    val artists = listOf(
        Artist(
            id = "artist1",
            name = "The Weeknd",
            imageUrl = ""  // ✅ Correct parameter name
        ),
        Artist(
            id = "artist2",
            name = "Dua Lipa",
            imageUrl = ""  // ✅ Correct parameter name
        ),
        Artist(
            id = "artist3",
            name = "BTS",
            imageUrl = ""  // ✅ Correct parameter name
        )
    )
}