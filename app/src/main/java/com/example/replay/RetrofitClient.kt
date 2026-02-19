package com.example.replay

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "https://itunes.apple.com/"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Main API service (used by DiscoverFragment and others)
    val api: ITunesApiService by lazy {
        retrofit.create(ITunesApiService::class.java)
    }

    // Alias for compatibility with MusicSelectionBottomSheet
    val iTunesService: ITunesApiService by lazy {
        api
    }
}