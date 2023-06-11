package com.example.tastytrails.main.data.repository

import android.app.Application
import android.util.Log
import com.example.tastytrails.main.data.local.RecipeDao
import com.example.tastytrails.main.data.mappers.toRecipe
import com.example.tastytrails.main.data.mappers.toRecipeEntity
import com.example.tastytrails.main.data.remote.RecipesApi
import com.example.tastytrails.main.domain.Recipe
import com.example.tastytrails.main.domain.RecipeRepository
import com.example.tastytrails.utils.RepoResult
import javax.inject.Inject

// TODO : inject
class RecipeRepositoryImpl @Inject constructor(
    private val recipesApi: RecipesApi,
    private val recipeDao: RecipeDao
) : RecipeRepository {
    // TODO build a fake repo for testing OR build a fake API

    override suspend fun searchForRecipes(
        query: String,
        searchByName: Boolean
    ): RepoResult<List<Recipe>> {
        // TODO: final step - prepare this for showtime ( results number = 10
        try {
            val response = recipesApi.getRecipesByName(
                recipeNames = if (searchByName) query else null,
                includeIngredients = if (searchByName) null else query,
                resultsNumber = 1,
                fillIngredients = true,
                addRecipeInformation = true,
            )

            Log.e("getRecipesByName", "getRecipesByName: $response")
            if (!response.isSuccessful) {
                Log.e("RecipeRepositoryImpl", "searchForRecipes error : ${response.errorBody()}")
                return RepoResult.Error("Server isn't done cooking, try again later")
            }

            val result = response.body()?.results?.map { it.toRecipe() }

            return if (result.isNullOrEmpty()) {
                RepoResult.Error("Couldn't find anything tasty down that trail. :(")
            } else {
                RepoResult.Success(result)
            }

        } catch (e: Exception) {
            Log.e("RecipeRepositoryImpl", "searchForRecipes exception : ${e.message}")
            return RepoResult.Error("Something didn't go right, try again later please.")

        }
    }

    override suspend fun upsertRecipe(recipe: Recipe): RepoResult<Unit> {
        return try {
            val recipeEntity = recipe.toRecipeEntity()
            recipeDao.upsert(recipeEntity)
            RepoResult.Success(Unit)
        } catch (e: Exception) {
            Log.e("RecipeRepositoryImpl", "upsertRecipe exception : ${e.message}")
            RepoResult.Error("Stubborn snack! There was an error saving the item, apologies.")
        }
    }

    override suspend fun getRecipesByPreviouslyViewed(previouslyViewed: Boolean): RepoResult<List<Recipe>> {
        return try {
            val recipeList = recipeDao.getAllViewed(previouslyViewed)
            RepoResult.Success(recipeList.map { it.toRecipe() })
        } catch (e: Exception) {
            Log.e("RecipeRepositoryImpl", "getRecipesByPreviouslyViewed exception : ${e.message}")
            RepoResult.Error(
                "Apologies, couldn't get the requested snack list. Try a sandwich instead?",
                null
            )
        }
    }

    override suspend fun getRecipesByFavorites(favorite: Boolean): RepoResult<List<Recipe>> {
        return try {
            val recipeList = recipeDao.getAllFavorite(favorite)
            RepoResult.Success(recipeList.map { it.toRecipe() })
        } catch (e: Exception) {
            Log.e("RecipeRepositoryImpl", "getRecipesByFavorites exception : ${e.message}")
            RepoResult.Error(
                "Apologies, couldn't get the requested snack list. Try a sandwich instead?",
                null
            )
        }
    }


}