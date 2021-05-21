package com.example.foody.viewmodels

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.foody.data.DataStoreRepository
import com.example.foody.util.Constants.Companion.API_KEY
import com.example.foody.util.Constants.Companion.DEFAULT_DIET_TYPE
import com.example.foody.util.Constants.Companion.DEFAULT_MEAL_TYPE
import com.example.foody.util.Constants.Companion.DEFAULT_RECIPES_NUMBER
import com.example.foody.util.Constants.Companion.QUERY_ADD_RECIPE_INFORMATION
import com.example.foody.util.Constants.Companion.QUERY_API_KEY
import com.example.foody.util.Constants.Companion.QUERY_DIET
import com.example.foody.util.Constants.Companion.QUERY_FILL_INGREDIENTS
import com.example.foody.util.Constants.Companion.QUERY_NUMBER
import com.example.foody.util.Constants.Companion.QUERY_SEARCH
import com.example.foody.util.Constants.Companion.QUERY_TYPE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class RecipesViewModel @ViewModelInject constructor(
    application: Application,
    private val dataStoreRepository: DataStoreRepository
) : AndroidViewModel(application) {

    private var mealType = DEFAULT_MEAL_TYPE
    private var dietType = DEFAULT_DIET_TYPE


    // network status
    var networkStatus = false
    var backOnline = false


    // search status

    var searching = false

    val readMealAndDietType = dataStoreRepository.readMealAndDietType
    val readBackOnline = dataStoreRepository.readBackOnline.asLiveData()

    //read search from data store
    val readSearch = dataStoreRepository.readSearch

    fun saveMealAndDietType(mealType: String, mealTypeId: Int, dietType: String, dietTypeId: Int) =
        viewModelScope.launch(Dispatchers.IO) {
            dataStoreRepository.saveMealAndDietType(mealType, mealTypeId, dietType, dietTypeId)
        }

    private fun saveBackOnline(backOnline: Boolean) =
        viewModelScope.launch(Dispatchers.IO) {
            dataStoreRepository.saveBackOnline(backOnline)
        }

    fun saveSearch(doSearch: Boolean) =
        viewModelScope.launch(Dispatchers.IO) {
            dataStoreRepository.saveSearch(doSearch)
        }

    fun applyQueries(): HashMap<String, String> {
        val queries: HashMap<String, String> = HashMap()

        queries["page"] = (1..35).random().toString()
        queries["size"] = "50"


        return queries
    }


    fun applySearchQuery(searchQuery: String, pageNum: Int): HashMap<String, String> {
        val queries: HashMap<String, String> = HashMap()


        viewModelScope.launch {
            readMealAndDietType.collect { value ->
                mealType = value.selectedMealType
                dietType = value.selectedDietType
            }
        }

        queries["name"] = searchQuery
        queries["page"] = "$pageNum"
        queries["size"] = "50"
        queries["category"] = mealType
        queries["cuisine"] = dietType
        return queries
    }

    fun applyAnyQuery(): HashMap<String, String> {


        val queries: HashMap<String, String> = HashMap()

        Log.d(
            "mah RecipesViewModel",
            "requestApiData called : " + mealType.toString() + " - "+ dietType.toString()
        )
        viewModelScope.launch {
            readMealAndDietType.collect { value ->

                dietType = value.selectedDietType

                mealType = value.selectedMealType
            }
        }

        if (mealType == "كل الوصفات") {
            mealType = ""

        }
        if (dietType == "كل الأطباق") {
            dietType = ""

        }
        viewModelScope.launch {
            readSearch.collect { value ->
                searching = value
            }
        }

        if (searching) {

            queries.clear()

            queries["name"] = ""
            queries["page"] = "1"
            queries["size"] = "50"
            queries["category"] = mealType
            queries["cuisine"] = dietType
            return queries
        }
        if (!searching) {
            queries.clear()
            queries["page"] = (1..35).random().toString()
            queries["size"] = "50"


            return queries
        }

        return queries
    }




    fun showNetworkStatus() {
        if (!networkStatus) {
            Toast.makeText(getApplication(), "No Internet Connection.", Toast.LENGTH_SHORT).show()
            saveBackOnline(true)
        } else if (networkStatus) {
            if (backOnline) {
                Toast.makeText(getApplication(), "We're back online.", Toast.LENGTH_SHORT).show()
                saveBackOnline(false)
            }
        }
    }

}