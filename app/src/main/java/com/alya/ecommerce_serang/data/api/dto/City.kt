package com.alya.ecommerce_serang.data.api.dto

import com.google.gson.annotations.SerializedName

data class City(
    @SerializedName("city_id")
    val cityId: String,

    @SerializedName("city_name")
    val cityName: String,

    @SerializedName("province_id")
    val provinceId: String,

    @SerializedName("province")
    val provinceName: String,

    @SerializedName("type")
    val type: String,

    @SerializedName("postal_code")
    val postalCode: String
)

data class CityResponse(
    @SerializedName("message")
    val message: String,

    @SerializedName("cities")
    val data: List<City>
)