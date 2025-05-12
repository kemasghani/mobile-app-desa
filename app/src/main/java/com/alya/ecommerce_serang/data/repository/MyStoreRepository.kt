package com.alya.ecommerce_serang.data.repository

import android.util.Log
import com.alya.ecommerce_serang.data.api.dto.Store
import com.alya.ecommerce_serang.data.api.response.customer.product.StoreResponse
import com.alya.ecommerce_serang.data.api.retrofit.ApiService
import retrofit2.HttpException
import java.io.IOException

class MyStoreRepository(private val apiService: ApiService) {
    suspend fun fetchMyStoreProfile(): Result<Store?> {
        return try {
            val response = apiService.getStore()

            if (response.isSuccessful) {
                val storeResponse: StoreResponse? = response.body()
                Result.Success(storeResponse?.store)  // ✅ Return Success with Store data
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Unknown API error"
                Log.e("MyStoreRepository", "Error: $errorMessage")
                Result.Error(HttpException(response)) // ✅ Wrap API error in Result.Error
            }
        } catch (e: IOException) {
            Log.e("MyStoreRepository", "Network error: ${e.message}")
            Result.Error(e)  // ✅ Handle network-related errors
        } catch (e: Exception) {
            Log.e("MyStoreRepository", "Unexpected error: ${e.message}")
            Result.Error(e)  // ✅ Handle unexpected errors
        }
    }
}