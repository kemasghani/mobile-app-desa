package com.alya.ecommerce_serang.data.api.dto

import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody

data class ComplaintRequest (
    @SerializedName("order_id")
    val orderId: Int,

    @SerializedName("description")
    val description: String,

    @SerializedName("complaintimg")
    val complaintImg: MultipartBody.Part

)