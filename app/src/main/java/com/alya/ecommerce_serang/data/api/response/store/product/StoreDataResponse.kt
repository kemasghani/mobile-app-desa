package com.alya.ecommerce_serang.data.api.response.store.product

import com.google.gson.annotations.SerializedName

data class StoreDataResponse(
    val message: String,
    val store: Store? = null,
    val shipping: List<Shipping>? = emptyList(),
    val payment: List<Payment> = emptyList()
)

data class Store(
    @SerializedName("store_id") val storeId: Int,
    @SerializedName("store_status") val storeStatus: String,
    @SerializedName("store_name") val storeName: String,
    @SerializedName("user_name") val userName: String,
    val email: String,
    @SerializedName("user_phone") val userPhone: String,
    val balance: String,
    val ktp: String,
    val npwp: String,
    val nib: String,
    val persetujuan: String?,
    @SerializedName("store_image") val storeImage: String,
    @SerializedName("store_description") val storeDescription: String,
    @SerializedName("is_on_leave") val isOnLeave: Boolean,
    @SerializedName("store_type_id") val storeTypeId: Int,
    @SerializedName("store_type") val storeType: String,
    val id: Int,
    val latitude: String,
    val longitude: String,
    val street: String,
    val subdistrict: String,
    @SerializedName("postal_code") val postalCode: String,
    val detail: String,
    @SerializedName("is_store_location") val isStoreLocation: Boolean,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("city_id") val cityId: Int,
    @SerializedName("province_id") val provinceId: Int,
    val phone: String?,
    val recipient: String?,
    @SerializedName("approval_status") val approvalStatus: String,
    @SerializedName("approval_reason") val approvalReason: String?
)

data class Shipping(
    val courier: String
)

data class Payment(
    val id: Int,
    @SerializedName("bank_num") val bankNum: String,
    @SerializedName("bank_name") val bankName: String,
    @SerializedName("qris_image") val qrisImage: String?,
    @SerializedName("account_name") val accountName: String?
)