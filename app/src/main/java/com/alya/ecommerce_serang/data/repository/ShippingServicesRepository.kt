package com.alya.ecommerce_serang.data.repository

import android.util.Log
import com.alya.ecommerce_serang.data.api.dto.ShippingServiceRequest
import com.alya.ecommerce_serang.data.api.retrofit.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ShippingServicesRepository(private val apiService: ApiService) {

    private val TAG = "ShippingServicesRepo"

    suspend fun getAvailableCouriers(): List<String> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Getting available shipping services")
            val response = apiService.getStoreData()

            if (response.isSuccessful) {
                val result = response.body()
                val shippingList = result?.shipping

                val couriers = if (shippingList != null) {
                    shippingList.map { it.courier }
                } else {
                    emptyList()
                }

                Log.d(TAG, "Get shipping services success: ${couriers.size} couriers")
                return@withContext couriers
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Log.e(TAG, "Get shipping services error: $errorBody")
                throw Exception("Failed to get shipping services: ${response.message()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception getting shipping services", e)
            throw e
        }
    }

    suspend fun addShippingServices(couriers: List<String>): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Adding shipping services: $couriers")

            val request = ShippingServiceRequest(couriers = couriers)
            val response = apiService.addShippingService(request)

            if (response.isSuccessful) {
                Log.d(TAG, "Add shipping services success: ${response.body()?.message}")
                return@withContext true
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Log.e(TAG, "Add shipping services error: $errorBody")
                throw Exception("Failed to add shipping services: ${response.message()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception adding shipping services", e)
            throw e
        }
    }

    suspend fun deleteShippingServices(couriers: List<String>): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Deleting shipping services: $couriers")

            val request = ShippingServiceRequest(couriers = couriers)
            val response = apiService.deleteShippingService(request)

            if (response.isSuccessful) {
                Log.d(TAG, "Delete shipping services success: ${response.body()?.message}")
                return@withContext true
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Log.e(TAG, "Delete shipping services error: $errorBody")
                throw Exception("Failed to delete shipping services: ${response.message()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception deleting shipping services", e)
            throw e
        }
    }
}