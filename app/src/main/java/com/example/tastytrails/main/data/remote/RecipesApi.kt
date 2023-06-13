package com.example.tastytrails.main.data.remote

import com.example.tastytrails.BuildConfig
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface RecipesApi {

    /*
    Terms of Service:
    You may cache user-requested data to improve performance (for a maximum of 1 hour).
    After 1 hour, you must delete your cache and refresh the data via the spoonacular API.
    If you stop using the spoonacular API or if your access to the API is suspended for any reason,
    then you must delete all data you ever obtained from the spoonacular API.
    */

    @Headers("Content-Type: application/json")
    @GET("complexSearch")
    suspend fun getRecipesByName(
        @Query("query") recipeNames: String?,
        @Query("number") resultsNumber: Int = 5,
        @Query("includeIngredients") includeIngredients: String? = null,
        @Query("fillIngredients") fillIngredients: Boolean = false,
        @Query("addRecipeInformation") addRecipeInformation: Boolean = false,
        @Query("ignorePantry") ignorePantry: Boolean = true,
        @Query("apiKey") apiKey: String = BuildConfig.SPOON_API_KEY
    ): Response<RecipeResponse>

    companion object {
        const val BASE_URL = "https://api.spoonacular.com/recipes/"
    }
}