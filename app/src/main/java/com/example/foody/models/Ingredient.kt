package com.example.foody.models


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Ingredient(
    @SerializedName("name")
    val name: String,
    @SerializedName("quantity")
    val quantity: String
): Parcelable