package com.alya.ecommerce_serang.data.api.response.customer.order

import com.alya.ecommerce_serang.data.api.dto.OrdersItem
import com.google.gson.annotations.SerializedName

data class OrderListResponse(

	@field:SerializedName("orders")
	val orders: List<OrdersItem>,

	@field:SerializedName("message")
	val message: String
)

data class OrderItemsItem(

	@field:SerializedName("review_id")
	val reviewId: Int? = null,

	@field:SerializedName("quantity")
	val quantity: Int,

	@field:SerializedName("price")
	val price: Int,

	@field:SerializedName("subtotal")
	val subtotal: Int,

	@field:SerializedName("product_image")
	val productImage: String,

	@field:SerializedName("store_name")
	val storeName: String,

	@field:SerializedName("product_price")
	val productPrice: Int,

	@field:SerializedName("product_name")
	val productName: String
)
