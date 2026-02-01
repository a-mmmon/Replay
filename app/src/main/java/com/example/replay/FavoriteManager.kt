package com.example.replay

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object FavoriteManager {

    private const val PREFS_NAME = "FavoritesPrefs"
    private const val KEY_FAVORITES = "favorite_songs"
    private var sharedPrefs: SharedPreferences? = null
    private val gson = Gson()

    fun init(context: Context) {
        if (sharedPrefs == null) {
            sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
    }

    fun addToFavorites(song: ITunesSong) {
        val favorites = getFavorites().toMutableList()
        if (!favorites.any { it.trackId == song.trackId }) {
            favorites.add(song)
            saveFavorites(favorites)
        }
    }

    fun removeFromFavorites(song: ITunesSong) {
        val favorites = getFavorites().toMutableList()
        favorites.removeAll { it.trackId == song.trackId }
        saveFavorites(favorites)
    }

    fun isFavorite(song: ITunesSong): Boolean {
        return getFavorites().any { it.trackId == song.trackId }
    }

    fun getFavorites(): List<ITunesSong> {
        val json = sharedPrefs?.getString(KEY_FAVORITES, null) ?: return emptyList()
        val type = object : TypeToken<List<ITunesSong>>() {}.type
        return try {
            gson.fromJson(json, type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveFavorites(favorites: List<ITunesSong>) {
        val json = gson.toJson(favorites)
        sharedPrefs?.edit()?.putString(KEY_FAVORITES, json)?.apply()
    }
}