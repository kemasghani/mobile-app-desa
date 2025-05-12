package com.alya.ecommerce_serang.data.api.response.customer.order

import com.google.gson.annotations.SerializedName

data class ListCityResponse(

	@field:SerializedName("cities")
	val cities: List<CitiesItem>,

	@field:SerializedName("message")
	val message: String
)

data class CitiesItem(

	@field:SerializedName("city_name")
	val cityName: String,

	@field:SerializedName("city_id")
	val cityId: String
)
