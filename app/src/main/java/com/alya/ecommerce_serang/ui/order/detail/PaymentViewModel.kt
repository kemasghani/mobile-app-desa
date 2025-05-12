package com.alya.ecommerce_serang.ui.order.detail

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alya.ecommerce_serang.data.api.dto.AddEvidenceMultipartRequest
import com.alya.ecommerce_serang.data.api.response.order.AddEvidenceResponse
import com.alya.ecommerce_serang.data.api.response.order.CompletedOrderResponse
import com.alya.ecommerce_serang.data.api.response.customer.order.OrderListItemsItem
import com.alya.ecommerce_serang.data.api.response.customer.order.Orders
import com.alya.ecommerce_serang.data.repository.OrderRepository
import com.alya.ecommerce_serang.data.repository.Result
import kotlinx.coroutines.launch

class PaymentViewModel(private val repository: OrderRepository) : ViewModel() {
    companion object {
        private const val TAG = "PaymentViewModel"
    }

    // LiveData untuk Order
    private val _orderDetails = MutableLiveData<Orders>()
    val orderDetails: LiveData<Orders> get() = _orderDetails

    // LiveData untuk OrderItems
    private val _orderItems = MutableLiveData<List<OrderListItemsItem>>()
    val orderItems: LiveData<List<OrderListItemsItem>> get() = _orderItems

    private val _orderCompletionStatus = MutableLiveData<Result<CompletedOrderResponse>>()
    val orderCompletionStatus: LiveData<Result<CompletedOrderResponse>> = _orderCompletionStatus

    // LiveData untuk status loading
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    // LiveData untuk error
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    private val _uploadResult = MutableLiveData<Result<AddEvidenceResponse>>()
    val uploadResult: LiveData<com.alya.ecommerce_serang.data.repository.Result<AddEvidenceResponse>> = _uploadResult

    fun getOrderDetails(orderId: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = repository.getOrderDetails(orderId)
                if (response != null) {
                    _orderDetails.value = response.orders
                    _orderItems.value = response.orders.orderItems
                } else {
                    _error.value = "Gagal memuat detail pesanan"
                }
            } catch (e: Exception) {
                _error.value = "Terjadi kesalahan: ${e.message}"
                Log.e(TAG, "Error fetching order details", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun uploadPaymentProof(request: AddEvidenceMultipartRequest) {
        viewModelScope.launch {
            _uploadResult.value = com.alya.ecommerce_serang.data.repository.Result.Loading
            try {
                val result = repository.uploadPaymentProof(request)
                _uploadResult.value = result
            } catch (e: Exception) {
                Log.e("PaymentProofViewModel", "Error uploading payment proof", e)
                _uploadResult.value = com.alya.ecommerce_serang.data.repository.Result.Error(e)
            }
        }
    }


}