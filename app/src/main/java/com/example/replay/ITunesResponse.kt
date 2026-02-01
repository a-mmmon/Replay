package com.example.replay

import com.google.gson.annotations.SerializedName

data class ITunesResponse(
    @SerializedName("resultCount")
    val resultCount: Int = 0,

    @SerializedName("results")
    val results: List<ITunesSong> = emptyList()
)