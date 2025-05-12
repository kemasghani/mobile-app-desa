package com.alya.ecommerce_serang.data.api.response.store.orders

import com.google.gson.annotations.SerializedName

data class UpdateOrderItemResponse(

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("updatedOrder")
	val updatedOrder: UpdatedOrder? = null,

	@field:SerializedName("updatedItems")
	val updatedItems: List<UpdatedItemsItem?>? = null
)

data class UpdatedItemsItem(

	@field:SerializedName("quantity")
	val quantity: Int? = null,

	@field:SerializedName("price")
	val price: String? = null,

	@field:SerializedName("subtotal")
	val subtotal: String? = null,

	@field:SerializedName("product_id")
	val productId: Int? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("order_id")
	val orderId: Int? = null
)

data class UpdatedOrder(

	@field:SerializedName("auto_canceled_at")
	val autoCanceledAt: String? = null,

	@field:SerializedName("payment_method_id")
	val paymentMethodId: Int? = null,

	@field:SerializedName("auto_completed_at")
	val autoCompletedAt: String? = null,

	@field:SerializedName("updated_at")
	val updatedAt: String? = null,

	@field:SerializedName("total_amount")
	val totalAmount: String? = null,

	@field:SerializedName("user_id")
	val userId: Int? = null,

	@field:SerializedName("address_id")
	val addressId: Int? = null,

	@field:SerializedName("is_negotiable")
	val isNegotiable: Boolean? = null,

	@field:SerializedName("created_at")
	val createdAt: String? = null,

	@field:SerializedName("voucher_id")
	val voucherId: Any? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("status")
	val status: String? = null
)
