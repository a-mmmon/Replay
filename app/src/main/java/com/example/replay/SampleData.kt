package com.example.replay

object SampleData {

    // ✅ ADDED: Individual music samples that were missing
    val sampleMusic1 = ITunesSong(
        trackId = 1,
        trackName = "Anti-Hero",
        artistName = "Taylor Swift",
        artworkUrl100 = "https://example.com/artwork1.jpg",
        previewUrl = "https://example.com/preview1.mp3",
        collectionName = "Midnights",
        trackViewUrl = "https://music.apple.com/us/album/anti-hero/1",
        releaseDate = "2022-10-21"
    )

    val sampleMusic2 = ITunesSong(
        trackId = 2,
        trackName = "As It Was",
        artistName = "Harry Styles",
        artworkUrl100 = "https://example.com/artwork2.jpg",
        previewUrl = "https://example.com/preview2.mp3",
        collectionName = "Harry's House",
        trackViewUrl = "https://music.apple.com/us/album/as-it-was/2",
        releaseDate = "2022-04-01"
    )

    val sampleMusic3 = ITunesSong(
        trackId = 3,
        trackName = "Flowers",
        artistName = "Miley Cyrus",
        artworkUrl100 = "https://example.com/artwork3.jpg",
        previewUrl = "https://example.com/preview3.mp3",
        collectionName = "Endless Summer Vacation",
        trackViewUrl = "https://music.apple.com/us/album/flowers/3",
        releaseDate = "2023-01-13"
    )

    // Sample songs for testing
    val sampleSongs = listOf(
        sampleMusic1,
        sampleMusic2,
        sampleMusic3,
        ITunesSong(
            trackId = 4,
            trackName = "Calm Down",
            artistName = "Rema & Selena Gomez",
            artworkUrl100 = "https://example.com/artwork4.jpg",
            previewUrl = "https://example.com/preview4.mp3",
            collectionName = "Rave & Roses",
            trackViewUrl = "https://music.apple.com/us/album/calm-down/4",
            releaseDate = "2022-08-25"
        )
    )

    // Sample posts for feed
    val samplePosts = listOf(
        Post(
            postId = "1",
            userId = "user1",
            username = "Taylor Swift",
            userProfileImage = "",
            caption = "Just released my new album!",
            imageUrl = "",
            likes = 1234,
            comments = 89,
            timestamp = System.currentTimeMillis() - 3600000,
            music = sampleSongs[0]
        ),
        Post(
            postId = "2",
            userId = "user2",
            username = "Harry Styles",
            userProfileImage = "",
            caption = "Love this song so much",
            imageUrl = "",
            likes = 567,
            comments = 23,
            timestamp = System.currentTimeMillis() - 7200000,
            music = sampleSongs[1]
        )
    )
}