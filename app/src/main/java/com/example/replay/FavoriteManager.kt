package com.example.replay

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object FavoritesManager {
    private const val PREFS_NAME = "replay_favorites"
    private const val FAVORITES_KEY = "favorite_songs"
    private val gson = Gson()

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun addFavorite(context: Context, music: Music) {
        val favorites = getFavorites(context).toMutableList()
        if (!favorites.any { it.id == music.id }) {
            favorites.add(music)
            saveFavorites(context, favorites)
        }
    }

    fun removeFavorite(context: Context, musicId: String) {
        val favorites = getFavorites(context).toMutableList()
        favorites.removeAll { it.id == musicId }
        saveFavorites(context, favorites)
    }

    fun isFavorite(context: Context, musicId: String): Boolean {
        return getFavorites(context).any { it.id == musicId }
    }

    fun getFavorites(context: Context): List<Music> {
        val prefs = getPreferences(context)
        val json = prefs.getString(FAVORITES_KEY, null) ?: return emptyList()
        val type = object : TypeToken<List<Music>>() {}.type
        return gson.fromJson(json, type)
    }

    private fun saveFavorites(context: Context, favorites: List<Music>) {
        val prefs = getPreferences(context)
        val json = gson.toJson(favorites)
        prefs.edit().putString(FAVORITES_KEY, json).apply()
    }
}