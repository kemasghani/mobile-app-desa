package com.alya.ecommerce_serang.data.api.response.customer.product

import com.google.gson.annotations.SerializedName

data class AllStoreResponse(

    @field:SerializedName("store")
	val store: AllStore,

    @field:SerializedName("message")
	val message: String
)

data class AllStore(

	@field:SerializedName("store_name")
	val storeName: String,

	@field:SerializedName("description")
	val description: String,

	@field:SerializedName("store_type")
	val storeType: String,

	@field:SerializedName("store_location")
	val storeLocation: String,

	@field:SerializedName("store_image")
	val storeImage: Any,

	@field:SerializedName("status")
	val status: String
)
