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
    HEALTH_SCORE
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
 * @param snackBarMessages a list of messages to be displayed to the user, one by one.
 */
data class SearchScreenUiState(
    val inProgress: Boolean = false,
    val searchQuery: String = "",
    val searchByName: Boolean = true,
    val currentSort: FilterOptions = FilterOptions.A_Z,
    val listDisplayMode: ListDisplayMode = ListDisplayMode.CURRENT_SEARCH,
    val recipesList: List<Recipe> = listOf(),
    val currentlySelectedRecipe: Recipe? = null,
    val snackBarMessages: List<SnackBarMessage> = listOf()
)

data class SnackBarMessage(
    val id: Int = Random.nextInt(),
    val message: String
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

    data class UpdateSearchQuery(val searchQuery: String) : SearchScreenStateAction() {
        override fun updateState(state: SearchScreenUiState): SearchScreenUiState =
            state.copy(searchQuery = searchQuery)
    }

    data class UpdateShowInProgress(val showLoading: Boolean) : SearchScreenStateAction() {
        override fun updateState(state: SearchScreenUiState): SearchScreenUiState =
            state.copy(inProgress = showLoading)
    }

    /**
     * Updates the [searchByName] value AND deletes the current searchQuery.
     */
    data class UpdateSearchByName(val searchByName: Boolean) : SearchScreenStateAction() {
        override fun updateState(state: SearchScreenUiState): SearchScreenUiState =
            state.copy(searchByName = searchByName, searchQuery = "")
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
            }
        }
    }

    data class UpdateRecipesListWithSort(
        val recipesList: List<Recipe>,
        val currentSort: FilterOptions
    ) : SearchScreenStateAction() {
        override fun updateState(state: SearchScreenUiState): SearchScreenUiState {
            return when (currentSort) {
                FilterOptions.A_Z -> state.copy(
                    currentSort = currentSort,
                    recipesList = recipesList.sortedBy { it.title })

                FilterOptions.Z_A -> state.copy(
                    currentSort = currentSort,
                    recipesList = recipesList.sortedByDescending { it.title })

                FilterOptions.HEALTH_SCORE -> state.copy(
                    currentSort = currentSort,
                    recipesList = recipesList.sortedByDescending { it.healthScore })
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

    data class AddToSnackBarMessages(val snackBarMessages: List<SnackBarMessage>) :
        SearchScreenStateAction() {
        override fun updateState(state: SearchScreenUiState): SearchScreenUiState {
            val snackBarMessages = state.snackBarMessages.plus(snackBarMessages)
            return state.copy(snackBarMessages = snackBarMessages)
        }
    }

    data class RemoveFromSnackBarMessages(val snackBarMessage: SnackBarMessage) :
        SearchScreenStateAction() {
        override fun updateState(state: SearchScreenUiState): SearchScreenUiState {
            val snackBarMessages = state.snackBarMessages.minus(snackBarMessage)
            return state.copy(snackBarMessages = snackBarMessages)
        }
    }

}