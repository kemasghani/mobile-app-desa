package com.alya.ecommerce_serang.data.api.response.customer.order

import com.google.gson.annotations.SerializedName

data class ListProvinceResponse(

	@field:SerializedName("provinces")
	val provinces: List<ProvincesItem>,

	@field:SerializedName("message")
	val message: String
)

data class ProvincesItem(

	@field:SerializedName("province")
	val province: String,

	@field:SerializedName("province_id")
	val provinceId: String
)
