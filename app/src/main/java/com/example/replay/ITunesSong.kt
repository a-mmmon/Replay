package com.example.replay

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class ITunesSong(
    @SerializedName("trackId")
    val trackId: Long,

    @SerializedName("trackName")
    val trackName: String,

    @SerializedName("artistName")
    val artistName: String,

    @SerializedName("artworkUrl100")
    val artworkUrl100: String,

    @SerializedName("previewUrl")
    val previewUrl: String = "",

    @SerializedName("collectionName")
    val collectionName: String = "",

    @SerializedName("trackViewUrl")
    val trackViewUrl: String = "",

    @SerializedName("releaseDate")
    val releaseDate: String = ""
) : Parcelable