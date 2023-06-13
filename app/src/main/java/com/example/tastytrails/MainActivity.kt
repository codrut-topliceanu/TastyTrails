package com.example.tastytrails

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import com.example.tastytrails.datastore.PreferencesKeys
import com.example.tastytrails.datastore.ThemeSettings
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var dataStore: DataStore<Preferences>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val themeSettingsFlow: Flow<ThemeSettings> = dataStore.data
            .catch {
                Log.e(
                    "SearchViewModel",
                    "userPreferencesFlow dataStore.data Exception : ${it.message}"
                )
                emit(emptyPreferences())
            }.map { preferences ->
                ThemeSettings(
                    darkMode = preferences[PreferencesKeys.DARK_MODE_SETTING] ?: 0,
                    dynamicThemeEnabled = preferences[PreferencesKeys.DYNAMIC_THEME_SETTING] ?: true
                )
            }


        setContent {
            MainNavigation(themeSettingsFlow = themeSettingsFlow)
        }
    }
}

