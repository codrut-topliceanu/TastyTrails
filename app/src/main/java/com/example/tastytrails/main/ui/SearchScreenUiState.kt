package com.example.tastytrails.main.ui

import androidx.annotation.StringRes
import com.example.tastytrails.main.domain.Recipe
import kotlin.random.Random

enum class ListDisplayMode(@StringRes val stringResourceId: Int = 0) {
    CURRENT_SEARCH,
    PREVIOUSLY_VIEWED,
    FAVORITES
}

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
    // TODO delete un-needed actions

    data class UpdateSearchQuery(val searchQuery: String) : SearchScreenStateAction() {
        override fun updateState(state: SearchScreenUiState): SearchScreenUiState =
            state.copy(searchQuery = searchQuery)
    }

    data class UpdateShowInProgress(val showLoading: Boolean) : SearchScreenStateAction() {
        override fun updateState(state: SearchScreenUiState): SearchScreenUiState =
            state.copy(inProgress = showLoading)
    }

    data class UpdateSearchByName(val searchByName: Boolean) : SearchScreenStateAction() {
        override fun updateState(state: SearchScreenUiState): SearchScreenUiState =
            state.copy(searchByName = searchByName, searchQuery = "")
    }

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
                    recipesList = state.recipesList.sortedBy { it.healthScore })
            }
        }
    }

    data class UpdateRecipesList(val recipesList: List<Recipe>) : SearchScreenStateAction() {
        override fun updateState(state: SearchScreenUiState): SearchScreenUiState =
            state.copy(recipesList = recipesList)
    }

    data class UpdateListDisplayMode(val listDisplayMode: ListDisplayMode) :
        SearchScreenStateAction() {
        override fun updateState(state: SearchScreenUiState): SearchScreenUiState =
            state.copy(listDisplayMode = listDisplayMode)
    }

    data class UpdateCurrentlySelectedRecipe(val currentlySelectedRecipe: Recipe?) :
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

    data class RemoveFromSnackBarMessages(val snackBarMessages: List<SnackBarMessage>) :
        SearchScreenStateAction() {
        override fun updateState(state: SearchScreenUiState): SearchScreenUiState {
            val snackBarMessages = state.snackBarMessages.minus(snackBarMessages.toSet())
            return state.copy(snackBarMessages = snackBarMessages)
        }
    }

}