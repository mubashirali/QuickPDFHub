package com.mobiapps.quickpdfhub.data

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.edit

object ThemeManager {
    private lateinit var prefs: SharedPreferences

    // Backing mutable state is private; public val has no setter → no JVM name clash
    // with setDarkMode(). Kotlin generates setIsDarkMode() for a var named _isDarkMode,
    // not setDarkMode(), so there is no collision.
    private var _isDarkMode by mutableStateOf(false)
    val isDarkMode: Boolean get() = _isDarkMode

    fun init(context: Context) {
        prefs = context.applicationContext.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        _isDarkMode = if (prefs.contains("dark_mode")) {
            prefs.getBoolean("dark_mode", false)
        } else {
            // First launch: mirror system dark mode so the app feels native immediately.
            val nightMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            nightMode == Configuration.UI_MODE_NIGHT_YES
        }
    }

    fun setDarkMode(enabled: Boolean) {
        _isDarkMode = enabled
        prefs.edit { putBoolean("dark_mode", enabled) }
    }
}
