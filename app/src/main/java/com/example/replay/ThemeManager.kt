package com.example.replay

import android.content.Context

object ThemeManager {

    private const val PREFS_NAME = "replay_theme_prefs"
    private const val KEY_THEME = "selected_theme"

    const val THEME_DEFAULT = "default"
    const val THEME_OCEAN = "ocean"
    const val THEME_SUNSET = "sunset"
    const val THEME_ROYAL = "royal"

    fun getSavedTheme(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getString(KEY_THEME, THEME_DEFAULT) ?: THEME_DEFAULT
        return when (saved) {
            THEME_DEFAULT, THEME_OCEAN, THEME_SUNSET, THEME_ROYAL -> saved
            else -> THEME_DEFAULT
        }
    }

    fun saveTheme(context: Context, themeKey: String): Boolean {
        val current = getSavedTheme(context)
        if (current == themeKey) return false

        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_THEME, themeKey)
            .apply()
        return true
    }

    fun getThemeRes(context: Context): Int {
        return when (getSavedTheme(context)) {
            THEME_DEFAULT -> R.style.Theme_Replay_Default
            THEME_SUNSET -> R.style.Theme_Replay_Sunset
            THEME_ROYAL -> R.style.Theme_Replay_Royal
            else -> R.style.Theme_Replay
        }
    }

    fun getDefaultAvatarRes(context: Context): Int {
        return when (getSavedTheme(context)) {
            THEME_DEFAULT -> R.drawable.ic_android_placeholder_ocean
            THEME_SUNSET -> R.drawable.ic_android_placeholder_sunset
            THEME_ROYAL -> R.drawable.ic_android_placeholder_royal
            else -> R.drawable.ic_android_placeholder_ocean
        }
    }

    fun getThemeIndex(context: Context): Int {
        return when (getSavedTheme(context)) {
            THEME_OCEAN -> 1
            THEME_SUNSET -> 2
            THEME_ROYAL -> 3
            else -> 0
        }
    }
}
