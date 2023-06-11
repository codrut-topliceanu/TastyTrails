package com.example.tastytrails.main.domain

interface RecipeRepository {

    suspend fun getRecipesByName(name: String): List<Recipe>

    suspend fun getRecipesByIngredient(ingredient: String): List<Recipe>

    suspend fun getRecipesByFavorites(): List<Recipe>

//    suspend fun getRecipeDetails(recipeId: Int): Recipe
}