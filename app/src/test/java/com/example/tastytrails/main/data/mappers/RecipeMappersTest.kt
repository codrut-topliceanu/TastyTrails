package com.example.tastytrails.main.data.mappers

import com.example.tastytrails.main.data.local.RecipeEntity
import com.example.tastytrails.main.data.remote.RecipeDto
import com.example.tastytrails.main.domain.Recipe
import com.google.common.truth.Truth
import org.junit.Test

class RecipeMappersTest {

    @Test
    fun `GIVEN a null filled RecipeDto WHEN toRecipeEntity() THEN return a valid RecipeEntity`() {
        val recipeEntity = RecipeDto(
            cookingMinutes = null,
            healthScore = null,
            extendedIngredients = null,
            id = null,
            title = null,
            readyInMinutes = null,
            servings = null,
            sourceUrl = null,
            image = null,
            imageType = null,
            summary = null,
            analyzedInstructions = null,
            spoonacularSourceUrl = null

        ).toRecipeEntity()

        Truth.assertThat(
            recipeEntity == RecipeEntity(
                id = 0,
                previouslyViewed = false,
                favorite = false,
                title = "",
                imageUrl = null,
                summary = "",
                healthScore = null,
                spoonSourceUrl = "",
                instructions = listOf(),
                ingredients = listOf()

            )
        ).isTrue()
    }

    @Test
    fun `GIVEN a null filled RecipeDto WHEN toRecipe() THEN return a valid Recipe`() {
        val recipe = RecipeDto(
            cookingMinutes = null,
            healthScore = null,
            extendedIngredients = null,
            id = null,
            title = null,
            readyInMinutes = null,
            servings = null,
            sourceUrl = null,
            image = null,
            imageType = null,
            summary = null,
            analyzedInstructions = null,
            spoonacularSourceUrl = null

        ).toRecipe()

        Truth.assertThat(
            recipe == Recipe(
                id = 0,
                previouslyViewed = false,
                favorite = false,
                title = "",
                imageUrl = null,
                summary = "",
                healthScore = null,
                spoonSourceUrl = "",
                instructions = listOf(),
                ingredients = listOf()
            )
        ).isTrue()
    }

}