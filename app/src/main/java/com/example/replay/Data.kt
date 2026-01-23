// Create this new file at: app/src/main/java/com/example/replay/Data.ktpackage com.example.replay

data class Music(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val coverUrl: String
)

data class Album(
    val id: String, // Add the 'id' field here
    val name: String,
    val artist: String,
    val coverUrl: String
)

data class Artist(
    val id: String,
    val name: String,
    val coverUrl: String
)

data class Post(
    val userName: String,
    val content: String,
    val musicList: List<Music>
)
    