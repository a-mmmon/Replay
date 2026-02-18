package com.example.replay

import android.app.Application
import com.google.firebase.FirebaseApp

class ReplayApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        // Initialize FavoriteManager
        FavoriteManager.init(this)
    }
}