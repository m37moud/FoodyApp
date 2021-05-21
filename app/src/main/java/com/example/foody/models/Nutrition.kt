package com.example.foody.models


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Nutrition(
    @SerializedName("calories")
    val calories: String,
    @SerializedName("servingSize")
    val servingSize: String
): Parcelable