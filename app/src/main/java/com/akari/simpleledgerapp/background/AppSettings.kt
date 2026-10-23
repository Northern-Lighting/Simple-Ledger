package com.akari.simpleledgerapp.background

import android.content.Context

enum class ThemeMode { System, AlwaysDark, AlwaysLight }

enum class HomeStyle { List, Calendar }

enum class LanguageMode { System, SimplifiedChinese, TraditionalChinese, English }

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.System,
    val homeStyle: HomeStyle = HomeStyle.List,
    val language: LanguageMode = LanguageMode.System,
)

class SettingsStore(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun load(): AppSettings = AppSettings(
        themeMode = prefs.getString(KEY_THEME, null)
            ?.let { name -> ThemeMode.entries.firstOrNull { it.name == name } }
            ?: ThemeMode.System,
        homeStyle = prefs.getString(KEY_HOME, null)
            ?.let { name -> HomeStyle.entries.firstOrNull { it.name == name } }
            ?: HomeStyle.List,
        language = prefs.getString(KEY_LANGUAGE, null)
            ?.let { name -> LanguageMode.entries.firstOrNull { it.name == name } }
            ?: LanguageMode.System,
    )

    fun save(settings: AppSettings) {
        prefs.edit()
            .putString(KEY_THEME, settings.themeMode.name)
            .putString(KEY_HOME, settings.homeStyle.name)
            .putString(KEY_LANGUAGE, settings.language.name)
            .apply()
    }

    private companion object {
        const val PREFS_NAME = "app_settings"
        const val KEY_THEME = "theme_mode"
        const val KEY_HOME = "home_style"
        const val KEY_LANGUAGE = "language"
    }
}
