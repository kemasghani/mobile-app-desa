package com.alya.ecommerce_serang.data.repository

import android.util.Log
import com.alya.ecommerce_serang.data.api.dto.City
import com.alya.ecommerce_serang.data.api.dto.Province
import com.alya.ecommerce_serang.data.api.dto.StoreAddress
import com.alya.ecommerce_serang.data.api.retrofit.ApiService
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class AddressRepository(private val apiService: ApiService) {

    private val TAG = "AddressRepository"

    suspend fun getProvinces(): List<Province> = withContext(Dispatchers.IO) {
        Log.d(TAG, "getProvinces() called")
        try {
            val response = apiService.getProvinces()
            Log.d(TAG, "getProvinces() response: isSuccessful=${response.isSuccessful}, code=${response.code()}")

            // Log the raw response body for debugging
            val rawBody = response.raw().toString()
            Log.d(TAG, "Raw response: $rawBody")

            if (response.isSuccessful) {
                val responseBody = response.body()
                Log.d(TAG, "Response body: ${Gson().toJson(responseBody)}")

                val provinces = responseBody?.data ?: emptyList()
                Log.d(TAG, "getProvinces() success, got ${provinces.size} provinces")
                return@withContext provinces
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Log.e(TAG, "getProvinces() error: $errorBody")
                throw Exception("API Error (${response.code()}): $errorBody")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in getProvinces()", e)
            throw Exception("Network error: ${e.message}")
        }
    }

    suspend fun getCities(provinceId: String): List<City> = withContext(Dispatchers.IO) {
        Log.d(TAG, "getCities() called with provinceId: $provinceId")
        try {
            val response = apiService.getCities(provinceId)
            Log.d(TAG, "getCities() response: isSuccessful=${response.isSuccessful}, code=${response.code()}")

            if (response.isSuccessful) {
                val responseBody = response.body()
                Log.d(TAG, "Response body: ${Gson().toJson(responseBody)}")

                val cities = responseBody?.data ?: emptyList()
                Log.d(TAG, "getCities() success, got ${cities.size} cities")
                return@withContext cities
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Log.e(TAG, "getCities() error: $errorBody")
                throw Exception("API Error (${response.code()}): $errorBody")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in getCities()", e)
            throw Exception("Network error: ${e.message}")
        }
    }

    suspend fun getStoreAddress(): StoreAddress? = withContext(Dispatchers.IO) {
        Log.d(TAG, "getStoreAddress() called")
        try {
            val response = apiService.getStoreAddress()
            Log.d(TAG, "getStoreAddress() response: isSuccessful=${response.isSuccessful}, code=${response.code()}")

            if (response.isSuccessful) {
                val responseBody = response.body()
                val rawJson = Gson().toJson(responseBody)
                Log.d(TAG, "Response body: $rawJson")

                val address = responseBody?.data
                Log.d(TAG, "getStoreAddress() success, address: $address")

                // Convert numeric strings to proper types if needed
                address?.let {
                    // Handle city_id if it's a number
                    if (it.cityId.isBlank() && rawJson.contains("city_id")) {
                        try {
                            val cityId = JSONObject(rawJson).getJSONObject("store").optInt("city_id", 0)
                            if (cityId > 0) {
                                it.javaClass.getDeclaredField("cityId").apply {
                                    isAccessible = true
                                    set(it, cityId.toString())
                                }
                                Log.d(TAG, "Updated cityId to: ${it.cityId}")
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing city_id", e)
                        }
                    }

                    // Handle province_id if it's a number
                    if (it.provinceId.isBlank() && rawJson.contains("province_id")) {
                        try {
                            val provinceId = JSONObject(rawJson).getJSONObject("store").optInt("province_id", 0)
                            if (provinceId > 0) {
                                it.javaClass.getDeclaredField("provinceId").apply {
                                    isAccessible = true
                                    set(it, provinceId.toString())
                                }
                                Log.d(TAG, "Updated provinceId to: ${it.provinceId}")
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing province_id", e)
                        }
                    }
                }

                return@withContext address
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Log.e(TAG, "getStoreAddress() error: $errorBody")
                throw Exception("API Error (${response.code()}): $errorBody")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in getStoreAddress()", e)
            throw Exception("Network error: ${e.message}")
        }
    }

    suspend fun saveStoreAddress(
        provinceId: String,
        provinceName: String,
        cityId: String,
        cityName: String,
        street: String,
        subdistrict: String,
        detail: String,
        postalCode: String,
        latitude: Double,
        longitude: Double
    ): Boolean = withContext(Dispatchers.IO) {
        Log.d(TAG, "saveStoreAddress() called with provinceId: $provinceId, cityId: $cityId")

        try {
            val addressMap = HashMap<String, Any?>()
            addressMap["provinceId"] = provinceId
            addressMap["provinceName"] = provinceName
            addressMap["cityId"] = cityId
            addressMap["cityName"] = cityName
            addressMap["street"] = street
            addressMap["subdistrict"] = subdistrict
            addressMap["detail"] = detail
            addressMap["postalCode"] = postalCode
            addressMap["latitude"] = latitude
            addressMap["longitude"] = longitude

            Log.d(TAG, "saveStoreAddress() request data: $addressMap")
            val response = apiService.updateStoreAddress(addressMap)
            Log.d(TAG, "saveStoreAddress() response: isSuccessful=${response.isSuccessful}, code=${response.code()}")

            if (response.isSuccessful) {
                Log.d(TAG, "saveStoreAddress() success")
                return@withContext true
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Log.e(TAG, "saveStoreAddress() error: $errorBody")
                throw Exception("API Error (${response.code()}): $errorBody")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in saveStoreAddress()", e)
            throw Exception("Network error: ${e.message}")
        }
    }
}