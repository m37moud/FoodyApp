package com.example.foody.models


import android.os.Parcelable
import com.example.foody.models.Ingredient
import com.example.foody.models.Nutrition
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue

@Parcelize
data class Recipe(
    @SerializedName("category")
    val category: String,
    @SerializedName("cuisine")
    val cuisine: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("images")
    val images: List<String>?,
    @SerializedName("ingredients")
    val ingredients:  @RawValue List<Ingredient>,
    @SerializedName("instructions")
    val instructions: List<String>,
    @SerializedName("name")
    val name: String,
    @SerializedName("nutrition")
    val nutrition: @RawValue Nutrition,
    @SerializedName("prepTime")
    val prepTime: String,
    @SerializedName("totalTime")
    val totalTime: String,
    @SerializedName("yield")
    val yield: String
):Parcelable