package com.alya.ecommerce_serang.data.api.dto

import com.google.gson.annotations.SerializedName

data class OrderRequestBuy (
    @SerializedName("address_id")
    val addressId : Int,

    @SerializedName("payment_method_id")
    val paymentMethodId : Int,

    @SerializedName("ship_price")
    val shipPrice : Int,

    @SerializedName("ship_name")
    val shipName : String,

    @SerializedName("ship_service")
    val shipService : String,

    @SerializedName("is_negotiable")
    val isNego: Boolean,

    @SerializedName("product_id")
    val productId: Int,

    @SerializedName("quantity")
    val quantity : Int,


    @SerializedName("ship_etd")
    val shipEtd: String
)