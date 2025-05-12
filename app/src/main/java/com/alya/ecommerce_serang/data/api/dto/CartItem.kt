package com.alya.ecommerce_serang.data.api.dto

import com.google.gson.annotations.SerializedName

data class CartItem (
    @SerializedName("product_id")
    val productId: Int,

    @SerializedName("quantity")
    val quantity: Int
)