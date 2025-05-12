package com.alya.ecommerce_serang.data.api.response.store.product

import com.alya.ecommerce_serang.data.api.dto.ProductsItem
import com.google.gson.annotations.SerializedName

data class ViewStoreProductsResponse(

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("products")
	val products: List<ProductsItem?>? = null
)