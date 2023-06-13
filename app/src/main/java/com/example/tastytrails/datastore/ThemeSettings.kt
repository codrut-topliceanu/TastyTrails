package com.example.tastytrails.datastore

import kotlinx.serialization.Serializable

@Serializable
/**
 * Used to control app theming
 */
data class ThemeSettings(
    val darkMode: Int = DarkMode.Auto.ordinal,
    val dynamicThemeEnabled: Boolean = true
) {
    @Serializable
    enum class DarkMode {
        Auto,
        Dark,
        Light
    }
}