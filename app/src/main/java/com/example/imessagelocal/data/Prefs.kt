package com.example.imessagelocal.data

import android.content.Context

data class Settings(
    val darkMode: Int = 0, // 0 = system, 1 = light, 2 = dark
    val fontScale: Float = 1f,
    val sound: Boolean = true,
)

class Prefs(context: Context) {
    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    fun load() = Settings(
        darkMode = prefs.getInt("dark", 0),
        fontScale = prefs.getFloat("font", 1f),
        sound = prefs.getBoolean("sound", true),
    )

    fun save(s: Settings) {
        prefs.edit()
            .putInt("dark", s.darkMode)
            .putFloat("font", s.fontScale)
            .putBoolean("sound", s.sound)
            .apply()
    }
}
