package com.alya.ecommerce_serang.data.api.dto

import com.google.gson.annotations.SerializedName

data class UpdateCart (
    @SerializedName("cart_item_id")
    val cartItemId: Int,

    @SerializedName("quantity")
    val quantity: Int
)