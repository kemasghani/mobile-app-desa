package com.alya.ecommerce_serang.data.api.dto

import com.google.gson.annotations.SerializedName

data class Product(

    @field:SerializedName("store_id")
    val storeId: Int? = null,

    @field:SerializedName("image")
    val image: String? = null,

    @field:SerializedName("sppirt")
    val sppirt: String? = null,

    @field:SerializedName("rating")
    val rating: String? = null,

    @field:SerializedName("description")
    val description: String? = null,

    @field:SerializedName("weight")
    val weight: Int? = null,

    @field:SerializedName("is_pre_order")
    val isPreOrder: Boolean? = null,

    @field:SerializedName("condition")
    val condition: String? = null,

    @field:SerializedName("category_id")
    val categoryId: Int? = null,

    @field:SerializedName("price")
    val price: String? = null,

    @field:SerializedName("name")
    val name: String? = null,

    @field:SerializedName("halal")
    val halal: String? = null,

    @field:SerializedName("id")
    val id: Int? = null,

    @field:SerializedName("min_order")
    val minOrder: Int? = null,

    @field:SerializedName("total_sold")
    val totalSold: Int? = null,

    @field:SerializedName("stock")
    val stock: Int? = null,

    @field:SerializedName("status")
    val status: String? = null
)
