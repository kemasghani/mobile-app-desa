package com.alya.ecommerce_serang.data.api.response.order

import com.google.gson.annotations.SerializedName

data class ComplaintResponse(

	@field:SerializedName("voucher")
	val voucher: Voucher,

	@field:SerializedName("message")
	val message: String
)

data class Voucher(

	@field:SerializedName("solution")
	val solution: Any,

	@field:SerializedName("evidence")
	val evidence: String,

	@field:SerializedName("description")
	val description: String,

	@field:SerializedName("created_at")
	val createdAt: String,

	@field:SerializedName("id")
	val id: Int,

	@field:SerializedName("order_id")
	val orderId: Int,

	@field:SerializedName("status")
	val status: String
)
