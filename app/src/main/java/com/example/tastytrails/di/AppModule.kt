package com.example.tastytrails.di

import android.app.Application
import com.example.tastytrails.main.data.remote.RecipesApi
import com.example.tastytrails.main.data.repository.RecipeRepositoryImpl
import com.example.tastytrails.main.domain.RecipeRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
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

    @Provides
    @Singleton
    fun provideRecipeRepository(recipesApi: RecipesApi, app: Application): RecipeRepository {
        return RecipeRepositoryImpl(recipesApi, app)
    }
}