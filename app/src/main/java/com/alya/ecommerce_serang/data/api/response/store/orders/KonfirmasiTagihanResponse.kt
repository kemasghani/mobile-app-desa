package com.alya.ecommerce_serang.data.api.response.store.orders

import com.google.gson.annotations.SerializedName

data class KonfirmasiTagihanResponse(

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("updatedOrder")
	val updatedOrder: UpdatedOrder? = null,

	@field:SerializedName("updatedItems")
	val updatedItems: List<Any?>? = null
)
