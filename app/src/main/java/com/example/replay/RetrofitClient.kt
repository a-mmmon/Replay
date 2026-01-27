package com.example.replay

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://itunes.apple.com/") // ✅ MUST end with /
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: iTunesApiService by lazy {
        retrofit.create(iTunesApiService::class.java)
    }
}
