package com.example.tastytrails.main.data.repository

import android.app.Application
import com.example.tastytrails.main.data.remote.RecipesApi
import com.example.tastytrails.main.domain.Recipe
import com.example.tastytrails.main.domain.RecipeRepository
import javax.inject.Inject

// TODO : inject
class RecipeRepositoryImpl @Inject constructor(
    private val recipesApi: RecipesApi,
    private val appContext: Application
) : RecipeRepository {
    // TODO build a fake repo for testing
    override suspend fun getRecipesByName(name: String): List<Recipe> {
        TODO("Not yet implemented")
    }

    override suspend fun getRecipesByIngredient(ingredients: String): List<Recipe> {
        TODO("Not yet implemented")
    }

    override suspend fun getRecipesByFavorites(): List<Recipe> {
        TODO("Not yet implemented")
    }

}