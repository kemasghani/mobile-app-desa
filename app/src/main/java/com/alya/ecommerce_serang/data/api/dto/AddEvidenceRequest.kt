package com.alya.ecommerce_serang.data.api.dto

import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody

data class AddEvidenceRequest (
    @SerializedName("orer_id")
    val orderId : Int,

    @SerializedName("amount")
    val amount : String,

    @SerializedName("evidence")
    val evidence: MultipartBody.Part
)