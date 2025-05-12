package com.alya.ecommerce_serang.data.api.dto

import com.google.gson.annotations.SerializedName

data class UserProfile(

    @field:SerializedName("image")
    val image: String? = null,

    @field:SerializedName("role")
    val role: String,

    @field:SerializedName("user_id")
    val userId: Int,

    @field:SerializedName("phone")
    val phone: String,

    @field:SerializedName("birth_date")
    val birthDate: String,

    @field:SerializedName("name")
    val name: String,

    @field:SerializedName("email")
    val email: String,

    @field:SerializedName("username")
    val username: String
)