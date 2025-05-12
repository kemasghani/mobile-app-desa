package com.alya.ecommerce_serang.ui.profile.mystore.address

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alya.ecommerce_serang.data.api.dto.City
import com.alya.ecommerce_serang.data.api.dto.Province
import com.alya.ecommerce_serang.data.api.dto.StoreAddress
import com.alya.ecommerce_serang.data.repository.AddressRepository
import kotlinx.coroutines.launch

class AddressViewModel(private val addressRepository: AddressRepository) : ViewModel() {

    private val TAG = "AddressViewModel"

    private val _provinces = MutableLiveData<List<Province>>()
    val provinces: LiveData<List<Province>> = _provinces

    private val _cities = MutableLiveData<List<City>>()
    val cities: LiveData<List<City>> = _cities

    private val _storeAddress = MutableLiveData<StoreAddress?>()
    val storeAddress: LiveData<StoreAddress?> = _storeAddress

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _saveSuccess = MutableLiveData<Boolean>()
    val saveSuccess: LiveData<Boolean> = _saveSuccess

    fun fetchProvinces() {
        Log.d(TAG, "fetchProvinces() called")
        _isLoading.value = true
        viewModelScope.launch {
            try {
                Log.d(TAG, "Calling addressRepository.getProvinces()")
                val response = addressRepository.getProvinces()
                Log.d(TAG, "Received provinces response: ${response.size} provinces")
                _provinces.value = response
                _isLoading.value = false
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching provinces", e)
                _errorMessage.value = "Failed to load provinces: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun fetchCities(provinceId: String) {
        Log.d(TAG, "fetchCities() called with provinceId: $provinceId")
        _isLoading.value = true
        viewModelScope.launch {
            try {
                Log.d(TAG, "Calling addressRepository.getCities()")
                val response = addressRepository.getCities(provinceId)
                Log.d(TAG, "Received cities response: ${response.size} cities")
                _cities.value = response
                _isLoading.value = false
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching cities", e)
                _errorMessage.value = "Failed to load cities: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun fetchStoreAddress() {
        Log.d(TAG, "fetchStoreAddress() called")
        _isLoading.value = true
        viewModelScope.launch {
            try {
                Log.d(TAG, "Calling addressRepository.getStoreAddress()")
                val response = addressRepository.getStoreAddress()
                Log.d(TAG, "Received store address response: $response")
                _storeAddress.value = response
                _isLoading.value = false
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching store address", e)
                _errorMessage.value = "Failed to load store address: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun saveStoreAddress(
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
    ) {
        Log.d(TAG, "saveStoreAddress() called with provinceId: $provinceId, cityId: $cityId")
        _isLoading.value = true
        viewModelScope.launch {
            try {
                Log.d(TAG, "Calling addressRepository.saveStoreAddress()")
                val success = addressRepository.saveStoreAddress(
                    provinceId = provinceId,
                    provinceName = provinceName,
                    cityId = cityId,
                    cityName = cityName,
                    street = street,
                    subdistrict = subdistrict,
                    detail = detail,
                    postalCode = postalCode,
                    latitude = latitude,
                    longitude = longitude
                )
                Log.d(TAG, "Save store address result: $success")
                _saveSuccess.value = success
                _isLoading.value = false
            } catch (e: Exception) {
                Log.e(TAG, "Error saving store address", e)
                _errorMessage.value = "Failed to save address: ${e.message}"
                _isLoading.value = false
            }
        }
    }
}