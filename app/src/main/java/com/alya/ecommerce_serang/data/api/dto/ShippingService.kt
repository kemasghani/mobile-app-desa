package com.alya.ecommerce_serang.data.api.dto

import com.google.gson.annotations.SerializedName

data class ShippingService(
    @SerializedName("courier")
    val courier: String
)

data class ShippingServiceRequest(
    @SerializedName("couriers")
    val couriers: List<String>
)