package com.example.tastytrails.di

import android.content.Context
import androidx.room.Room
import com.example.tastytrails.main.data.local.RecipeDao
import com.example.tastytrails.main.data.local.RecipeDatabase
import com.example.tastytrails.main.data.remote.RecipesApi
import com.example.tastytrails.main.data.repository.RecipeRepositoryImpl
import com.example.tastytrails.main.domain.RecipeRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideRecipesApi(): RecipesApi {
        return Retrofit.Builder()
            .baseUrl(RecipesApi.BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(RecipesApi::class.java)
    }

    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext app: Context): RecipeDatabase {
        return Room.databaseBuilder(
            context = app,
            klass = RecipeDatabase::class.java,
            name = "Recipes.db"
        ).build()
    }

    @Provides
    fun provideTaskDao(database: RecipeDatabase): RecipeDao = database.dao

    @Provides
    @Singleton
    fun provideRecipeRepository(
        recipesApi: RecipesApi,
        recipeDao: RecipeDao
    ): RecipeRepository {
        return RecipeRepositoryImpl(recipesApi, recipeDao)
    }

}