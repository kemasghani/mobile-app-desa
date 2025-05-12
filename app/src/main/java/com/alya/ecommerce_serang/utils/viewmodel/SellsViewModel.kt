package com.alya.ecommerce_serang.utils.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.alya.ecommerce_serang.data.api.dto.OrdersItem
import com.alya.ecommerce_serang.data.repository.OrderRepository
import kotlinx.coroutines.launch

class SellsViewModel(private val repository: OrderRepository) : ViewModel() {
    private val _sellsList = MutableLiveData<List<OrdersItem?>>()
    val sellsList: LiveData<List<OrdersItem?>> get() = _sellsList

    fun loadOrdersByStatus(status: String) {
        viewModelScope.launch {
            val result = if (status == "all") {
                repository.fetchSells()
            } else {
                repository.fetchOrdersByStatus(status)
            }
            _sellsList.value = result
        }
    }
}