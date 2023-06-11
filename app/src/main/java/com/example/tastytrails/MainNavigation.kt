package com.example.tastytrails

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.example.tastytrails.main.ui.RecipeDetailScreen
import com.example.tastytrails.main.ui.SearchScreen
import com.example.tastytrails.main.ui.SearchViewModel
import kotlinx.coroutines.CoroutineScope

private object MainDestinations {
    const val SEARCH_SCREEN = "search"
    const val RECIPE_DETAILS_SCREEN = "recipe_details"
}

@Composable
fun MainNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    coroutineScope: CoroutineScope = rememberCoroutineScope()
) {

    NavHost(
        navController = navController,
        startDestination = "searchFeature"
    ) {
        // Navigation of the main feature: Search Screen
        navigation(
            startDestination = MainDestinations.SEARCH_SCREEN,
            route = "searchFeature"
        ) {

            composable(MainDestinations.SEARCH_SCREEN) {
                val viewModel = it.sharedViewModel<SearchViewModel>(navController)
                SearchScreen(
                    viewModel = viewModel,
                    onRecipeClicked = { navController.navigate(MainDestinations.RECIPE_DETAILS_SCREEN) }
                )
            }

            composable(MainDestinations.RECIPE_DETAILS_SCREEN) {
                val viewModel = it.sharedViewModel<SearchViewModel>(navController)
                RecipeDetailScreen(viewModel = viewModel) { navController.popBackStack() }
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