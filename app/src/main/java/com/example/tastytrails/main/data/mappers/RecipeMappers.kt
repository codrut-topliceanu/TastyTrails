package com.example.tastytrails.main.data.mappers

import com.example.tastytrails.main.data.local.RecipeEntity
import com.example.tastytrails.main.data.remote.AnalyzedInstruction
import com.example.tastytrails.main.data.remote.ExtendedIngredient
import com.example.tastytrails.main.data.remote.RecipeDto
import com.example.tastytrails.main.domain.Recipe


fun RecipeDto.toRecipeEntity(): RecipeEntity =
    RecipeEntity(
        id = id ?: 0,
        title = title ?: "",
        imageUrl = image,
        summary = summary ?: "",
        healthScore = healthScore,
        spoonSourceUrl = spoonacularSourceUrl ?: sourceUrl ?: "",
        instructions = getStepInstructions(analyzedInstructions) ?: listOf(),
        ingredients = getIngredients(extendedIngredients) ?: listOf()
    )

fun RecipeDto.toRecipe(): Recipe =
    Recipe(
        id = id ?: 0,
        title = title ?: "",
        imageUrl = image,
        summary = summary ?: "",
        healthScore = healthScore,
        spoonSourceUrl = spoonacularSourceUrl ?: sourceUrl ?: "",
        instructions = getStepInstructions(analyzedInstructions) ?: listOf(),
        ingredients = getIngredients(extendedIngredients) ?: listOf()
    )

fun RecipeEntity.toRecipe(): Recipe =
    Recipe(
        id = id,
        title = title,
        previouslyViewed = previouslyViewed,
        favorite = favorite,
        imageUrl = imageUrl,
        summary = summary,
        healthScore = healthScore,
        spoonSourceUrl = spoonSourceUrl,
        instructions = instructions,
        ingredients = ingredients
    )

fun Recipe.toRecipeEntity(): RecipeEntity =
    RecipeEntity(
        id = id,
        title = title,
        previouslyViewed = previouslyViewed,
        favorite = favorite,
        imageUrl = imageUrl,
        summary = summary,
        healthScore = healthScore,
        spoonSourceUrl = spoonSourceUrl,
        instructions = instructions,
        ingredients = ingredients
    )

private fun getStepInstructions(analyzedInstructions: List<AnalyzedInstruction>?): List<String>? =
    analyzedInstructions?.first()?.steps?.map { step ->
        step.step ?: ""
    }

private fun getIngredients(extendedIngredients: List<ExtendedIngredient>?): List<String>? =
    extendedIngredients?.map { extendedIngredient ->
        extendedIngredient.original ?: ""
    }