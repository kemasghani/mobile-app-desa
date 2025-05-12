package com.alya.ecommerce_serang.data.api.dto

import com.google.gson.annotations.SerializedName

data class StoreAddress(
    @SerializedName("id")
    val id: String? = null,

    @SerializedName("store_id")
    val storeId: String? = null,

    @SerializedName("province_id")
    val provinceId: String = "",

    @SerializedName("province_name")
    val provinceName: String = "",

    @SerializedName("city_id")
    val cityId: String = "",

    @SerializedName("city_name")
    val cityName: String = "",

    @SerializedName("street")
    val street: String = "",

    @SerializedName("subdistrict")
    val subdistrict: String = "",

    @SerializedName("detail")
    val detail: String? = null,

    @SerializedName("postal_code")
    val postalCode: String = "",

    @SerializedName("latitude")
    val latitude: Double? = 0.0,

    @SerializedName("longitude")
    val longitude: Double? = 0.0,

    @SerializedName("created_at")
    val createdAt: String? = null,

    @SerializedName("updated_at")
    val updatedAt: String? = null
)

data class StoreAddressResponse(
    @SerializedName("message")
    val message: String,

    @SerializedName("store")
    val data: StoreAddress? = null
)