package com.example.tastytrails.main.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tastytrails.main.domain.Recipe
import com.example.tastytrails.main.domain.RecipeRepository
import com.example.tastytrails.utils.RepoResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel()
class SearchViewModel @Inject constructor(
    private val repository: RecipeRepository
) : ViewModel() {

    lateinit var state: StateFlow<SearchScreenUiState>
    private val _searchScreenUiState = MutableStateFlow(SearchScreenUiState())

    /**
     * Previous search results, used when switching between [ListDisplayMode]s
     */
    private var previousSearchResults: List<Recipe> = listOf()

    init {
        viewModelScope.launch {
            state = _searchScreenUiState.stateIn(viewModelScope)
        }
    }

    /**
     * Executes a web call to search for the searchQuery, shows user a message if an error happened.
     * Then updates ui state with the results returned from the API call.
     */
    fun executeOnlineSearch() {
        // No need to make a call if we don't have any search queries
        if (_searchScreenUiState.value.searchQuery.isBlank()) {
            SearchScreenStateAction.AddToSnackBarMessages(
                listOf(SnackBarMessage(message = "Try typing in something delicious."))
            )
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            // Clear the previous search results, just in case
            previousSearchResults = listOf()

            // Show progress indicator
            updateSearchScreenUiState(SearchScreenStateAction.UpdateShowInProgress(true))

            // Call server
            when (val repoResult =
                repository.searchForRecipes(
                    _searchScreenUiState.value.searchQuery,
                    _searchScreenUiState.value.searchByName
                )) {
                is RepoResult.Error -> {
                    repoResult.message?.let {
                        // Print msg to user with error
                        updateSearchScreenUiState(
                            SearchScreenStateAction.AddToSnackBarMessages(
                                listOf(SnackBarMessage(message = repoResult.message))
                            )
                        )
                    }
                }

                is RepoResult.Success -> {
                    repoResult.data?.let { newList ->
                        // Update list with results
                        updateSearchScreenUiState(
                            SearchScreenStateAction.UpdateRecipesList(newList)
                        )

                        previousSearchResults = newList

                        // Switch list to results search
                        updateSearchScreenUiState(
                            SearchScreenStateAction
                                .UpdateListDisplayMode(ListDisplayMode.CURRENT_SEARCH)
                        )
                    }
                }
            }

            // Hide progress indicator
            updateSearchScreenUiState(SearchScreenStateAction.UpdateShowInProgress(false))
        }
    }

    /**
     * Marks a recipe as having been viewed previously and saves it in the local DB.
     */
    fun executeSaveViewedRecipe(recipe: Recipe) {
        viewModelScope.launch(Dispatchers.IO) {
            val recipeToSave = recipe.copy(previouslyViewed = true)
            val repoResult = repository.upsertRecipe(recipeToSave)
            if (repoResult is RepoResult.Success) {
                updateSearchScreenUiState(
                    SearchScreenStateAction.UpdateCurrentlySelectedRecipe
                        (recipeToSave)
                )
            }
        }
    }

    /**
     * Marks a recipe as having been set to favorite/un-favorite and saves it in the local DB.
     */
    fun executeSaveFavoriteRecipe(recipe: Recipe, favorite: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val recipeToSave = recipe.copy(favorite = favorite)
            val repoResult = repository.upsertRecipe(recipeToSave)
            if (repoResult is RepoResult.Success) {
                updateSearchScreenUiState(
                    SearchScreenStateAction.UpdateCurrentlySelectedRecipe
                        (recipeToSave)
                )
            } else {
                repoResult.message?.let { message ->
                    updateSearchScreenUiState(
                        SearchScreenStateAction.AddToSnackBarMessages(
                            listOf(SnackBarMessage(message = message))
                        )
                    )
                }
            }
        }
    }

    fun executeChangeList(switchTo: ListDisplayMode) {
        // This shouldn't be needed, but it's safer to check anyway.
        if (_searchScreenUiState.value.listDisplayMode == switchTo) return

        viewModelScope.launch(Dispatchers.IO) {
            when (switchTo) {
                ListDisplayMode.CURRENT_SEARCH -> {
                    updateSearchScreenUiState(
                        SearchScreenStateAction.UpdateRecipesList(previousSearchResults)
                    )
                    updateSearchScreenUiState(
                        SearchScreenStateAction.UpdateListDisplayMode(switchTo)
                    )
                }

                ListDisplayMode.PREVIOUSLY_VIEWED -> {
                    val repoResult = repository.getRecipesByPreviouslyViewed(true)
                    if (repoResult is RepoResult.Success) {
                        repoResult.data?.let {
                            updateSearchScreenUiState(
                                SearchScreenStateAction.UpdateRecipesList(it)
                            )
                        }
                        updateSearchScreenUiState(
                            SearchScreenStateAction.UpdateListDisplayMode(switchTo)
                        )
                    } else {
                        repoResult.message?.let { message ->
                            updateSearchScreenUiState(
                                SearchScreenStateAction.AddToSnackBarMessages(
                                    listOf(SnackBarMessage(message = message))
                                )
                            )
                        }
                    }
                }

                ListDisplayMode.FAVORITES -> {
                    val repoResult = repository.getRecipesByFavorites(true)
                    if (repoResult is RepoResult.Success) {
                        repoResult.data?.let {
                            updateSearchScreenUiState(
                                SearchScreenStateAction.UpdateRecipesList(it)
                            )
                        }
                        updateSearchScreenUiState(
                            SearchScreenStateAction.UpdateListDisplayMode(switchTo)
                        )
                    } else {
                        repoResult.message?.let { message ->
                            updateSearchScreenUiState(
                                SearchScreenStateAction.AddToSnackBarMessages(
                                    listOf(SnackBarMessage(message = message))
                                )
                            )
                        }
                    }

                }
            }
        }
    }


    /**
     * Updates the ui state of [SearchScreen].
     */
    fun updateSearchScreenUiState(action: SearchScreenStateAction) {
        _searchScreenUiState.update { latest ->
            action.updateState(latest)
        }
    }
}