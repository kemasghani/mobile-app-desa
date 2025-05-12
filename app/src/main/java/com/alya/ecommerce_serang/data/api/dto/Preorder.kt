package com.alya.ecommerce_serang.data.api.dto

import com.google.gson.annotations.SerializedName

data class Preorder(

    @field:SerializedName("duration")
    val duration: Int? = null,

    @field:SerializedName("product_id")
    val productId: Int? = null,

    @field:SerializedName("id")
    val id: Int? = null
)
