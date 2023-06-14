package com.example.tastytrails.main.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface RecipeDao {

    @Upsert
    suspend fun upsertAll(recipes: List<RecipeEntity>)

    @Upsert
    suspend fun upsert(recipe: RecipeEntity)

    @Query("SELECT * FROM recipeentity")
    fun getAll(): List<RecipeEntity>

    @Query("SELECT * FROM recipeentity WHERE previouslyViewed = :viewed")
    fun getAllViewed(viewed: Boolean): List<RecipeEntity>

    @Query("SELECT * FROM recipeentity WHERE favorite = :favorite")
    fun getAllFavorite(favorite: Boolean): List<RecipeEntity>

    @Query("SELECT * FROM recipeentity WHERE id = :recipeId")
    suspend fun getRecipeById(recipeId: String): RecipeEntity?

    @Query("DELETE FROM recipeentity")
    suspend fun clearAll()
}