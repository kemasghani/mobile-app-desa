package com.alya.ecommerce_serang.ui.order

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alya.ecommerce_serang.data.api.dto.CostProduct
import com.alya.ecommerce_serang.data.api.dto.CourierCostRequest
import com.alya.ecommerce_serang.data.api.response.customer.order.CourierCostsItem
import com.alya.ecommerce_serang.data.repository.OrderRepository
import com.alya.ecommerce_serang.data.repository.Result
import kotlinx.coroutines.launch

class ShippingViewModel(
    private val repository: OrderRepository
) : ViewModel() {

    // Shipping options LiveData
    private val _shippingOptions = MutableLiveData<List<CourierCostsItem>>()
    val shippingOptions: LiveData<List<CourierCostsItem>> = _shippingOptions

    // Loading state LiveData
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // Error message LiveData
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    /**
     * Load shipping options based on address, product, and quantity
     */
    fun loadShippingOptions(addressId: Int, productId: Int, quantity: Int) {
        // Reset previous state
        _isLoading.value = true
        _errorMessage.value = ""

        // Prepare the request
        val costProduct = CostProduct(
            productId = productId,
            quantity = quantity
        )

        val request = CourierCostRequest(
            addressId = addressId,
            itemCost = listOf(costProduct)  // Wrap in a list
        )

        viewModelScope.launch {
            try {
                // Fetch courier costs
                val result = repository.getCountCourierCost(request)

                when (result) {
                    is Result.Success -> {
                        // Update shipping options directly with courier costs
                        _shippingOptions.value = result.data.courierCosts
                    }
                    is Result.Error -> {
                        // Handle error case
                        _errorMessage.value = result.exception.message ?: "Unknown error occurred"
                    }
                    is Result.Loading -> {
                        // Typically handled by the loading state
                    }
                }
            } catch (e: Exception) {
                // Catch any unexpected exceptions
                _errorMessage.value = e.localizedMessage ?: "An unexpected error occurred"
            } finally {
                // Always set loading to false
                _isLoading.value = false
            }
        }
    }
}