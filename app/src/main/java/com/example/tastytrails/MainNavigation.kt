package com.example.tastytrails

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tastytrails.main.ui.RecipeDetailScreen
import com.example.tastytrails.main.ui.SearchScreen
import kotlinx.coroutines.CoroutineScope

private object MainDestinations {
    const val SEARCH_SCREEN = "search"
    const val RECIPE_DETAILS_SCREEN = "recipe_details"
}

@Composable
fun MainNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    startDestination: String = MainDestinations.SEARCH_SCREEN,
) {

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(MainDestinations.SEARCH_SCREEN) {
            SearchScreen(
                onRecipeClicked = { navController.navigate(MainDestinations.RECIPE_DETAILS_SCREEN) }
            )
        }

        composable(MainDestinations.RECIPE_DETAILS_SCREEN) {
            RecipeDetailScreen { navController.popBackStack() }
        }

    }
}