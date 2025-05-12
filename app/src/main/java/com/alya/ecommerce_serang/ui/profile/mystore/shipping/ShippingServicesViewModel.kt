package com.alya.ecommerce_serang.ui.profile.mystore.shipping

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alya.ecommerce_serang.data.repository.ShippingServicesRepository
import kotlinx.coroutines.launch

class ShippingServicesViewModel(private val repository: ShippingServicesRepository) : ViewModel() {

    private val TAG = "ShippingServicesVM"

    private val _availableCouriers = MutableLiveData<List<String>>()
    val availableCouriers: LiveData<List<String>> = _availableCouriers

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _saveSuccess = MutableLiveData<Boolean>()
    val saveSuccess: LiveData<Boolean> = _saveSuccess

    fun getAvailableCouriers() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = repository.getAvailableCouriers()
                _availableCouriers.value = result
                _isLoading.value = false
            } catch (e: Exception) {
                Log.e(TAG, "Error getting available couriers", e)
                _errorMessage.value = "Failed to load shipping services: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun saveShippingServices(selectedCouriers: List<String>) {
        if (selectedCouriers.isEmpty()) {
            _errorMessage.value = "Please select at least one courier"
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            try {
                // First get current couriers to determine what to add/delete
                val currentCouriers = repository.getAvailableCouriers()

                // Calculate couriers to add (selected but not in current)
                val couriersToAdd = selectedCouriers.filter { !currentCouriers.contains(it) }

                // Calculate couriers to delete (in current but not selected)
                val couriersToDelete = currentCouriers.filter { !selectedCouriers.contains(it) }

                // Perform additions if needed
                if (couriersToAdd.isNotEmpty()) {
                    repository.addShippingServices(couriersToAdd)
                }

                // Perform deletions if needed
                if (couriersToDelete.isNotEmpty()) {
                    repository.deleteShippingServices(couriersToDelete)
                }

                _saveSuccess.value = true
                _isLoading.value = false
            } catch (e: Exception) {
                Log.e(TAG, "Error saving shipping services", e)
                _errorMessage.value = "Failed to save shipping services: ${e.message}"
                _isLoading.value = false
                _saveSuccess.value = false
            }
        }
    }
}