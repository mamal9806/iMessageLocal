package com.example.imessagelocal.data

import android.content.Context

data class Settings(
    val darkMode: Int = 0,
    val fontScale: Float = 1f,
    val sound: Boolean = true,
    val defaultSimSubscriptionId: Int = -1,
    val notificationSoundUri: String? = null,
    val accentTheme: Int = 0,
    val language: Int = 0,
    val setupComplete: Boolean = false,
)

class Prefs(context: Context) {
    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    fun load() = Settings(
        darkMode = prefs.getInt("dark", 0),
        fontScale = prefs.getFloat("font", 1f),
        sound = prefs.getBoolean("sound", true),
        defaultSimSubscriptionId = prefs.getInt("default_sim", -1),
        notificationSoundUri = prefs.getString("notification_sound_uri", null),
        accentTheme = prefs.getInt("accent_theme", 0),
        language = prefs.getInt("language", 0),
        setupComplete = prefs.getBoolean("setup_complete", false),
    )

    fun save(s: Settings) {
        prefs.edit()
            .putInt("dark", s.darkMode)
            .putFloat("font", s.fontScale)
            .putBoolean("sound", s.sound)
            .putInt("default_sim", s.defaultSimSubscriptionId)
            .putString("notification_sound_uri", s.notificationSoundUri)
            .putInt("accent_theme", s.accentTheme)
            .putInt("language", s.language)
            .putBoolean("setup_complete", s.setupComplete)
            .apply()
    }
}
