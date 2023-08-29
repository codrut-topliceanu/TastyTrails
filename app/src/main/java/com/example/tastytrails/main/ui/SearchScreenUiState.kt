package com.example.tastytrails.main.ui

import com.example.tastytrails.main.domain.Recipe
import kotlin.random.Random

/**
 * Used for displaying different lists in the SearchScreen
 */
enum class ListDisplayMode {
    CURRENT_SEARCH,
    PREVIOUSLY_VIEWED,
    FAVORITES
}

/**
 * Modes to filter the currently displayed list.
 */
enum class FilterOptions {
    A_Z,
    Z_A,
    HEALTH_SCORE,
    NONE
}

/**
 * The main UI state class for the [SearchScreen]
 * @param inProgress a web call or some other work is being done so buttons should be disabled if true.
 * @param searchQuery string to search for a recipe OR ingredients using the API.
 * @param searchByName if true the API call will search by recipe name, else it will search by ingredients.
 * @param currentSort the currently selected sort method for the SearchScreen list.
 * @param listDisplayMode used to switch between current search results, previously viewed recipes and favorites.
 * @param recipesList the list of currently displayed recipes.
 * @param currentlySelectedRecipe used to easily pass a [Recipe] to the [RecipeDetailScreen] for display.
 */
data class SearchScreenUiState(
    val inProgress: Boolean = false,
    val searchQuery: String = "",
    val searchByName: Boolean = true,
    val currentSort: FilterOptions = FilterOptions.NONE,
    val listDisplayMode: ListDisplayMode = ListDisplayMode.CURRENT_SEARCH,
    val recipesList: List<Recipe> = listOf(),
    val resultsOffset: Int = 0,
    val currentlySelectedRecipe: Recipe? = null,
)

data class SnackBarMessage(
    val id: Int = Random.nextInt(),
    val message: String = "",
    val messageStringId: Int? = null
)

/**
 * Represents a stateAction that modifies the UI state in some way.
 */
interface StateAction<T : Any> {
    fun updateState(state: T): T
}

/**
 * List of actions available in SearchScreen
 */
sealed class SearchScreenStateAction : StateAction<SearchScreenUiState> {

    /**
     * Updates the search query AND resets the [SearchScreenUiState.resultsOffset]
     * so that LoadMore button doesn't mistakenly search for more results with a different
     * query.
     */
    data class UpdateSearchQuery(val searchQuery: String) : SearchScreenStateAction() {
        override fun updateState(state: SearchScreenUiState): SearchScreenUiState =
            state.copy(searchQuery = searchQuery, resultsOffset = 0)
    }

    data class UpdateShowInProgress(val showLoading: Boolean) : SearchScreenStateAction() {
        override fun updateState(state: SearchScreenUiState): SearchScreenUiState =
            state.copy(inProgress = showLoading)
    }

    /**
     * Updates the [searchByName] value and deletes the current searchQuery, while also
     * resetting [SearchScreenUiState.resultsOffset] so that LoadMore doesn't try to do a
     * search with different query parameters.
     */
    data class UpdateSearchByName(val searchByName: Boolean) : SearchScreenStateAction() {
        override fun updateState(state: SearchScreenUiState): SearchScreenUiState =
            state.copy(searchByName = searchByName, searchQuery = "", resultsOffset = 0)
    }

    /**
     * Changes current sort setting AND updates the current recipesList to match.
     */
    data class UpdateCurrentSort(val currentSort: FilterOptions) : SearchScreenStateAction() {
        override fun updateState(state: SearchScreenUiState): SearchScreenUiState {
            return when (currentSort) {
                FilterOptions.A_Z -> state.copy(
                    currentSort = currentSort,
                    recipesList = state.recipesList.sortedBy { it.title })

                FilterOptions.Z_A -> state.copy(
                    currentSort = currentSort,
                    recipesList = state.recipesList.sortedByDescending { it.title })

                FilterOptions.HEALTH_SCORE -> state.copy(
                    currentSort = currentSort,
                    recipesList = state.recipesList.sortedByDescending { it.healthScore })

                FilterOptions.NONE -> state.copy(
                    currentSort = currentSort,
                    recipesList = state.recipesList
                )
            }
        }
    }

    /**
     * Updates the list of recipes while also sorting them,
     * and updates [SearchScreenUiState.resultsOffset] to be equal to the current list size.
     */
    data class UpdateRecipesListWithSort(
        val recipesList: List<Recipe>,
        val currentSort: FilterOptions
    ) : SearchScreenStateAction() {
        override fun updateState(state: SearchScreenUiState): SearchScreenUiState {
            return when (currentSort) {
                FilterOptions.A_Z -> state.copy(
                    currentSort = currentSort,
                    recipesList = recipesList.sortedBy { it.title },
                    resultsOffset = recipesList.size)

                FilterOptions.Z_A -> state.copy(
                    currentSort = currentSort,
                    recipesList = recipesList.sortedByDescending { it.title },
                    resultsOffset = recipesList.size)

                FilterOptions.HEALTH_SCORE -> state.copy(
                    currentSort = currentSort,
                    recipesList = recipesList.sortedByDescending { it.healthScore },
                    resultsOffset = recipesList.size)

                FilterOptions.NONE -> state.copy(
                    currentSort = currentSort,
                    recipesList = recipesList,
                    resultsOffset = recipesList.size
                )
            }
        }

    }

    data class UpdateListDisplayMode(val listDisplayMode: ListDisplayMode) :
        SearchScreenStateAction() {
        override fun updateState(state: SearchScreenUiState): SearchScreenUiState =
            state.copy(listDisplayMode = listDisplayMode)
    }

    data class UpdateCurrentlySelectedRecipe(val currentlySelectedRecipe: Recipe) :
        SearchScreenStateAction() {
        override fun updateState(state: SearchScreenUiState): SearchScreenUiState =
            state.copy(currentlySelectedRecipe = currentlySelectedRecipe)
    }

}