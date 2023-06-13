package com.example.tastytrails.main.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class RecipeEntity(
    @PrimaryKey
    val id: Long,
    val previouslyViewed: Boolean = false,
    val favorite: Boolean = false,
    val title: String,
    val imageUrl: String? = "",
    val summary: String,
    val healthScore: Int? = null,
    val spoonSourceUrl: String,
    val instructions: List<String>,
    val ingredients: List<String>
)