package com.example.tastytrails.main.data.remote

data class RecipeResponse(
    val results: List<RecipeDto>?,
    val offset: Long?,
    val number: Long?,
    val totalResults: Long?,
)

data class RecipeDto(
    val cookingMinutes: Long?,
    val healthScore: Int?,
    val extendedIngredients: List<ExtendedIngredient?>?,
    val id: Long?,
    val title: String?,
    val readyInMinutes: Long?,
    val servings: Long?,
    val sourceUrl: String?,
    val image: String?,
    val imageType: String?,
    val summary: String?,
    val analyzedInstructions: List<AnalyzedInstruction?>?,
    val spoonacularSourceUrl: String?,
)

data class ExtendedIngredient(
    val id: Long?,
    val aisle: String?,
    val image: String?,
    val consistency: String?,
    val name: String?,
    val nameClean: String?,
    val original: String?,
    val originalName: String?,
    val amount: Double?,
    val unit: String?,
    val meta: List<String>?,
)

data class AnalyzedInstruction(
    val name: String?,
    val steps: List<Step?>?,
)

data class Step(
    val number: Long?,
    val step: String?,
)