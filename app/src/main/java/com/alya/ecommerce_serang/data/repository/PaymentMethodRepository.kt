package com.alya.ecommerce_serang.data.repository

import android.util.Log
import com.alya.ecommerce_serang.data.api.dto.PaymentMethod
import com.alya.ecommerce_serang.data.api.dto.PaymentMethodResponse
import com.alya.ecommerce_serang.data.api.response.store.product.Payment
import com.alya.ecommerce_serang.data.api.response.store.product.StoreDataResponse
import com.alya.ecommerce_serang.data.api.retrofit.ApiService
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import okhttp3.OkHttpClient
import okhttp3.Request

class PaymentMethodRepository(private val apiService: ApiService) {

    private val TAG = "PaymentMethodRepository"
    private val gson = Gson()

    suspend fun getPaymentMethods(): List<PaymentMethod> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Getting payment methods")
            val response = apiService.getStoreData()

            if (response.isSuccessful) {
                val result = response.body()

                // Log the raw response
                Log.d(TAG, "API Response body: ${gson.toJson(result)}")

                // Check if payment list is null or empty
                val paymentList = result?.payment
                if (paymentList.isNullOrEmpty()) {
                    Log.d(TAG, "Payment list is null or empty in response")
                    return@withContext emptyList<PaymentMethod>()
                }

                Log.d(TAG, "Raw payment list: ${gson.toJson(paymentList)}")
                Log.d(TAG, "Get payment methods success: ${paymentList.size} methods")

                // Convert Payment objects to PaymentMethod objects
                val convertedPayments = paymentList.map { payment ->
                    PaymentMethod(
                        id = payment.id,
                        bankNum = payment.bankNum,
                        bankName = payment.bankName,
                        qrisImage = payment.qrisImage,
                        accountName = payment.accountName
                    )
                }

                return@withContext convertedPayments
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Log.e(TAG, "Get payment methods error: $errorBody, HTTP code: ${response.code()}")
                throw Exception("Failed to get payment methods: ${response.message()}, code: ${response.code()}, error: $errorBody")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception getting payment methods", e)
            throw e
        }
    }

    suspend fun addPaymentMethod(
        bankName: String,
        bankNumber: String,
        accountName: String,
        qrisImageFile: File?
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "===== START PAYMENT METHOD ADD =====")
            Log.d(TAG, "Adding payment method with parameters:")
            Log.d(TAG, "Bank Name: $bankName")
            Log.d(TAG, "Bank Number: $bankNumber")
            Log.d(TAG, "Account Name: $accountName")
            Log.d(TAG, "QRIS Image File: ${qrisImageFile?.absolutePath}")
            Log.d(TAG, "QRIS File exists: ${qrisImageFile?.exists()}")
            Log.d(TAG, "QRIS File size: ${qrisImageFile?.length() ?: 0} bytes")

            // Create text RequestBody objects with explicit content type
            val contentType = "text/plain".toMediaTypeOrNull()
            val bankNamePart = bankName.toRequestBody(contentType)
            val bankNumPart = bankNumber.toRequestBody(contentType)
            val accountNamePart = accountName.toRequestBody(contentType)

            // Log request parameters details
            Log.d(TAG, "Request parameters details:")
            Log.d(TAG, "bank_name RequestBody created with value: $bankName")
            Log.d(TAG, "bank_num RequestBody created with value: $bankNumber")
            Log.d(TAG, "account_name RequestBody created with value: $accountName")

            // Create image part if file exists
            var qrisPart: MultipartBody.Part? = null
            if (qrisImageFile != null && qrisImageFile.exists() && qrisImageFile.length() > 0) {
                // Use image/* content type to ensure proper MIME type for images
                val imageContentType = "image/jpeg".toMediaTypeOrNull()
                val requestFile = qrisImageFile.asRequestBody(imageContentType)
                qrisPart = MultipartBody.Part.createFormData("qris", qrisImageFile.name, requestFile)
                Log.d(TAG, "qris MultipartBody.Part created with filename: ${qrisImageFile.name}")
                Log.d(TAG, "qris file size: ${qrisImageFile.length()} bytes")
            } else {
                Log.d(TAG, "No qris image part will be included in the request")
            }

            // Example input data being sent to API
            Log.d(TAG, "Example input data sent to API endpoint http://192.168.100.31:3000/mystore/payment/add:")
            Log.d(TAG, "Method: POST")
            Log.d(TAG, "Content-Type: multipart/form-data")
            Log.d(TAG, "Form fields:")
            Log.d(TAG, "- bank_name: $bankName")
            Log.d(TAG, "- bank_num: $bankNumber")
            Log.d(TAG, "- account_name: $accountName")
            if (qrisPart != null) {
                Log.d(TAG, "- qris: [binary image file: ${qrisImageFile?.name}, size: ${qrisImageFile?.length()} bytes]")
            }

            try {
                // Use the direct API method call
                val response = apiService.addPaymentMethodDirect(
                    bankName = bankNamePart,
                    bankNum = bankNumPart,
                    accountName = accountNamePart,
                    qris = qrisPart
                )

                if (response.isSuccessful) {
                    val result = response.body()
                    Log.d(TAG, "API response: ${gson.toJson(result)}")
                    Log.d(TAG, "Add payment method success")
                    Log.d(TAG, "===== END PAYMENT METHOD ADD - SUCCESS =====")
                    return@withContext true
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    Log.e(TAG, "Add payment method error: $errorBody, HTTP code: ${response.code()}")
                    Log.e(TAG, "===== END PAYMENT METHOD ADD - FAILURE =====")
                    throw Exception("Failed to add payment method: ${response.message()}, code: ${response.code()}, error: $errorBody")
                }
            } catch (e: Exception) {
                Log.e(TAG, "API call exception", e)
                throw e
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception adding payment method", e)
            Log.e(TAG, "===== END PAYMENT METHOD ADD - EXCEPTION =====")
            throw e
        }
    }

    suspend fun deletePaymentMethod(paymentMethodId: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Deleting payment method with ID: $paymentMethodId")

            val response = apiService.deletePaymentMethod(paymentMethodId)

            if (response.isSuccessful) {
                Log.d(TAG, "Delete payment method success: ${response.body()?.message}")
                return@withContext true
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Log.e(TAG, "Delete payment method error: $errorBody, HTTP code: ${response.code()}")
                throw Exception("Failed to delete payment method: ${response.message()}, code: ${response.code()}, error: $errorBody")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception deleting payment method", e)
            throw e
        }
    }
}