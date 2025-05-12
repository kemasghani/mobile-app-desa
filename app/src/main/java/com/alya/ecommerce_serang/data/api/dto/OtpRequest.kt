package com.alya.ecommerce_serang.data.api.dto

import com.google.gson.annotations.SerializedName

data class OtpRequest(
    @SerializedName("email") val email: String
)