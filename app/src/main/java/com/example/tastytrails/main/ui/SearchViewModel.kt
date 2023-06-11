package com.example.tastytrails.main.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import javax.inject.Singleton

@HiltViewModel()
class SearchViewModel @Inject constructor(
    private val repository: RecipeRepository
) : ViewModel() {

    lateinit var state: StateFlow<SearchScreenUiState>
    private val _searchScreenUiState = MutableStateFlow(SearchScreenUiState())

    init {
        viewModelScope.launch {
            state = _searchScreenUiState.stateIn(viewModelScope)
        }
    }

    fun executeOnlineSearch() {
        if (_searchScreenUiState.value.searchQuery.isBlank()) {
            SearchScreenStateAction.AddToSnackBarMessages(
                listOf(SnackBarMessage(message = "Try typing in something delicious."))
            )
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            updateSearchScreenUiState(SearchScreenStateAction.UpdateShowInProgress(true))
            when (val repoResult =
                repository.searchForRecipes(
                    _searchScreenUiState.value.searchQuery,
                    _searchScreenUiState.value.searchByName
                )) {
                is RepoResult.Error -> {
                    repoResult.message?.let {
                        updateSearchScreenUiState(
                            SearchScreenStateAction.AddToSnackBarMessages(
                                listOf(SnackBarMessage(message = repoResult.message))
                            )
                        )
                    }
                }

                is RepoResult.Success -> {
                    repoResult.data?.let { newList ->
                        updateSearchScreenUiState(
                            SearchScreenStateAction.UpdateRecipesList(newList)
                        )
                        updateSearchScreenUiState(
                            SearchScreenStateAction
                                .UpdateListDisplayMode(ListDisplayMode.CURRENT_SEARCH)
                        )
                    }
                }
            }
            updateSearchScreenUiState(SearchScreenStateAction.UpdateShowInProgress(false))
        }
    }

    /**
     * Updates the ui state of [SearchScreen]
     */
    fun updateSearchScreenUiState(action: SearchScreenStateAction) {
        _searchScreenUiState.update { latest ->
            action.updateState(latest)
        }
    }
}