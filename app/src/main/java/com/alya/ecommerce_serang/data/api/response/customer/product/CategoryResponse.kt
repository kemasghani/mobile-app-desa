package com.alya.ecommerce_serang.data.api.response.customer.product

import com.alya.ecommerce_serang.data.api.dto.CategoryItem
import com.google.gson.annotations.SerializedName

data class CategoryResponse(

	@field:SerializedName("Category")
	val category: List<CategoryItem>,

	@field:SerializedName("message")
	val message: String
)


