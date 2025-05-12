package com.alya.ecommerce_serang.data.api.response.customer.order

import com.google.gson.annotations.SerializedName

data class CourierCostResponse(

	@field:SerializedName("courierCosts")
	val courierCosts: List<CourierCostsItem>
)

data class CourierCostsItem(

	@field:SerializedName("courier")
	val courier: String,

	@field:SerializedName("services")
	val services: List<ServicesItem>
)

data class ServicesItem(

	@field:SerializedName("cost")
	val cost: Int,

	@field:SerializedName("etd")
	val etd: String,

	@field:SerializedName("service")
	val service: String,

	@field:SerializedName("description")
	val description: String
)
