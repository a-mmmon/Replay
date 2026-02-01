package com.example.replay

object SampleData {

    // Sample music data (using ITunesSong from DataModels.kt)
    val sampleMusic1 = ITunesSong(
        trackId = 1L,
        trackName = "Blinding Lights",
        artistName = "The Weeknd",
        collectionName = "After Hours",
        artworkUrl100 = "",
        previewUrl = "",
        trackTimeMillis = 200000L,
        releaseDate = "2020-03-20",
        primaryGenreName = "Pop"
    )

    val sampleMusic2 = ITunesSong(
        trackId = 2L,
        trackName = "Levitating",
        artistName = "Dua Lipa",
        collectionName = "Future Nostalgia",
        artworkUrl100 = "",
        previewUrl = "",
        trackTimeMillis = 203000L,
        releaseDate = "2020-03-27",
        primaryGenreName = "Pop"
    )

    val sampleMusic3 = ITunesSong(
        trackId = 3L,
        trackName = "Dynamite",
        artistName = "BTS",
        collectionName = "BE",
        artworkUrl100 = "",
        previewUrl = "",
        trackTimeMillis = 199000L,
        releaseDate = "2020-11-20",
        primaryGenreName = "Pop"
    )

    // Sample posts for home feed (using Post from DataModels.kt)
    val posts = mutableListOf(
        Post(
            postId = "post1",
            userId = "user_taylor",
            username = "Taylor Swift",
            userProfileImage = "",
            caption = "Such a fun night making music! ✨",
            imageUrl = "",
            likes = 5,
            comments = 0,
            timestamp = System.currentTimeMillis() - 60000L, // 1 minute ago
            music = sampleMusic1
        ),
        Post(
            postId = "post2",
            userId = "user_bts",
            username = "BTS",
            userProfileImage = "",
            caption = "Have a wonderful concert! ✨",
            imageUrl = "",
            likes = 1000,
            comments = 50,
            timestamp = System.currentTimeMillis() - 3600000L, // 1 hour ago
            music = sampleMusic2
        ),
        Post(
            postId = "post3",
            userId = "user_blackpink",
            username = "Black Pink",
            userProfileImage = "",
            caption = "How amazing is this new album! ✨",
            imageUrl = "",
            likes = 2000,
            comments = 100,
            timestamp = System.currentTimeMillis() - 86400000L, // 1 day ago
            music = sampleMusic3
        )
    )

    // Current user profile (using UserProfile from DataModels.kt)
    val currentUserProfile = UserProfile(
        userId = "user123",
        username = "Uri",
        profileImage = "",
        bio = "Music lover 🎵",
        followers = 245,
        following = 189
    )

    // Sample conversations for messages (using Conversation from DataModels.kt)
    val conversations = listOf(
        Conversation(
            conversationId = "conv1",
            otherUserId = "user_taylor",
            otherUserName = "Taylor Swift",
            otherUserProfileImage = "",
            lastMessage = "Thanks for sharing that song!",
            timestamp = System.currentTimeMillis() - 7200000L, // 2 hours ago
            unreadBadge = 2,
            messages = emptyList()
        ),
        Conversation(
            conversationId = "conv2",
            otherUserId = "user_ariana",
            otherUserName = "Ariana Grande",
            otherUserProfileImage = "",
            lastMessage = "Let's collaborate sometime",
            timestamp = System.currentTimeMillis() - 18000000L, // 5 hours ago
            unreadBadge = 0,
            messages = emptyList()
        ),
        Conversation(
            conversationId = "conv3",
            otherUserId = "user_bts",
            otherUserName = "BTS",
            otherUserProfileImage = "",
            lastMessage = "Check out our new album!",
            timestamp = System.currentTimeMillis() - 86400000L, // 1 day ago
            unreadBadge = 1,
            messages = emptyList()
        ),
        Conversation(
            conversationId = "conv4",
            otherUserId = "user_ed",
            otherUserName = "Ed Sheeran",
            otherUserProfileImage = "",
            lastMessage = "See you at the concert!",
            timestamp = System.currentTimeMillis() - 172800000L, // 2 days ago
            unreadBadge = 0,
            messages = emptyList()
        )
    )

    // Sample users to message
    val usersToMessage = listOf(
        UserProfile(
            userId = "user_taylor",
            username = "Taylor Swift",
            profileImage = "",
            bio = "Singer-songwriter",
            followers = 1000000,
            following = 500
        ),
        UserProfile(
            userId = "user_ariana",
            username = "Ariana Grande",
            profileImage = "",
            bio = "Artist",
            followers = 2000000,
            following = 600
        ),
        UserProfile(
            userId = "user_bts",
            username = "BTS",
            profileImage = "",
            bio = "K-Pop Group",
            followers = 5000000,
            following = 100
        ),
        UserProfile(
            userId = "user_ed",
            username = "Ed Sheeran",
            profileImage = "",
            bio = "Musician",
            followers = 1500000,
            following = 400
        )
    )
}