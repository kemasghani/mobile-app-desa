package com.alya.ecommerce_serang.data.api.response.customer.product

import com.google.gson.annotations.SerializedName

data class DetailStoreProductResponse(

	@field:SerializedName("store")
	val store: StoreProduct,

	@field:SerializedName("message")
	val message: String
)

data class PaymentInfoItem(

	val id: Int = 1,

	@field:SerializedName("qris_image")
	val qrisImage: String,

	@field:SerializedName("bank_num")
	val bankNum: String,

	@field:SerializedName("name")
	val name: String
)

data class StoreProduct(

	@field:SerializedName("store_id")
	val storeId: Int,

	@field:SerializedName("shipping_service")
	val shippingService: List<ShippingServiceItem>,

	@field:SerializedName("store_rating")
	val storeRating: String,

	@field:SerializedName("store_name")
	val storeName: String,

	@field:SerializedName("description")
	val description: String,

	@field:SerializedName("store_type")
	val storeType: String,

	@field:SerializedName("payment_info")
	val paymentInfo: List<PaymentInfoItem>,

	@field:SerializedName("store_location")
	val storeLocation: String,

	@field:SerializedName("store_image")
	val storeImage: String,

	@field:SerializedName("status")
	val status: String
)

data class ShippingServiceItem(

	@field:SerializedName("courier")
	val courier: String
)
