package com.alya.ecommerce_serang.data.api.response.store

import com.google.gson.annotations.SerializedName

data class StoreResponse(
    val message: String,
    val store: Store
)

data class Store(
    @SerializedName("store_id") val storeId: Int,
    @SerializedName("store_status") val storeStatus: String,
    @SerializedName("store_name") val storeName: String,
    @SerializedName("user_name") val userName: String,
    val email: String,
    @SerializedName("user_phone") val userPhone: String,
    val balance: String
)