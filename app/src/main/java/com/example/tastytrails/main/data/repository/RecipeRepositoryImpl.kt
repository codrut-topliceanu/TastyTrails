package com.example.tastytrails.main.data.repository

import android.app.Application
import android.util.Log
import com.example.tastytrails.R
import com.example.tastytrails.main.data.local.RecipeDao
import com.example.tastytrails.main.data.mappers.toRecipe
import com.example.tastytrails.main.data.mappers.toRecipeEntity
import com.example.tastytrails.main.data.remote.RecipesApi
import com.example.tastytrails.main.domain.Recipe
import com.example.tastytrails.main.domain.RecipeRepository
import com.example.tastytrails.utils.RepoResult
import javax.inject.Inject

const val MAX_RESULTS_PER_SEARCH = 10

class RecipeRepositoryImpl @Inject constructor(
    private val recipesApi: RecipesApi,
    private val recipeDao: RecipeDao,
    private val context: Application
) : RecipeRepository {

    override suspend fun searchForRecipes(
        query: String,
        searchByName: Boolean,
        resultsOffset: Int
    ): RepoResult<List<Recipe>> {
        try {
            val response = recipesApi.getRecipesByName(
                recipeNames = if (searchByName) query else null,
                includeIngredients = if (searchByName) null else query,
                resultsNumber = MAX_RESULTS_PER_SEARCH,
                offset = resultsOffset,
                fillIngredients = true,
                addRecipeInformation = true,
            )

            Log.i("getRecipesByName", "getRecipesByName: $response")

            if (!response.isSuccessful) {
                Log.e("RecipeRepositoryImpl", "searchForRecipes error : ${response.errorBody()}")
                return RepoResult.Error(context.getString(R.string.error_search_recipe))
            }

            val result = response.body()?.results?.map { it.toRecipe() }

            return if (result.isNullOrEmpty()) {
                RepoResult.Error(
                    if (resultsOffset == 0) context.getString(R.string.error_search_recipe_empty_search)
                    else context.getString(R.string.error_search_recipe_empty_search_with_offset)

                )
            } else {
                RepoResult.Success(result)
            }

        } catch (e: Exception) {
            Log.e("RecipeRepositoryImpl", "searchForRecipes exception : ", e)
            return RepoResult.Error(context.getString(R.string.error_search_recipe_exception))

        }
    }

    override suspend fun upsertRecipe(recipe: Recipe): RepoResult<Unit> {
        return try {
            val recipeEntity = recipe.toRecipeEntity()
            recipeDao.upsert(recipeEntity)
            RepoResult.Success(Unit)
        } catch (e: Exception) {
            Log.e("RecipeRepositoryImpl", "upsertRecipe exception : ", e)
            RepoResult.Error(context.getString(R.string.error_upser_exception))
        }
    }

    override suspend fun getRecipesByPreviouslyViewed(previouslyViewed: Boolean): RepoResult<List<Recipe>> {
        return try {
            val recipeList = recipeDao.getAllViewed(previouslyViewed)
            RepoResult.Success(recipeList.map { it.toRecipe() })
        } catch (e: Exception) {
            Log.e(
                "RecipeRepositoryImpl",
                "getRecipesByPreviouslyViewed exception : ", e
            )
            RepoResult.Error(
                context.getString(R.string.error_get_recipes_exception),
                null
            )
        }
    }

    override suspend fun getRecipesByFavorites(favorite: Boolean): RepoResult<List<Recipe>> {
        return try {
            val recipeList = recipeDao.getAllFavorite(favorite)
            RepoResult.Success(recipeList.map { it.toRecipe() })
        } catch (e: Exception) {
            Log.e("RecipeRepositoryImpl", "getRecipesByFavorites exception :  ", e)
            RepoResult.Error(
                context.getString(R.string.error_get_recipes_exception),
                null
            )
        }
    }


}