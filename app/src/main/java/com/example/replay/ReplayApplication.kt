package com.example.replay

import android.app.Application

class ReplayApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize FavoriteManager
        FavoriteManager.init(this)
    }
}