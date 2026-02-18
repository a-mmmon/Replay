package com.example.replay

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class ITunesSong(
<<<<<<< HEAD
    val trackId: Long = 0L,           // ← added default
    val trackName: String = "",        // ← added default
    val artistName: String = "",       // ← added default
    val artworkUrl100: String = "",
=======
    @SerializedName("trackId")
    val trackId: Long,

    @SerializedName("trackName")
    val trackName: String,

    @SerializedName("artistName")
    val artistName: String,

    @SerializedName("artworkUrl100")
    val artworkUrl100: String,

    @SerializedName("previewUrl")
>>>>>>> aa23625cdec0aadc5966ebfc9b59bd4c93417628
    val previewUrl: String = "",

    @SerializedName("collectionName")
    val collectionName: String = "",

    @SerializedName("trackViewUrl")
    val trackViewUrl: String = "",

    @SerializedName("releaseDate")
    val releaseDate: String = ""
) : Parcelable