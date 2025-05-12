package com.alya.ecommerce_serang.data.api.response.store.orders

import com.google.gson.annotations.SerializedName

data class PaymentConfirmationResponse(

	@field:SerializedName("message")
	val message: String? = null
)
