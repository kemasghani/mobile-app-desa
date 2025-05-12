package com.alya.ecommerce_serang.data.api.response.customer.order

import com.google.gson.annotations.SerializedName

data class CreateOrderResponse(

    @field:SerializedName("shipping")
	val shipping: Shipping,

    @field:SerializedName("order_item")
	val orderItem: List<OrderItemItem>,

    @field:SerializedName("message")
	val message: String,

    @field:SerializedName("order")
	val order: Order
)

data class Shipping(

	@field:SerializedName("receipt_num")
	val receiptNum: Int? = null,

	@field:SerializedName("etd")
	val etd: String,

	@field:SerializedName("price")
	val price: String,

	@field:SerializedName("service")
	val service: String,

	@field:SerializedName("name")
	val name: String,

	@field:SerializedName("created_at")
	val createdAt: String,

	@field:SerializedName("id")
	val id: Int,

	@field:SerializedName("order_id")
	val orderId: Int,

	@field:SerializedName("status")
	val status: String
)

data class OrderItemItem(

	@field:SerializedName("quantity")
	val quantity: Int,

	@field:SerializedName("price")
	val price: String,

	@field:SerializedName("subtotal")
	val subtotal: String,

	@field:SerializedName("product_id")
	val productId: Int,

	@field:SerializedName("id")
	val id: Int,

	@field:SerializedName("order_id")
	val orderId: Int
)

data class Order(

	@field:SerializedName("payment_method_id")
	val paymentMethodId: Int,

	@field:SerializedName("auto_completed_at")
	val autoCompletedAt: String? = null,

	@field:SerializedName("updated_at")
	val updatedAt: String,

	@field:SerializedName("total_amount")
	val totalAmount: String,

	@field:SerializedName("user_id")
	val userId: Int,

	@field:SerializedName("address_id")
	val addressId: Int,

	@field:SerializedName("is_negotiable")
	val isNegotiable: Boolean,

	@field:SerializedName("created_at")
	val createdAt: String,

	@field:SerializedName("voucher_id")
	val voucherId: String? = null,

	@field:SerializedName("id")
	val id: Int,

	@field:SerializedName("status")
	val status: String
)
