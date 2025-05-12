package com.alya.ecommerce_serang.data.api.dto


import com.google.gson.annotations.SerializedName

data class ProductsItem(

    @field:SerializedName("store_id")
    val storeId: Int,

    @field:SerializedName("image")
    val image: String,

    @field:SerializedName("rating")
    val rating: String,

    @field:SerializedName("description")
    val description: String,

    @field:SerializedName("weight")
    val weight: Int,

    @field:SerializedName("is_pre_order")
    val isPreOrder: Boolean,

    @field:SerializedName("category_id")
    val categoryId: Int,

    @field:SerializedName("price")
    val price: String,

    @field:SerializedName("name")
    val name: String,

    @field:SerializedName("id")
    val id: Int,

    @field:SerializedName("min_order")
    val minOrder: Int,

    @field:SerializedName("total_sold")
    val totalSold: Int,

    @field:SerializedName("stock")
    val stock: Int,

    @field:SerializedName("status")
    val status: String
)