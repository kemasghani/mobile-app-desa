package com.alya.ecommerce_serang.data.api.dto

import com.google.gson.annotations.SerializedName

data class LoginRequest (
    @SerializedName("emailOrPhone") val email: String,
    @SerializedName("password") val password: String,
)