package com.example.tastytrails.main.domain

import com.example.tastytrails.utils.RepoResult

interface RecipeRepository {

    suspend fun searchForRecipes(query: String, searchByName:Boolean): RepoResult<List<Recipe>>

    suspend fun getRecipesByPreviouslyViewed(): List<Recipe>

    suspend fun getRecipesByFavorites(): List<Recipe>

}