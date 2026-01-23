package com.example.replay

object SampleData {

    // Music data list that DiscoverActivity is looking for
    val music = listOf(
        Music("1", "Anti-Hero", "Taylor Swift", "Midnights", "https://picsum.photos/200/200?random=9"),
        Music("2", "7 rings", "Ariana Grande", "thank u, next", "https://picsum.photos/200/200?random=10"),
        Music("3", "How You Like That", "BLACKPINK", "THE ALBUM", "https://picsum.photos/200/200?random=11"),
        Music("4", "Dynamite", "BTS", "BE", "https://picsum.photos/200/200?random=12")
    )

    // Album data list that DiscoverActivity is looking for
    val albums = listOf(
        Album("1", "locket", "Madison Beer", "https://picsum.photos/200/200?random=5"),
        Album("2", "THE SIN: VANISH", "Enhypen", "https://picsum.photos/200/200?random=6"),
        Album("3", "Midnights", "Taylor Swift", "https://picsum.photos/200/200?random=7")
    )

    // Artist data list that DiscoverActivity is looking for
    val artists = listOf(
        Artist("1", "Taylor Swift", "https://picsum.photos/200/200?random=1"),
        Artist("2", "Ariana Grande", "https://picsum.photos/200/200?random=2"),
        Artist("3", "Black Pink", "https://picsum.photos/200/200?random=3"),
        Artist("4", "BTS", "https://picsum.photos/200/200?random=4")
    )

    val posts = listOf(
        Post(
            userName = "Taylor Swift",
            content = "Such a fun night making music! ✨",
            musicList = listOf(music[0]) // Example: Anti-Hero
        ),
        Post(
            userName = "Ariana Grande",
            content = "In the studio, feeling inspired.",
            musicList = listOf(music[1]) // Example: 7 rings
        ),
        Post(
            userName = "BTS",
            content = "Just wrapped up our tour! Thank you, ARMY! 💜",
            musicList = listOf(music[3]) // Example: Dynamite
        )
    )

}
