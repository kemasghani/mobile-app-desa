package com.alya.ecommerce_serang.data.api.dto

import com.google.gson.annotations.SerializedName

data class PaymentMethod(
    @SerializedName("id")
    val id: Int,

    @SerializedName("bank_num")
    val bankNum: String,

    @SerializedName("bank_name")
    val bankName: String,

    @SerializedName("qris_image")
    val qrisImage: String?,

    @SerializedName("account_name")
    val accountName: String?
)

data class PaymentMethodResponse(
    @SerializedName("message")
    val message: String,

    @SerializedName("payment")
    val payment: List<PaymentMethod>
)

data class AddPaymentMethodRequest(
    @SerializedName("bank_name")
    val bankName: String,

    @SerializedName("bank_num")
    val bankNum: String,

    @SerializedName("account_name")
    val accountName: String

    // qris will be sent as multipart form data
)

data class DeletePaymentMethodResponse(
    @SerializedName("message")
    val message: String,

    @SerializedName("success")
    val success: Boolean
)

data class AddPaymentMethodResponse(
    @SerializedName("message")
    val message: String,

    @SerializedName("success")
    val success: Boolean,

    @SerializedName("payment_method")
    val paymentMethod: PaymentMethod?
)