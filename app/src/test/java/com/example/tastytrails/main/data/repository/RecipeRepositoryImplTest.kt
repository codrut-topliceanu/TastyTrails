package com.example.tastytrails.main.data.repository

import android.app.Application
import android.util.Log
import com.example.tastytrails.main.data.local.RecipeDao
import com.example.tastytrails.main.data.local.RecipeEntity
import com.example.tastytrails.main.data.mappers.toRecipeEntity
import com.example.tastytrails.main.data.remote.RecipeDto
import com.example.tastytrails.main.data.remote.RecipeResponse
import com.example.tastytrails.main.data.remote.RecipesApi
import com.example.tastytrails.main.domain.Recipe
import com.example.tastytrails.main.domain.RecipeRepository
import com.example.tastytrails.utils.RepoResult
import com.google.common.truth.Truth
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Test
import retrofit2.Response

@Suppress("SameParameterValue")
class RecipeRepositoryImplTest {

    private lateinit var recipeRepository: RecipeRepository
    private lateinit var recipesApi: RecipesApi
    private lateinit var recipeDao: RecipeDao
    private lateinit var context: Application

    @Before
    fun setUp() {
        recipesApi = mockk()
        recipeDao = mockk()
        context = mockk()
        recipeRepository = RecipeRepositoryImpl(recipesApi, recipeDao, context)

        mockkStatic(Log::class)
        every { Log.v(any(), any()) } returns 0
        every { Log.d(any(), any()) } returns 0
        every { Log.i(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
        every { context.getString(any()) } returns "testString"
    }

    @Test
    fun `WHEN searchForRecipes() response is successful THEN return success`() = runBlocking {
        // given
        val query = "chicken"
        val searchByName = true
        val response = Response.success(
            fakeRecipeResponse(listOf("chicken"), 50)
        )

        coEvery {
            recipesApi.getRecipesByName(
                recipeNames = query,
                resultsNumber = any(),
                fillIngredients = true,
                addRecipeInformation = true
            )
        } returns response

        // when
        val result = recipeRepository.searchForRecipes(query, searchByName)

        // then
        Truth.assertThat(result is RepoResult.Success).isTrue()
        val repoResult = result.data as List<Recipe>
        Truth.assertThat(repoResult.firstOrNull()?.title == "chicken").isTrue()
        Truth.assertThat(repoResult.firstOrNull()?.healthScore == 50).isTrue()
    }

    @Test
    fun `WHEN searchForRecipes() response fails THEN return error`() = runBlocking {
        // given
        val query = "chicken"
        val searchByName = true

        val response = Response.error<RecipeResponse>(
            400,
            "{}".toResponseBody("application/json".toMediaTypeOrNull())
        )
        coEvery { recipesApi.getRecipesByName(query) } returns response

        // when
        val result = recipeRepository.searchForRecipes(query, searchByName)

        // then
        Truth.assertThat(result is RepoResult.Error).isTrue()
    }

    @Test
    fun `WHEN searchForRecipes() body is null THEN return error`() = runBlocking {
        // given
        val query = "chicken"
        val searchByName = true

        val response = Response.success<RecipeResponse>(null)
        coEvery { recipesApi.getRecipesByName(query) } returns response

        // when
        val result = recipeRepository.searchForRecipes(query, searchByName)

        // then
        Truth.assertThat(result is RepoResult.Error).isTrue()
    }

    @Test
    fun `WHEN upsertRecipe() THEN insert recipe into database`() = runBlocking {
        // given
        val recipe = Recipe(
            id = 9167,
            previouslyViewed = false,
            favorite = false,
            title = "pasta",
            imageUrl = null,
            summary = "pasta pasta",
            healthScore = null,
            spoonSourceUrl = "https://search.yahoo.com/search?p=doming",
            instructions = listOf(),
            ingredients = listOf()
        )
        val recipeEntity = recipe.toRecipeEntity()
        coEvery { recipeDao.upsert(recipeEntity) } just Runs

        // when
        val result = recipeRepository.upsertRecipe(recipe)

        // then
        Truth.assertThat(result is RepoResult.Success).isTrue()
        coVerify(exactly = 1) { recipeDao.upsert(recipeEntity) }
    }

    @Test
    fun `WHEN upsertRecipe() exception is thrown THEN return error`() = runBlocking {
        // given
        val recipe = Recipe(
            id = 9167,
            previouslyViewed = false,
            favorite = false,
            title = "he pasta away",
            imageUrl = null,
            summary = "rest in spaghetti",
            healthScore = null,
            spoonSourceUrl = "https://search.yahoo.com/search?p=doming",
            instructions = listOf(),
            ingredients = listOf()
        )
        val recipeEntity = recipe.toRecipeEntity()
        coEvery { recipeDao.upsert(recipeEntity) } throws Exception()

        // when
        val result = recipeRepository.upsertRecipe(recipe)

        // then
        Truth.assertThat(result is RepoResult.Error).isTrue()
        coVerify(exactly = 1) { recipeDao.upsert(recipeEntity) }
    }

    @Test
    fun `WHEN getRecipesByFavorites() THEN return favorite recipes`() = runBlocking {
        // given
        val recipeEntityList =
            fakeRecipeEntities(listOf("1", "2"), 51)

        coEvery { recipeDao.getAllFavorite(true) } returns recipeEntityList

        // when
        val result = recipeRepository.getRecipesByFavorites(true)

        // then
        Truth.assertThat(result is RepoResult.Success).isTrue()
        Truth.assertThat(result.data?.size == 2).isTrue()
    }

    @Test
    fun `WHEN getRecipesByFavorites() throws exception THEN return error`() = runBlocking {
        // given
        coEvery { recipeDao.getAllFavorite(true) } throws Exception()

        // when
        val result = recipeRepository.getRecipesByFavorites(true)

        // then
        Truth.assertThat(result is RepoResult.Error).isTrue()
    }

    @Test
    fun `WHEN getRecipesByPreviouslyViewed() THEN return favorite recipes`() = runBlocking {
        // given
        val recipeEntityList =
            fakeRecipeEntities(listOf("1", "2"), 51)

        coEvery { recipeDao.getAllViewed(true) } returns recipeEntityList

        // when
        val result = recipeRepository.getRecipesByPreviouslyViewed(true)

        // then
        Truth.assertThat(result is RepoResult.Success).isTrue()
        Truth.assertThat(result.data?.size == 2).isTrue()
    }

    @Test
    fun `WHEN getRecipesByPreviouslyViewed() throws exception THEN return error`() = runBlocking {
        // given
        coEvery { recipeDao.getAllViewed(true) } throws Exception()

        // when
        val result = recipeRepository.getRecipesByPreviouslyViewed(true)

        // then
        Truth.assertThat(result is RepoResult.Error).isTrue()
    }

    private fun fakeRecipeResponse(
        titles: List<String>,
        healthScore: Int
    ): RecipeResponse {
        return RecipeResponse(
            titles.map {
                RecipeDto(
                    cookingMinutes = null,
                    healthScore = healthScore,
                    extendedIngredients = listOf(),
                    id = 432,
                    title = it,
                    readyInMinutes = null,
                    servings = null,
                    sourceUrl = null,
                    image = null,
                    imageType = null,
                    summary = null,
                    analyzedInstructions = listOf(),
                    spoonacularSourceUrl = null
                )
            },
            offset = null,
            number = null,
            totalResults = null
        )
    }

    private fun fakeRecipeEntities(
        titles: List<String>,
        healthScore: Int
    ): List<RecipeEntity> {
        return titles.map {
            RecipeEntity(
                healthScore = healthScore,
                id = 432,
                title = it,
                summary = "mamma mia",
                previouslyViewed = false,
                favorite = false,
                imageUrl = null,
                spoonSourceUrl = "https://search.yahoo.com/search?p=quem",
                instructions = listOf(),
                ingredients = listOf(),
            )
        }
    }

}