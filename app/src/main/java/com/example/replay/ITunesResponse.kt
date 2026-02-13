package com.example.replay

import com.google.gson.annotations.SerializedName

data class ITunesResponse(
    @SerializedName("resultCount")
    val resultCount: Int,

    @SerializedName("results")
    val results: List<ITunesSong>
)