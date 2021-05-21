package com.example.foody.data

import android.util.Log
import com.example.foody.data.network.FoodRecipesApi
import com.example.foody.models.FoodJoke
import com.example.foody.models.FoodRecipe
import kotlinx.coroutines.flow.Flow
import retrofit2.Response
import javax.inject.Inject

class RemoteDataSource @Inject constructor(
    private val foodRecipesApi: FoodRecipesApi
) {

    suspend fun getRecipes(queries: Map<String, String>): Response<FoodRecipe> {
        Log.d("RemoteDataSource", "getRecipes \n" + foodRecipesApi.getRecipes(queries).toString())
        return foodRecipesApi.getRecipes(queries)
    }
    suspend fun getAnyRecipes(isSearch:Boolean , queries: Map<String, String>): Response<FoodRecipe> {
//        Log.d("RemoteDataSource", "getRecipes \n" + foodRecipesApi.getRecipes(queries).toString())
        if(!isSearch) {
            return foodRecipesApi.getRecipes(queries)
        }else
            return foodRecipesApi.searchRecipes(queries)
    }

    suspend fun searchRecipes(searchQuery: Map<String, String>): Response<FoodRecipe> {
        return foodRecipesApi.searchRecipes(searchQuery)
    }

    suspend fun getFoodJoke(apiKey: String): Response<FoodJoke> {
        return foodRecipesApi.getFoodJoke(apiKey)
    }

}