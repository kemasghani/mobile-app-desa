package com.alya.ecommerce_serang.data.api.response.customer.cart

import com.google.gson.annotations.SerializedName

data class AddCartResponse(

    @field:SerializedName("data")
	val data: Data,

    @field:SerializedName("message")
	val message: String
)

data class Data(

	@field:SerializedName("cart_id")
	val cartId: Int,

	@field:SerializedName("quantity")
	val quantity: Int,

	@field:SerializedName("product_id")
	val productId: Int,

	@field:SerializedName("created_at")
	val createdAt: String,

	@field:SerializedName("id")
	val id: Int
)
