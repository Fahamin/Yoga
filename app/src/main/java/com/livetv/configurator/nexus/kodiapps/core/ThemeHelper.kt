package com.livetv.configurator.nexus.kodiapps.core

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

object ThemeHelper {

    fun applySavedTheme(context: Context) {
        applyTheme(Prefs(context).getPref(Constant.PREF_THEME_MODE, Constant.THEME_SYSTEM))
    }

    fun applyTheme(mode: String?) {
        val nightMode = when (mode) {
            Constant.THEME_LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            Constant.THEME_DARK -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(nightMode)
    }
}
