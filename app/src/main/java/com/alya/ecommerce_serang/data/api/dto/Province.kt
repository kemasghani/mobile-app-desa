package com.alya.ecommerce_serang.data.api.dto

import com.google.gson.annotations.SerializedName

data class Province(
    @SerializedName("province_id")
    val provinceId: String,

    @SerializedName("province")
    val provinceName: String
)

data class ProvinceResponse(
    @SerializedName("message")
    val message: String,

    @SerializedName("provinces")
    val data: List<Province>
)