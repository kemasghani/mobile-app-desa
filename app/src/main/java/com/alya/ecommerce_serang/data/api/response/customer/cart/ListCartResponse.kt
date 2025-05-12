package com.alya.ecommerce_serang.data.api.response.customer.cart

import com.google.gson.annotations.SerializedName

data class ListCartResponse(

    @field:SerializedName("data")
	val data: List<DataItem>,

    @field:SerializedName("message")
	val message: String
)

data class DataItem(

    @field:SerializedName("store_id")
	val storeId: Int,

    @field:SerializedName("cart_items")
	val cartItems: List<CartItemsItem>,

    @field:SerializedName("store_name")
	val storeName: String,

    @field:SerializedName("most_recent_item")
	val mostRecentItem: String
)

data class CartItemsItem(

	@field:SerializedName("quantity")
	val quantity: Int,

	@field:SerializedName("price")
	val price: Int,

	@field:SerializedName("product_id")
	val productId: Int,

	@field:SerializedName("created_at")
	val createdAt: String,

	@field:SerializedName("cart_item_id")
	val cartItemId: Int,

	@field:SerializedName("product_name")
	val productName: String
)
