package com.alya.ecommerce_serang.data.api.dto

import com.alya.ecommerce_serang.data.api.response.customer.order.OrderItemsItem
import com.google.gson.annotations.SerializedName

data class OrdersItem(

    @field:SerializedName("receipt_num")
    val receiptNum: Int? = null,

    @field:SerializedName("latitude")
    val latitude: String,

    @field:SerializedName("created_at")
    val createdAt: String,

    @field:SerializedName("voucher_code")
    val voucherCode: String? = null,

    @field:SerializedName("updated_at")
    val updatedAt: String,

    @field:SerializedName("etd")
    val etd: String,

    @field:SerializedName("street")
    val street: String,

    @field:SerializedName("cancel_date")
    val cancelDate: String? = null,

    @field:SerializedName("longitude")
    val longitude: String,

    @field:SerializedName("shipment_status")
    val shipmentStatus: String,

    @field:SerializedName("order_items")
    val orderItems: List<OrderItemsItem>,

    @field:SerializedName("auto_completed_at")
    val autoCompletedAt: String? = null,

    @field:SerializedName("is_store_location")
    val isStoreLocation: Boolean? = null,

    @field:SerializedName("voucher_name")
    val voucherName: String? = null,

    @field:SerializedName("address_id")
    val addressId: Int,

    @field:SerializedName("cancel_reason")
    val cancelReason: String? = null,

    @field:SerializedName("total_amount")
    val totalAmount: String,

    @field:SerializedName("user_id")
    val userId: Int,

    @field:SerializedName("province_id")
    val provinceId: Int,

    @field:SerializedName("courier")
    val courier: String,

    @field:SerializedName("subdistrict")
    val subdistrict: String,

    @field:SerializedName("service")
    val service: String,

    @field:SerializedName("shipment_price")
    val shipmentPrice: String,

    @field:SerializedName("voucher_id")
    val voucherId: Int? = null,

    @field:SerializedName("payment_info_id")
    val paymentInfoId: Int? = null,

    @field:SerializedName("detail")
    val detail: String,

    @field:SerializedName("postal_code")
    val postalCode: String,

    @field:SerializedName("order_id")
    val orderId: Int,

    @field:SerializedName("city_id")
    val cityId: Int
)
