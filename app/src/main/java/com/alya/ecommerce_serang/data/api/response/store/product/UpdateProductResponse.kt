package com.alya.ecommerce_serang.data.api.response.store.product

import com.alya.ecommerce_serang.data.api.dto.Product
import com.google.gson.annotations.SerializedName

data class UpdateProductResponse(

	@field:SerializedName("product")
	val product: Product? = null,

	@field:SerializedName("message")
	val message: String? = null
)
