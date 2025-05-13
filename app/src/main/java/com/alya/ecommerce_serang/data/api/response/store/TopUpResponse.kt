package com.alya.ecommerce_serang.data.api.response.store

import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class TopUpResponse(
    val message: String,
    val topup: List<TopUp>
)

data class TopUp(
    val id: Int,
    val amount: String,
    @SerializedName("store_id") val storeId: Int,
    val status: String,
    @SerializedName("created_at") val createdAt: String,
    val image: String,
    @SerializedName("payment_info_id") val paymentInfoId: Int,
    @SerializedName("transaction_date") val transactionDate: String,
    @SerializedName("payment_method") val paymentMethod: String,
    @SerializedName("account_name") val accountName: String?
) {
    fun getFormattedDate(): String {
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale("id"))
            val date = inputFormat.parse(createdAt) ?: Date()
            return outputFormat.format(date)
        } catch (e: Exception) {
            return createdAt
        }
    }

    fun getFormattedAmount(): String {
        return try {
            val amountValue = amount.toDouble()
            String.format("+ Rp%,.0f", amountValue)
        } catch (e: Exception) {
            "Rp$amount"
        }
    }
}