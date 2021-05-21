package com.example.foody.viewmodels

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import android.widget.Toast
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.example.foody.data.Repository
import com.example.foody.data.database.entities.FavoritesEntity
import com.example.foody.data.database.entities.FoodJokeEntity
import com.example.foody.data.database.entities.RecipesEntity
import com.example.foody.models.FoodJoke
import com.example.foody.models.FoodRecipe
import com.example.foody.util.NetworkResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response
import java.lang.Exception

class MainViewModel @ViewModelInject constructor(
    private val repository: Repository,
    application: Application
) : AndroidViewModel(application) {
    //query
    private var nQuieres: HashMap<String, String>? = HashMap()
    private var isPerformingQuery = false
    private var isQueryExcousted = false


    /** ROOM DATABASE */

    val readRecipes: LiveData<List<RecipesEntity>> = repository.local.readRecipes().asLiveData()
    val readFavoriteRecipes: LiveData<List<FavoritesEntity>> =
        repository.local.readFavoriteRecipes().asLiveData()
    val readFoodJoke: LiveData<List<FoodJokeEntity>> = repository.local.readFoodJoke().asLiveData()

    private fun insertRecipes(recipesEntity: RecipesEntity) =
        viewModelScope.launch(Dispatchers.IO) {
            repository.local.insertRecipes(recipesEntity)
        }

    fun insertFavoriteRecipe(favoritesEntity: FavoritesEntity) =
        viewModelScope.launch(Dispatchers.IO) {
            repository.local.insertFavoriteRecipes(favoritesEntity)
        }

    private fun insertFoodJoke(foodJokeEntity: FoodJokeEntity) =
        viewModelScope.launch(Dispatchers.IO) {
            repository.local.insertFoodJoke(foodJokeEntity)
        }

    fun deleteFavoriteRecipe(favoritesEntity: FavoritesEntity) =
        viewModelScope.launch(Dispatchers.IO) {
            repository.local.deleteFavoriteRecipe(favoritesEntity)
        }

    fun deleteAllFavoriteRecipes() =
        viewModelScope.launch(Dispatchers.IO) {
            repository.local.deleteAllFavoriteRecipes()
        }

    /** RETROFIT */
    var recipesResponse: MutableLiveData<NetworkResult<FoodRecipe>> = MutableLiveData()
    var recipesAnyResponse: MutableLiveData<NetworkResult<FoodRecipe>> = MutableLiveData()

    var recipesResponse2: MediatorLiveData<NetworkResult<FoodRecipe>> = MediatorLiveData()
    var searchedRecipesResponse: MutableLiveData<NetworkResult<FoodRecipe>> = MutableLiveData()
    var foodJokeResponse: MutableLiveData<NetworkResult<FoodJoke>> = MutableLiveData()

    //get First recipes
    fun getRecipes(queries: HashMap<String, String>) = viewModelScope.launch {
        nQuieres = queries
        getRecipesSafeCall(nQuieres!!)
    }

    fun getAnyResponse(isSearch: Boolean, queries: HashMap<String, String>) = viewModelScope.launch {
        Log.d("mah mainViewModel", "getAnyResponse " + queries.toString())
        getAnyRecipesSafeCall(isSearch , queries)
    }

    //search query

    fun searchRecipes(searchQuery: Map<String, String>) = viewModelScope.launch {
        searchRecipesSafeCall(searchQuery)
    }

    fun nextQuery() {
        isPerformingQuery = true
        // nQuieres = searchQuery

        if (isPerformingQuery) {
            if (nQuieres!!.containsKey("page")) {
                var page: Int = (nQuieres!!.get("page"))?.toInt() ?: 0
                if (page != null) {
                    if (page < 35) {
                        page++
                        nQuieres!!.replace("page", page.toString())
                        getRecipes(nQuieres!!)
                    } else if (page == 35) {
//                        isPerformingQuery = false
                        isQueryExcousted = true
                        getRecipes(nQuieres!!)

                    } else {
                        nQuieres!!.clear()
                        return
                    }
                }
            }

        }

    }

    fun applyNextQuery(searchQuery: HashMap<String, String>) {
        isPerformingQuery = true
        nQuieres = searchQuery
////       queries["name"] = searchQuery
////       queries["page"] = "$pageNum"
        if (isPerformingQuery) {
            if (nQuieres!!.containsKey("page")) {
                var page: Int = (nQuieres!!.get("page"))?.toInt() ?: 0
                if (page != null) {
                    if (page < 35) {
                        page++
                        nQuieres!!.replace("page", page.toString())
                        searchRecipes(nQuieres!!)
                    } else if (page == 35) {
//                        isPerformingQuery = false
                        isQueryExcousted = true
                        searchRecipes(nQuieres!!)

                    } else {
                        nQuieres!!.clear()
                        return
                    }
                }

            }

        }


    }

    //joke query
    fun getFoodJoke(apiKey: String) = viewModelScope.launch {
        getFoodJokeSafeCall(apiKey)
    }

    private suspend fun getRecipesSafeCall(queries: Map<String, String>) {
        if (!isPerformingQuery) {
            recipesResponse.value = NetworkResult.Loading()
        }
        if (hasInternetConnection()) {
            try {
                Log.d("mah mainViewModel", "getRecipesSafeCall " + hasInternetConnection())

                val response = repository.remote.getRecipes(queries)
                Log.d("mah mainViewModel", "getRecipesSafeCall\n " + response)

                recipesResponse.value = handleFoodRecipesResponse(response)


                val foodRecipe = recipesResponse.value!!.data
                if (foodRecipe != null) {
                    offlineCacheRecipes(foodRecipe)
                }
            } catch (e: Exception) {
                Log.d("mah mainViewModel", "requestApiData error! \n " + e.message)
                recipesResponse.value = NetworkResult.Error("Recipes not found.")
            }
        } else {
            Log.d("mah mainViewModel", "No Internet Connection!")
            recipesResponse.value = NetworkResult.Error("No Internet Connection.")
        }
    }

    private suspend fun getAnyRecipesSafeCall(isSearch: Boolean, queries: Map<String, String>) {

        recipesAnyResponse.value = NetworkResult.Loading()

        if (hasInternetConnection()) {
            try {
                Log.d("mah mainViewModel", "getAnyRecipesSafeCall " + hasInternetConnection())

                val response = repository.remote.getAnyRecipes(isSearch, queries)
                Log.d("mah mainViewModel", "getRecipesSafeCall\n " + response)

                recipesAnyResponse.value = handleAnyFoodRecipesResponse(response)


                val foodRecipe = recipesAnyResponse.value!!.data
                if (foodRecipe != null) {
                    offlineCacheRecipes(foodRecipe)
                }
            } catch (e: Exception) {
                Log.d("mah mainViewModel", "requestApiData error! \n " + e.message)
                recipesAnyResponse.value = NetworkResult.Error("Recipes not found.")
            }
        } else {
            Log.d("mah mainViewModel", "No Internet Connection!")
            recipesAnyResponse.value = NetworkResult.Error("No Internet Connection.")
        }
    }

    private suspend fun searchRecipesSafeCall(searchQuery: Map<String, String>) {
        if (!isPerformingQuery) {
            searchedRecipesResponse.value = NetworkResult.Loading()
        }
        if (hasInternetConnection()) {
            try {
                Log.d("mah mainViewModel", "searchRecipesSafeCall " + hasInternetConnection())

                val response = repository.remote.searchRecipes(searchQuery)
                Log.d("mah mainViewModel", "searchRecipesSafeCall \n " + response)
                searchedRecipesResponse.value = handleFoodRecipesResponse(response)
            } catch (e: Exception) {
                Log.d("mah mainViewModel", "searchRecipesSafeCall error! \n " + e.cause.toString())
                searchedRecipesResponse.value = NetworkResult.Error("Recipes not found.")
            }
        } else {
            Log.d("mah mainViewModel", "searchRecipesSafeCall : \n No Internet Connection!")
            searchedRecipesResponse.value = NetworkResult.Error("No Internet Connection.")
        }

    }

    private suspend fun getFoodJokeSafeCall(apiKey: String) {
        foodJokeResponse.value = NetworkResult.Loading()
        if (hasInternetConnection()) {
            try {
                val response = repository.remote.getFoodJoke(apiKey)
                foodJokeResponse.value = handleFoodJokeResponse(response)

                val foodJoke = foodJokeResponse.value!!.data
                if (foodJoke != null) {
                    offlineCacheFoodJoke(foodJoke)
                }
            } catch (e: Exception) {
                foodJokeResponse.value = NetworkResult.Error("Recipes not found.")
            }
        } else {
            foodJokeResponse.value = NetworkResult.Error("No Internet Connection.")
        }
    }

    private fun offlineCacheRecipes(foodRecipe: FoodRecipe) {
        val recipesEntity = RecipesEntity(foodRecipe)
        insertRecipes(recipesEntity)
    }

    private fun offlineCacheFoodJoke(foodJoke: FoodJoke) {
        val foodJokeEntity = FoodJokeEntity(foodJoke)
        insertFoodJoke(foodJokeEntity)
    }

    private fun handleFoodRecipesResponse(response: Response<FoodRecipe>): NetworkResult<FoodRecipe>? {

        Log.d("mah mainViewModel", "handleFoodRecipesResponse response.body! \n " + response.body())
        Log.d(
            "mah mainViewModel",
            "handleFoodRecipesResponse response.code! \n " + response.code().toString()
        )
        Log.d(
            "mah mainViewModel",
            "handleFoodRecipesResponse erorr + msg! \n " + response.message().toString()
        )

        when {
            response.message().toString().contains("timeout") -> {
                return NetworkResult.Error("Timeout")
            }
            response.code() == 402 -> {
                Log.d(
                    "mah mainViewModel",
                    "handleFoodRecipesResponse erorr! \n 402 error" + response.message()
                )
                return NetworkResult.Error("API Key Limited.")
            }
            response.body()!!.recipes.isNullOrEmpty() -> {

                if (isPerformingQuery && !isQueryExcousted) {
                    nQuieres?.let { applyNextQuery(it) }

                    Log.d(
                        "mah mainViewModel",
                        "handleFoodRecipesResponse error  =  \n " + nQuieres.toString() + " $isPerformingQuery"
                    )

                    return NetworkResult.Check()
                }
                isPerformingQuery = false
                Log.d(
                    "mah mainViewModel",
                    "handleFoodRecipesResponse error null ! \n " + response.message().toString()
                )
                return NetworkResult.Error("No Result Found :(")
            }
            response.isSuccessful -> {
                isPerformingQuery = false

                val foodRecipes = response.body()
                Log.d(
                    "mah mainViewModel",
                    "handleFoodRecipesResponse sucsess! \n " + response.body().toString()
                )
                return NetworkResult.Success(foodRecipes!!)
            }
            else -> {
                Log.d(
                    "mah mainViewModel",
                    "handleFoodRecipesResponse erorr! \n " + response.message().toString()
                )
                return NetworkResult.Error(response.message())
            }
        }
    }

    private fun handleAnyFoodRecipesResponse(response: Response<FoodRecipe>): NetworkResult<FoodRecipe>? {

        Log.d("mah mainViewModel", "handleFoodRecipesResponse response.body! \n " + response.body())
        Log.d(
            "mah mainViewModel",
            "handleFoodRecipesResponse response.code! \n " + response.code().toString()
        )
        Log.d(
            "mah mainViewModel",
            "handleFoodRecipesResponse erorr + msg! \n " + response.message().toString()
        )

        when {
            response.message().toString().contains("timeout") -> {
                return NetworkResult.Error("Timeout")
            }
            response.code() == 402 -> {
                Log.d(
                    "mah mainViewModel",
                    "handleFoodRecipesResponse erorr! \n 402 error" + response.message()
                )
                return NetworkResult.Error("API Key Limited.")
            }
            response.body()!!.recipes.isNullOrEmpty() -> {

//                if (isPerformingQuery && !isQueryExcousted) {
//                    nQuieres?.let { applyNextQuery(it) }
//
//                    Log.d(
//                        "mah mainViewModel",
//                        "handleFoodRecipesResponse error  =  \n " + nQuieres.toString() + " $isPerformingQuery"
//                    )
//
//                    return NetworkResult.Check()
//                }
//                isPerformingQuery = false
                Log.d(
                    "mah mainViewModel",
                    "handleFoodRecipesResponse error null ! \n " + response.message().toString()
                )
                return NetworkResult.Error("No Result Found :(")
            }
            response.isSuccessful -> {
                isPerformingQuery = false

                val foodRecipes = response.body()
                Log.d(
                    "mah mainViewModel",
                    "handleFoodRecipesResponse sucsess! \n " + response.body().toString()
                )
                return NetworkResult.Success(foodRecipes!!)
            }
            else -> {
                Log.d(
                    "mah mainViewModel",
                    "handleFoodRecipesResponse erorr! \n " + response.message().toString()
                )
                return NetworkResult.Error(response.message())
            }
        }
    }

    private fun handleFoodRecipesResponse2(response: Response<FoodRecipe>): NetworkResult<FoodRecipe>? {

        Log.d("mah mainViewModel", "handleFoodRecipesResponse response.body! \n " + response.body())
        Log.d(
            "mah mainViewModel",
            "handleFoodRecipesResponse response.code! \n " + response.code().toString()
        )
        Log.d(
            "mah mainViewModel",
            "handleFoodRecipesResponse erorr + msg! \n " + response.message().toString()
        )

        when {
            response.message().toString().contains("timeout") -> {
                return NetworkResult.Error("Timeout")
            }
            response.code() == 402 -> {
                Log.d(
                    "mah mainViewModel",
                    "handleFoodRecipesResponse erorr! \n 402 error" + response.message()
                )
                return NetworkResult.Error("API Key Limited.")
            }
            response.body()!!.recipes.isNullOrEmpty() -> {

                if (isPerformingQuery && !isQueryExcousted) {
                    nQuieres?.let { applyNextQuery(it) }

                    Log.d(
                        "mah mainViewModel",
                        "handleFoodRecipesResponse error  =  \n " + nQuieres.toString() + " $isPerformingQuery"
                    )

                    return NetworkResult.Check()
                }
                isPerformingQuery = false
                Log.d(
                    "mah mainViewModel",
                    "handleFoodRecipesResponse error null ! \n " + response.message().toString()
                )
                return NetworkResult.Error("No Result Found :(")
            }
            response.isSuccessful -> {
                isPerformingQuery = false

                val foodRecipes = response.body()
                Log.d(
                    "mah mainViewModel",
                    "handleFoodRecipesResponse sucsess! \n " + response.body().toString()
                )
                return NetworkResult.Success(foodRecipes!!)
            }
            else -> {
                Log.d(
                    "mah mainViewModel",
                    "handleFoodRecipesResponse erorr! \n " + response.message().toString()
                )
                return NetworkResult.Error(response.message())
            }
        }
    }

    private fun handleFoodJokeResponse(response: Response<FoodJoke>): NetworkResult<FoodJoke>? {
        return when {
            response.message().toString().contains("timeout") -> {
                NetworkResult.Error("Timeout")
            }
            response.code() == 402 -> {
                NetworkResult.Error("API Key Limited.")
            }
            response.isSuccessful -> {
                val foodJoke = response.body()
                NetworkResult.Success(foodJoke!!)
            }
            else -> {
                NetworkResult.Error(response.message())
            }
        }
    }

    private fun hasInternetConnection(): Boolean {
        val connectivityManager = getApplication<Application>().getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }


}