package com.example.tastytrails.di

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStoreFile
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

private const val THEME_PREFERENCES_NAME = "theme_preferences"

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
        recipeDao: RecipeDao,
        app: Application
    ): RecipeRepository {
        return RecipeRepositoryImpl(recipesApi, recipeDao, app)
    }

    @Singleton
    @Provides
    fun providePreferencesDataStore(@ApplicationContext appContext: Context): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            corruptionHandler = ReplaceFileCorruptionHandler(
                produceNewData = { emptyPreferences() }
            ),
            migrations = listOf(SharedPreferencesMigration(appContext, THEME_PREFERENCES_NAME)),
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
            produceFile = { appContext.preferencesDataStoreFile(THEME_PREFERENCES_NAME) }
        )
    }

}