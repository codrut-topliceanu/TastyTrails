package com.example.tastytrails

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.tastytrails.main.ui.SearchViewModel
import com.example.tastytrails.main.ui.SearchScreen
import com.example.tastytrails.ui.theme.TastyTrailsTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TastyTrailsTheme {
                val searchViewModel = hiltViewModel<SearchViewModel>()

                MainNavigation()
            }
        }
    }
}

