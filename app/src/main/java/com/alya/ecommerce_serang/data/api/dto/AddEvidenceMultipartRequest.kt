package com.alya.ecommerce_serang.data.api.dto

import okhttp3.MultipartBody
import okhttp3.RequestBody

data class AddEvidenceMultipartRequest(
    val orderId: RequestBody,
    val amount: RequestBody,
    val evidence: MultipartBody.Part
)