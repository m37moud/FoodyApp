package com.example.foody.models


import com.google.gson.annotations.SerializedName

data class FoodRecipe(
    @SerializedName("count")
    val count: Int,
    @SerializedName("max_page")
    val maxPage: Int,
    @SerializedName("page")
    val page: String,
    @SerializedName("recipes")
    val recipes: List<Recipe>
)