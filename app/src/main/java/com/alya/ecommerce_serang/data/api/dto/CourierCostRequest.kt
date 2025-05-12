package com.alya.ecommerce_serang.data.api.dto

import com.google.gson.annotations.SerializedName

data class CourierCostRequest(
    @SerializedName("address_id")
    val addressId: Int,

    @SerializedName("items")
    val itemCost: List<CostProduct>
)

data class CostProduct (
    @SerializedName("product_id")
    val productId: Int,

    @SerializedName("quantity")
    val quantity: Int
)
