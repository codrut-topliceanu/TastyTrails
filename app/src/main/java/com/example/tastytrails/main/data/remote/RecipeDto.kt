package com.example.tastytrails.main.data.remote

data class RecipeResponse(
    val results: List<RecipeDto>?,
    val offset: Long?,
    val number: Long?,
    val totalResults: Long?,
)

data class RecipeDto(
    val vegetarian: Boolean?,
    val vegan: Boolean?,
    val glutenFree: Boolean?,
    val dairyFree: Boolean?,
    val veryHealthy: Boolean?,
    val cheap: Boolean?,
    val veryPopular: Boolean?,
    val sustainable: Boolean?,
    val lowFodmap: Boolean?,
    val weightWatcherSmartPoints: Long?,
    val gaps: String?,
    val preparationMinutes: Long?,
    val cookingMinutes: Long?,
    val aggregateLikes: Long?,
    val healthScore: Int?,
    val creditsText: String?,
    val sourceName: String?,
    val pricePerServing: Double?,
    val extendedIngredients: List<ExtendedIngredient>?,
    val id: Long?,
    val title: String?,
    val readyInMinutes: Long?,
    val servings: Long?,
    val sourceUrl: String?,
    val image: String?,
    val imageType: String?,
    val summary: String?,
    val cuisines: List<Any?>?,
    val dishTypes: List<String>?,
    val diets: List<String>?,
    val occasions: List<Any?>?,
    val analyzedInstructions: List<AnalyzedInstruction>?,
    val spoonacularSourceUrl: String?,
    val usedIngredientCount: Long?,
    val missedIngredientCount: Long?,
    val likes: Long?,
    val usedIngredients: List<Any?>?,
    val unusedIngredients: List<Any?>?,
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
    val steps: List<Step>?,
)

data class Step(
    val number: Long?,
    val step: String?,
)