package com.alya.ecommerce_serang.data.api.response.customer.product

import com.google.gson.annotations.SerializedName

data class ReviewProductResponse(

    @field:SerializedName("reviews")
	val reviews: List<ReviewsItem>,

    @field:SerializedName("message")
	val message: String
)

data class ReviewsItem(

	@field:SerializedName("order_item_id")
	val orderItemId: Int,

	@field:SerializedName("review_date")
	val reviewDate: String,

	@field:SerializedName("user_image")
	val userImage: String? = null,

	@field:SerializedName("product_id")
	val productId: Int,

	@field:SerializedName("rating")
	val rating: Int,

	@field:SerializedName("review_text")
	val reviewText: String,

	@field:SerializedName("product_name")
	val productName: String,

	@field:SerializedName("username")
	val username: String
)
