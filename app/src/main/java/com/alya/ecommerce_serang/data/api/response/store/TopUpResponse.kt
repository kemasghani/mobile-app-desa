package com.alya.ecommerce_serang.data.api.response.store

import android.util.Log
import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

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
            // Try to use transaction_date first, then fall back to created_at
            val dateStr = if (transactionDate.isNotEmpty()) transactionDate else createdAt

            // Try different formats to parse the date
            val parsedDate = parseApiDate(dateStr) ?: return dateStr

            // Format with Indonesian locale for month names
            val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale("id"))
            return outputFormat.format(parsedDate)
        } catch (e: Exception) {
            Log.e("TopUp", "Error formatting date: ${e.message}")
            return createdAt
        }
    }

    private fun parseApiDate(dateStr: String): Date? {
        if (dateStr.isEmpty()) return null

        // List of possible date formats to try
        val formats = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",  // Standard ISO with milliseconds
            "yyyy-MM-dd'T'HH:mm:ss'Z'",      // ISO without milliseconds
            "yyyy-MM-dd'T'HH:mm:ss.SSSZ",    // ISO with timezone offset
            "yyyy-MM-dd'T'HH:mm:ssZ",        // ISO with timezone offset, no milliseconds
            "yyyy-MM-dd",                    // Just the date part
            "dd-MM-yyyy"                     // Alternative date format
        )

        for (format in formats) {
            try {
                val sdf = SimpleDateFormat(format, Locale.US)
                sdf.timeZone = TimeZone.getTimeZone("UTC") // Assuming API dates are in UTC
                return sdf.parse(dateStr)
            } catch (e: Exception) {
                // Try next format
                continue
            }
        }

        // If all formats fail, just try to extract the date part and parse it
        try {
            val datePart = dateStr.split("T").firstOrNull() ?: return null
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            return simpleDateFormat.parse(datePart)
        } catch (e: Exception) {
            Log.e("TopUp", "Failed to parse date: $dateStr", e)
            return null
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