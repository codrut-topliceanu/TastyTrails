package com.example.tastytrails.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey

object PreferencesKeys {
    val DARK_MODE_SETTING = intPreferencesKey("dark_mode_setting")
    val DYNAMIC_THEME_SETTING = booleanPreferencesKey("dynamic_theme_setting")
}