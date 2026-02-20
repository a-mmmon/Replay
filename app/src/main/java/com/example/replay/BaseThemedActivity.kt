package com.example.replay

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

open class BaseThemedActivity : AppCompatActivity() {

    private var appliedThemeRes: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        appliedThemeRes = ThemeManager.getThemeRes(this)
        setTheme(appliedThemeRes)
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        val latestTheme = ThemeManager.getThemeRes(this)
        if (latestTheme != appliedThemeRes) {
            appliedThemeRes = latestTheme
            recreate()
        }
    }
}
