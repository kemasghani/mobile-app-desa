package com.alya.ecommerce_serang.data.api.response.customer.product

import com.alya.ecommerce_serang.data.api.dto.Store
import com.google.gson.annotations.SerializedName

data class StoreResponse(

    @field:SerializedName("shipping")
	val shipping: List<ShippingItem>,

    @field:SerializedName("payment")
	val payment: List<PaymentItem>,

    @field:SerializedName("store")
	val store: Store,

    @field:SerializedName("message")
	val message: String
)

data class Store(

	@field:SerializedName("approval_reason")
	val approvalReason: String,

	@field:SerializedName("store_status")
	val storeStatus: String,

	@field:SerializedName("sppirt")
	val sppirt: String,

	@field:SerializedName("user_name")
	val userName: String,

	@field:SerializedName("nib")
	val nib: String,

	@field:SerializedName("latitude")
	val latitude: String,

	@field:SerializedName("store_type_id")
	val storeTypeId: Int,

	@field:SerializedName("balance")
	val balance: String,

	@field:SerializedName("street")
	val street: String,

	@field:SerializedName("store_name")
	val storeName: String,

	@field:SerializedName("user_phone")
	val userPhone: String,

	@field:SerializedName("halal")
	val halal: String,

	@field:SerializedName("id")
	val id: Int,

	@field:SerializedName("email")
	val email: String,

	@field:SerializedName("store_image")
	val storeImage: String? = null,

	@field:SerializedName("longitude")
	val longitude: String,

	@field:SerializedName("store_id")
	val storeId: Int,

	@field:SerializedName("is_store_location")
	val isStoreLocation: Boolean,

	@field:SerializedName("ktp")
	val ktp: String,

	@field:SerializedName("approval_status")
	val approvalStatus: String,

	@field:SerializedName("npwp")
	val npwp: String,

	@field:SerializedName("store_type")
	val storeType: String,

	@field:SerializedName("is_on_leave")
	val isOnLeave: Boolean,

	@field:SerializedName("user_id")
	val userId: Int,

	@field:SerializedName("province_id")
	val provinceId: Int,

	@field:SerializedName("phone")
	val phone: String,

	@field:SerializedName("subdistrict")
	val subdistrict: String,

	@field:SerializedName("recipient")
	val recipient: String,

	@field:SerializedName("detail")
	val detail: String,

	@field:SerializedName("postal_code")
	val postalCode: String,

	@field:SerializedName("store_description")
	val storeDescription: String,

	@field:SerializedName("city_id")
	val cityId: Int
)

data class ShippingItem(

	@field:SerializedName("courier")
	val courier: String
)

data class PaymentItem(

	@field:SerializedName("qris_image")
	val qrisImage: String,

	@field:SerializedName("bank_num")
	val bankNum: String,

	@field:SerializedName("bank_name")
	val bankName: String,

	@field:SerializedName("id")
	val id: Int
)
