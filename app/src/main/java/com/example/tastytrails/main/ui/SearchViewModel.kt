package com.example.tastytrails.main.ui

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tastytrails.datastore.PreferencesKeys
import com.example.tastytrails.datastore.ThemeSettings
import com.example.tastytrails.main.domain.Recipe
import com.example.tastytrails.main.domain.RecipeRepository
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

@HiltViewModel()
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
                    _searchScreenUiState.value.searchQuery.trim(),
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
                            SearchScreenStateAction.UpdateRecipesListWithSort(
                                newList,
                                _searchScreenUiState.value.currentSort
                            )
                        )

                        previousSearchResults = _searchScreenUiState.value.recipesList

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
                /*  If the user un-favorites a recipe, we should remove it from the current displayed list
                *  if that list is Favorites(safety check, not really necessary)*/
                if (!favorite && state.value.listDisplayMode == ListDisplayMode.FAVORITES) {
                    updateSearchScreenUiState(
                        SearchScreenStateAction.UpdateRecipesListWithSort
                            (state.value.recipesList.minus(recipe), state.value.currentSort)
                    )
                }
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

    /**
     * Switches the currently displayed list of results: [ListDisplayMode]
     */
    fun executeChangeList(switchTo: ListDisplayMode) {
        // This shouldn't be needed, but it's safer to check anyway.
        if (_searchScreenUiState.value.listDisplayMode == switchTo) return

        viewModelScope.launch(Dispatchers.IO) {
            when (switchTo) {
                ListDisplayMode.CURRENT_SEARCH -> {
                    updateSearchScreenUiState(
                        SearchScreenStateAction.UpdateRecipesListWithSort(
                            previousSearchResults,
                            _searchScreenUiState.value.currentSort
                        )
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
                                SearchScreenStateAction.UpdateRecipesListWithSort(
                                    it,
                                    _searchScreenUiState.value.currentSort
                                )
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
                                SearchScreenStateAction.UpdateRecipesListWithSort(
                                    it,
                                    _searchScreenUiState.value.currentSort
                                )
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
     * Saves to the dataStore the new [ThemeSettings]
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
    fun updateSearchScreenUiState(action: SearchScreenStateAction) {
        _searchScreenUiState.update { latest ->
            action.updateState(latest)
        }
    }
}