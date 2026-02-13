package com.example.replay

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ITunesApiService {

    // For DiscoverFragment (uses Call<ITunesResponse>)
    @GET("search")
    fun searchSongs(
        @Query("term") searchTerm: String,
        @Query("media") media: String = "music",
        @Query("entity") entity: String = "song",
        @Query("limit") limit: Int = 20
    ): Call<ITunesResponse>

    // For MusicSelectionBottomSheet (uses suspend + Response)
    @GET("search")
    suspend fun searchSongsAsync(
        @Query("term") searchTerm: String,
        @Query("media") media: String = "music",
        @Query("entity") entity: String = "song",
        @Query("limit") limit: Int = 20
    ): Response<ITunesResponse>
}