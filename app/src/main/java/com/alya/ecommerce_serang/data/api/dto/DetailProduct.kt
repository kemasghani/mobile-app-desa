package com.alya.ecommerce_serang.data.api.dto

import com.google.gson.annotations.SerializedName

data class DetailProduct(
    val category: String,
    val description: String,
    val discount: Double?,
    @SerializedName("favorite")
    val wishlist: Boolean,
    val id: String,
    val images: List<String>,
    @SerializedName("in_stock")
    val inStock: Int,
    val price: Double,
    val rating: Double,
    val related: List<ProductsItem>,
    val reviews: Int,
    val title: String,
    @SerializedName("free_delivery")
    val freeDelivery:Boolean
)