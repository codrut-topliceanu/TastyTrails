package com.example.tastytrails

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.example.tastytrails.datastore.ThemeSettings
import com.example.tastytrails.main.ui.RecipeDetailScreen
import com.example.tastytrails.main.ui.SearchScreen
import com.example.tastytrails.main.ui.SearchViewModel
import com.example.tastytrails.ui.theme.TastyTrailsTheme
import kotlinx.coroutines.flow.Flow

sealed class AppFeatures {
    sealed class Search(val route: String) {
        object SearchScreen : Search("search_screen")
        object RecipeDetailsScreen : Search("recipe_details_screen")
    }
}

@Composable
fun MainNavigation(
    navController: NavHostController = rememberNavController(),
    themeSettingsFlow: Flow<ThemeSettings>
) {

    /**
     * Tracks app theming: dark/light mode and dynamic theme
     */
    val themeSettingsState by themeSettingsFlow.collectAsStateWithLifecycle(ThemeSettings())

    TastyTrailsTheme(themeSettingsState) {
        NavHost(
            navController = navController,
            startDestination = AppFeatures.Search::javaClass.name
        ) {
            // Navigation of the main feature: Search Screen
            navigation(
                startDestination = AppFeatures.Search.SearchScreen.route,
                route = AppFeatures.Search::javaClass.name
            ) {

                composable(AppFeatures.Search.SearchScreen.route) {
                    val searchViewModel = it.sharedViewModel<SearchViewModel>(navController)
                    SearchScreen(
                        searchViewModel = searchViewModel,
                        onRecipeClicked = { navController.navigate(AppFeatures.Search.RecipeDetailsScreen.route) }
                    )
                }

                composable(AppFeatures.Search.RecipeDetailsScreen.route) {
                    val viewModel = it.sharedViewModel<SearchViewModel>(navController)
                    RecipeDetailScreen(viewModel = viewModel) { navController.popBackStack() }
                }
            }

        }
    }
}

/**
 * Returns a shared instance of a ViewModel that can be accessed by multiple composable functions
 * within the same navigation graph. This function retrieves the ViewModel instance scoped to the
 * parent destination of the current destination, or the root destination if the current destination
 * has no parent. If there is no parent destination, this function returns a new instance of the
 * ViewModel.
 *
 * @param navController The NavController used for navigation.
 *
 * @returns A shared instance of the ViewModel.
 */
@Composable
inline fun <reified T : ViewModel> NavBackStackEntry.sharedViewModel(navController: NavController): T {
    val navGraphRoute = destination.parent?.route ?: return hiltViewModel()
    val parentEntry = remember(this) {
        navController.getBackStackEntry(navGraphRoute)
    }
    return hiltViewModel(parentEntry)
}