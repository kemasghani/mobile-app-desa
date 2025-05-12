package com.alya.ecommerce_serang.data.api.dto

import com.google.gson.annotations.SerializedName

data class RegisterRequest (
    val name: String?,
    val email: String?,
    val password: String?,
    val username: String?,
    val phone: String?,
    @SerializedName("birth_date") val birthDate: String?,
    @SerializedName("userimg") val image: String?,
    val otp: String? = null
)