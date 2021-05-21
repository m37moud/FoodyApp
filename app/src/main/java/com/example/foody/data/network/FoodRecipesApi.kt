package com.example.foody.data.network

import com.example.foody.models.FoodJoke
import com.example.foody.models.FoodRecipe
import kotlinx.coroutines.flow.Flow
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.QueryMap

interface FoodRecipesApi {


    /**
     * this method send get request to the api/filter path to search for specific recipes
     * @param page page number to fetch
     * @param size number of recipes per request
     * @param name the recipe name to search for
     * @param category the recipe category to search for
     * @param cuisine the recipe cuisine to search for
     */


    @GET("api/recipes")
    suspend fun getRecipes(
        @QueryMap queries: Map<String, String>
    ): Response<FoodRecipe>

    @GET("api/filter")
    suspend fun searchRecipes(
        @QueryMap searchQuery: Map<String, String>
    ): Response<FoodRecipe>

    @GET("food/jokes/random")
    suspend fun getFoodJoke(
        @Query("apiKey") apiKey: String
    ): Response<FoodJoke>

}