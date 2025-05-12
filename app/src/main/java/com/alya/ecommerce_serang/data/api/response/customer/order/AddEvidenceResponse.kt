package com.alya.ecommerce_serang.data.api.response.order

import com.google.gson.annotations.SerializedName

data class AddEvidenceResponse(

	@field:SerializedName("evidence")
	val evidence: Evidence,

	@field:SerializedName("message")
	val message: String
)

data class Evidence(

	@field:SerializedName("amount")
	val amount: String,

	@field:SerializedName("evidence")
	val evidence: String,

	@field:SerializedName("uploaded_at")
	val uploadedAt: String,

	@field:SerializedName("id")
	val id: Int,

	@field:SerializedName("order_id")
	val orderId: Int,

	@field:SerializedName("status")
	val status: String
)
