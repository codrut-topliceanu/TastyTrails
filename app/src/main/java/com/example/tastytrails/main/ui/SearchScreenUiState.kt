package com.example.tastytrails.main.ui

import com.example.tastytrails.main.domain.Recipe

data class SearchScreenUiState(
    val showLoading: Boolean = false,
    val searchQuery: String = "",
    val searchByName: Boolean = true,
    val currentSort: FilterOptions = FilterOptions.A_Z,
    val recipesList: List<Recipe> = listOf(),
    val cachedRecipesList: List<Recipe> = listOf(),
    val currentlySelectedRecipe: Recipe? = null
)

interface StateAction<T : Any> {
    fun updateState(state: T): T
}

/**
 * List of actions available to the user in SearchScreen
 */
sealed class SearchScreenStateAction : StateAction<SearchScreenUiState> {

    data class UpdateSearchQuery(val searchQuery: String) : SearchScreenStateAction() {
        override fun updateState(state: SearchScreenUiState): SearchScreenUiState =
            state.copy(searchQuery = searchQuery)
    }

    data class UpdateShowLoading(val showLoading: Boolean) : SearchScreenStateAction() {
        override fun updateState(state: SearchScreenUiState): SearchScreenUiState =
            state.copy(showLoading = showLoading)
    }

    data class UpdateSearchByName(val searchByName: Boolean) : SearchScreenStateAction() {
        override fun updateState(state: SearchScreenUiState): SearchScreenUiState =
            state.copy(searchByName = searchByName)
    }

    data class UpdateCurrentSort(val currentSort: FilterOptions) : SearchScreenStateAction() {
        override fun updateState(state: SearchScreenUiState): SearchScreenUiState =
            state.copy(currentSort = currentSort)
    }

    data class UpdateRecipesList(val recipesList: List<Recipe>) : SearchScreenStateAction() {
        override fun updateState(state: SearchScreenUiState): SearchScreenUiState =
            state.copy(recipesList = recipesList)
    }

    data class UpdateCachedRecipesList(val cachedRecipesList: List<Recipe>) :
        SearchScreenStateAction() {
        override fun updateState(state: SearchScreenUiState): SearchScreenUiState =
            state.copy(cachedRecipesList = cachedRecipesList)
    }

    data class UpdateCurrentlySelectedRecipe(val currentlySelectedRecipe: Recipe?) :
        SearchScreenStateAction() {
        override fun updateState(state: SearchScreenUiState): SearchScreenUiState =
            state.copy(currentlySelectedRecipe = currentlySelectedRecipe)
    }

}