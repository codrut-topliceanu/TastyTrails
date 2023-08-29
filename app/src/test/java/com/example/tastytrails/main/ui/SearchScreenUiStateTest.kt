package com.example.tastytrails.main.ui

import com.example.tastytrails.main.domain.Recipe
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.junit.Before
import org.junit.Test

class SearchScreenUiStateTest {

    private lateinit var fakeSearchScreenUiState: MutableStateFlow<SearchScreenUiState>

    private lateinit var fakeRecipeList: MutableList<Recipe>

    @Before
    fun setup() {
        fakeRecipeList = mutableListOf()
        ('a'..'z').forEachIndexed { index, char ->
            fakeRecipeList.add(
                Recipe(
                    id = index.toLong(),
                    previouslyViewed = false,
                    favorite = false,
                    title = "$char",
                    imageUrl = null,
                    summary = "$char - summary",
                    healthScore = index,
                    spoonSourceUrl = "$char - url",
                    instructions = listOf("boil", "salt", "fry"),
                    ingredients = listOf("onions", "flour", "tomato")
                )
            )
        }
        fakeRecipeList.shuffle()


        fakeSearchScreenUiState = MutableStateFlow(
            SearchScreenUiState(
                inProgress = false,
                searchQuery = "",
                searchByName = true,
                currentSort = FilterOptions.A_Z,
                listDisplayMode = ListDisplayMode.CURRENT_SEARCH,
                recipesList = fakeRecipeList,
                currentlySelectedRecipe = null
            )
        )

    }

    private fun updateSearchScreenUiState(action: SearchScreenStateAction) {
        fakeSearchScreenUiState.update { latest ->
            action.updateState(latest)
        }
    }


    @Test
    fun `WHEN UpdateSearchQuery called THEN update fakeSearchScreenUiState`() {
        updateSearchScreenUiState(
            SearchScreenStateAction.UpdateSearchQuery("testQuery1")
        )
        assertThat(fakeSearchScreenUiState.value.searchQuery == "testQuery1").isTrue()
    }

    @Test
    fun `WHEN UpdateShowInProgress called THEN update fakeSearchScreenUiState`() {
        updateSearchScreenUiState(
            SearchScreenStateAction.UpdateShowInProgress(true)
        )
        assertThat(fakeSearchScreenUiState.value.inProgress).isTrue()

        updateSearchScreenUiState(
            SearchScreenStateAction.UpdateShowInProgress(false)
        )
        assertThat(fakeSearchScreenUiState.value.inProgress).isFalse()
    }

    @Test
    fun `WHEN UpdateSearchByName called THEN update fakeSearchScreenUiState`() {
        updateSearchScreenUiState(
            SearchScreenStateAction.UpdateSearchByName(false)
        )
        assertThat(fakeSearchScreenUiState.value.searchByName).isFalse()

        updateSearchScreenUiState(
            SearchScreenStateAction.UpdateSearchByName(true)
        )
        assertThat(fakeSearchScreenUiState.value.searchByName).isTrue()
    }

    @Test
    fun `WHEN UpdateCurrentSort called THEN update fakeSearchScreenUiState`() {
        assertThat(fakeSearchScreenUiState.value.currentSort == FilterOptions.A_Z).isTrue()

        updateSearchScreenUiState(
            SearchScreenStateAction.UpdateCurrentSort(FilterOptions.Z_A)
        )
        assertThat(fakeSearchScreenUiState.value.currentSort == FilterOptions.Z_A).isTrue()
        assertThat(
            ('a'..'z').reversed().first()
                .toString() == fakeSearchScreenUiState.value.recipesList.first().title
        ).isTrue()
    }

    @Test
    fun `WHEN UpdateRecipesListWithSort called THEN update fakeSearchScreenUiState`() {
        val newFakeList = mutableListOf<Recipe>()
        ('1'..'5').forEachIndexed { index, char ->
            newFakeList.add(
                Recipe(
                    id = index.toLong(),
                    previouslyViewed = false,
                    favorite = false,
                    title = "$char",
                    imageUrl = null,
                    summary = "$char - summary",
                    healthScore = index,
                    spoonSourceUrl = "$char - url",
                    instructions = listOf("boil", "salt", "fry"),
                    ingredients = listOf("onions", "flour", "tomato")
                )
            )
        }

        updateSearchScreenUiState(
            SearchScreenStateAction.UpdateRecipesListWithSort(
                newFakeList, FilterOptions.A_Z
            )
        )
        assertThat(fakeSearchScreenUiState.value.currentSort == FilterOptions.A_Z).isTrue()
        assertThat(newFakeList == fakeSearchScreenUiState.value.recipesList).isTrue()

        updateSearchScreenUiState(
            SearchScreenStateAction.UpdateRecipesListWithSort(
                newFakeList, FilterOptions.HEALTH_SCORE
            )
        )

        assertThat(fakeSearchScreenUiState.value.currentSort == FilterOptions.HEALTH_SCORE).isTrue()
        assertThat(newFakeList.reversed() == fakeSearchScreenUiState.value.recipesList).isTrue()
    }

    @Test
    fun `WHEN UpdateListDisplayMode called THEN update fakeSearchScreenUiState`() {
        updateSearchScreenUiState(
            SearchScreenStateAction.UpdateListDisplayMode(ListDisplayMode.FAVORITES)
        )
        assertThat(fakeSearchScreenUiState.value.listDisplayMode == ListDisplayMode.FAVORITES).isTrue()
    }

    @Test
    fun `WHEN UpdateCurrentlySelectedRecipe called THEN update fakeSearchScreenUiState`() {
        val fakeRecipe = Recipe(
            id = 4864,
            previouslyViewed = false,
            favorite = false,
            title = "bibendum",
            imageUrl = null,
            summary = "civibus",
            healthScore = null,
            spoonSourceUrl = "http://www.bing.com/search?q=volutpat",
            instructions = listOf(),
            ingredients = listOf()
        )
        updateSearchScreenUiState(
            SearchScreenStateAction.UpdateCurrentlySelectedRecipe(
                fakeRecipe
            )
        )
        assertThat(fakeSearchScreenUiState.value.currentlySelectedRecipe == fakeRecipe).isTrue()
    }

}