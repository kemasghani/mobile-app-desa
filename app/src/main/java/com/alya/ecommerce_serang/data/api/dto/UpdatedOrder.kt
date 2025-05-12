package com.alya.ecommerce_serang.data.api.dto

import com.google.gson.annotations.SerializedName

data class UpdatedOrder(

    @field:SerializedName("payment_method_id")
    val paymentMethodId: Int? = null,

    @field:SerializedName("updated_at")
    val updatedAt: String? = null,

    @field:SerializedName("total_amount")
    val totalAmount: String? = null,

    @field:SerializedName("user_id")
    val userId: Int? = null,

    @field:SerializedName("address_id")
    val addressId: Int? = null,

    @field:SerializedName("is_negotiable")
    val isNegotiable: Boolean? = null,

    @field:SerializedName("created_at")
    val createdAt: String? = null,

    @field:SerializedName("voucher_id")
    val voucherId: Any? = null,

    @field:SerializedName("id")
    val id: Int? = null,

    @field:SerializedName("status")
    val status: String? = null
)
