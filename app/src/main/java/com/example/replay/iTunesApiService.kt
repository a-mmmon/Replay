package com.example.replay

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface iTunesApiService {

    @GET("search")
    fun searchSongs(
        @Query("term") term: String,
        @Query("media") media: String = "music",
        @Query("entity") entity: String = "song",
        @Query("limit") limit: Int = 25
    ): Call<iTunesResponse>
}
