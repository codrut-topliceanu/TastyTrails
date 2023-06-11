package com.example.tastytrails.main.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tastytrails.main.domain.RecipeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
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

    /**
     * Updates the ui state of [SearchScreen]
     */
    fun updateSearchScreenUiState(action: SearchScreenStateAction) {
        _searchScreenUiState.update { latest ->
            action.updateState(latest)
        }
    }
}