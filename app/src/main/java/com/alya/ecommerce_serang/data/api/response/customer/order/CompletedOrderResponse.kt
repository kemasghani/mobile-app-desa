package com.alya.ecommerce_serang.data.api.response.order

import com.google.gson.annotations.SerializedName

data class CompletedOrderResponse(

	@field:SerializedName("message")
	val message: String,

	@field:SerializedName("updatedOrder")
	val updatedOrder: UpdatedOrder,

	@field:SerializedName("updatedItems")
	val updatedItems: List<Any>
)

data class UpdatedOrder(

	@field:SerializedName("auto_completed_at")
	val autoCompletedAt: Any,

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
	val voucherId: Any,

	@field:SerializedName("payment_info_id")
	val paymentInfoId: Any,

	@field:SerializedName("id")
	val id: Int,

	@field:SerializedName("status")
	val status: String
)
