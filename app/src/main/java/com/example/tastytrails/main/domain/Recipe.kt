package com.example.tastytrails.main.domain


data class Recipe(
    val id: Long,
    val favorite: Boolean = false,
    val title: String,
    val imageUrl: String? = null,
    val summary: String,
    val healthScore: Int? = null,
    val spoonSourceUrl: String = "",
    val instructions: List<String> = arrayListOf(),
    val ingredients: List<String> = arrayListOf()
)
