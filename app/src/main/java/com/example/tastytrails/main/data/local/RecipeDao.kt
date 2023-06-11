package com.example.tastytrails.main.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface RecipeDao {

    @Upsert
    suspend fun upsertAll(beers: List<RecipeEntity>)

    @Query("SELECT * FROM recipeentity")
    fun getAll(): List<RecipeEntity>

    @Query("SELECT * FROM recipeentity WHERE id = :recipeId")
    suspend fun getRecipeById(recipeId: String): RecipeEntity?

    @Query("DELETE FROM recipeentity")
    suspend fun clearAll()
}