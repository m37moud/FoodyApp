package com.example.foody.util

class Constants {
//https://us-central1-se7ywmofeed.cloudfunctions.net/app/api/recipes?size=50&page=1

    companion object {

        const val BASE_URL = "https://us-central1-se7ywmofeed.cloudfunctions.net/app/"
        const val BASE_IMAGE_URL = "https://raw.githubusercontent.com/mahmoud-scrapping/kitchen_sayidaty/master/images/"
       const val API_KEY = "f81565d54ad647ed90bc231ebe2991ef"

        const val RECIPE_RESULT_KEY = "recipeBundle"

        // API Query Keys
        const val QUERY_SEARCH = "name"
        const val QUERY_NUMBER = "number"
        const val QUERY_API_KEY = "apiKey"
        const val QUERY_TYPE = "type"
        const val QUERY_DIET = "diet"
        const val QUERY_ADD_RECIPE_INFORMATION = "addRecipeInformation"
        const val QUERY_FILL_INGREDIENTS = "fillIngredients"

        // ROOM Database
        const val DATABASE_NAME = "recipes_database"
        const val RECIPES_TABLE = "recipes_table"
        const val FAVORITE_RECIPES_TABLE = "favorite_recipes_table"
        const val FOOD_JOKE_TABLE = "food_joke_table"

        // Bottom Sheet and Preferences
        const val DEFAULT_RECIPES_NUMBER = "50"
        const val DEFAULT_MEAL_TYPE = ""
        const val DEFAULT_DIET_TYPE = ""

        const val PREFERENCES_NAME = "foody_preferences"
        const val PREFERENCES_MEAL_TYPE = "mealType"
        const val PREFERENCES_MEAL_TYPE_ID = "mealTypeId"
        const val PREFERENCES_DIET_TYPE = "dietType"
        const val PREFERENCES_DIET_TYPE_ID = "dietTypeId"
        const val PREFERENCES_BACK_ONLINE = "backOnline"
        const val PREFERENCES_search_ONLINE = "doSearch"

    }

}