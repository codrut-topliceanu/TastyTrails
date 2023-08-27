package com.example.tastytrails.main.ui

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tastytrails.R
import com.example.tastytrails.datastore.PreferencesKeys
import com.example.tastytrails.datastore.ThemeSettings
import com.example.tastytrails.main.domain.Recipe
import com.example.tastytrails.main.domain.RecipeRepository
import com.example.tastytrails.main.ui.SearchScreenStateAction.AddToSnackBarMessages
import com.example.tastytrails.main.ui.SearchScreenStateAction.UpdateListDisplayMode
import com.example.tastytrails.main.ui.SearchScreenStateAction.UpdateRecipesListWithSort
import com.example.tastytrails.main.ui.SearchScreenStateAction.UpdateShowInProgress
import com.example.tastytrails.utils.RepoResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: RecipeRepository,
    private val dataStore: DataStore<Preferences>
) : ViewModel() {

    lateinit var state: StateFlow<SearchScreenUiState>
    private val _searchScreenUiState = MutableStateFlow(SearchScreenUiState())

    val themeSettingsFlow: Flow<ThemeSettings> = dataStore.data
        .catch {
            Log.e("SearchViewModel", "userPreferencesFlow dataStore.data Exception : ${it.message}")
            emit(emptyPreferences())
        }.map { preferences ->
            ThemeSettings(
                darkMode = preferences[PreferencesKeys.DARK_MODE_SETTING] ?: 0,
                dynamicThemeEnabled = preferences[PreferencesKeys.DYNAMIC_THEME_SETTING] ?: true
            )
        }

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
     * @param loadMore if the call should use [SearchScreenUiState.resultsOffset] to load more results
     */
    fun executeOnlineSearch(loadMore: Boolean = false) {
        // No need to make a call if we don't have any search queries
        if (state.value.searchQuery.isBlank()) {
            updateUiState(
                AddToSnackBarMessages(
                    listOf(SnackBarMessage(messageStringId = R.string.empty_query_msg))
                )
            )
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            // Clear the previous search results, just in case
            previousSearchResults = listOf()

            // Show progress indicator
            updateUiState(UpdateShowInProgress(true))

            // Call server
            when (val repoResult =
                repository.searchForRecipes(
                    query = _searchScreenUiState.value.searchQuery.trim(),
                    searchByName = _searchScreenUiState.value.searchByName,
                    resultsOffset = if (loadMore
                        && _searchScreenUiState.value.listDisplayMode == ListDisplayMode.CURRENT_SEARCH
                    ) {
                        _searchScreenUiState.value.resultsOffset
                    } else 0
                )) {
                is RepoResult.Error -> {
                    repoResult.message?.let {
                        // Print msg to user with error
                        updateUiState(
                            AddToSnackBarMessages(
                                listOf(SnackBarMessage(message = repoResult.message))
                            )
                        )
                    }
                }

                is RepoResult.Success -> {
                    repoResult.data?.let { newList ->
                        // Update list with results
                        if (loadMore && _searchScreenUiState.value.listDisplayMode == ListDisplayMode.CURRENT_SEARCH) {
                            updateUiState(
                                UpdateRecipesListWithSort(
                                    recipesList = _searchScreenUiState.value.recipesList + newList,
                                    currentSort = _searchScreenUiState.value.currentSort
                                )
                            )
                        } else {
                            updateUiState(
                                UpdateRecipesListWithSort(
                                    recipesList = newList,
                                    currentSort = _searchScreenUiState.value.currentSort
                                )
                            )
                        }
                        previousSearchResults = _searchScreenUiState.value.recipesList

                        // Switch list to results search
                        updateUiState(
                            UpdateListDisplayMode(ListDisplayMode.CURRENT_SEARCH)
                        )
                    }
                }
            }

            // Hide progress indicator
            updateUiState(UpdateShowInProgress(false))
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
                updateUiState(
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
                updateUiState(
                    SearchScreenStateAction.UpdateCurrentlySelectedRecipe
                        (recipeToSave)
                )
                /* If the user un-favorites a recipe, we should remove it from the current displayed list
                *   if that list is Favorites */
                if (!favorite && _searchScreenUiState.value.listDisplayMode == ListDisplayMode.FAVORITES) {
                    updateUiState(
                        SearchScreenStateAction.UpdateRecipesListWithSort
                            (
                            _searchScreenUiState.value.recipesList.minus(recipe),
                            state.value.currentSort
                        )
                    )
                } else {
                    // Update list to reflect the recipe's favorite state
                    updateUiState(
                        UpdateRecipesListWithSort(
                            recipesList = _searchScreenUiState.value.recipesList.map { oldRecipe ->
                                if (oldRecipe.id == recipeToSave.id)
                                    recipeToSave
                                else oldRecipe
                            },
                            currentSort = _searchScreenUiState.value.currentSort
                        )
                    )
                }

                // Update previous search result as well (in case the user was in a different display mode)
                previousSearchResults = previousSearchResults.map { oldRecipe ->
                    if (oldRecipe.id == recipeToSave.id)
                        recipeToSave
                    else oldRecipe
                }
            } else {
                repoResult.message?.let { message ->
                    updateUiState(
                        AddToSnackBarMessages(
                            listOf(SnackBarMessage(message = message))
                        )
                    )
                }
            }
        }
    }

    /**
     * Switches the currently displayed list of results: [ListDisplayMode].
     */
    fun executeChangeList(switchTo: ListDisplayMode) {
        // This shouldn't be needed, but it's safer to check anyway.
        if (_searchScreenUiState.value.listDisplayMode == switchTo) return

        viewModelScope.launch(Dispatchers.IO) {
            when (switchTo) {
                ListDisplayMode.CURRENT_SEARCH -> {
                    updateUiState(
                        UpdateRecipesListWithSort(
                            previousSearchResults,
                            _searchScreenUiState.value.currentSort
                        )
                    )
                    updateUiState(
                        UpdateListDisplayMode(switchTo)
                    )
                }

                ListDisplayMode.PREVIOUSLY_VIEWED -> {
                    val repoResult = repository.getRecipesByPreviouslyViewed(true)
                    if (repoResult is RepoResult.Success) {
                        repoResult.data?.let {
                            updateUiState(
                                UpdateRecipesListWithSort(
                                    it,
                                    _searchScreenUiState.value.currentSort
                                )
                            )
                        }
                        updateUiState(
                            UpdateListDisplayMode(switchTo)
                        )
                    } else {
                        repoResult.message?.let { message ->
                            updateUiState(
                                AddToSnackBarMessages(
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
                            updateUiState(
                                UpdateRecipesListWithSort(
                                    recipesList = it,
                                    currentSort = _searchScreenUiState.value.currentSort
                                )
                            )
                        }
                        updateUiState(UpdateListDisplayMode(switchTo))
                    } else {
                        repoResult.message?.let { message ->
                            updateUiState(
                                AddToSnackBarMessages(listOf(SnackBarMessage(message = message)))
                            )
                        }
                    }
                }
            }
        }
    }

    /**
     * Saves to the dataStore the new [ThemeSettings].
     */
    fun executeUpdateThemeSettings(themeSettings: ThemeSettings) {
        viewModelScope.launch(Dispatchers.IO) {
            dataStore.edit { preferences ->
                preferences[PreferencesKeys.DARK_MODE_SETTING] = themeSettings.darkMode
                preferences[PreferencesKeys.DYNAMIC_THEME_SETTING] =
                    themeSettings.dynamicThemeEnabled
            }
        }
    }

    /**
     * Updates the ui state of [SearchScreen].
     */
    fun updateUiState(action: SearchScreenStateAction) {
        _searchScreenUiState.update { latest ->
            action.updateState(latest)
        }
    }

    /**
     * Returns the string of the [snackBarMessage]'s message or messageStringId, or null if none found.
     */
    fun getSnackBarMsg(localContext: Context, snackBarMessage: SnackBarMessage): String? {
        return when {
            snackBarMessage.message.isNotBlank() -> snackBarMessage.message
            snackBarMessage.messageStringId != null -> localContext.getString(snackBarMessage.messageStringId)
            else -> null
        }
    }
}

/**
 * Filters a string to consist of a list of alphanumeric items separated by commas and optional spaces.
 */
fun filterSearchInput(newValue: String) = newValue.matches(Regex("^([a-zA-Z0-9-]+,?+\\s?)+\$"))