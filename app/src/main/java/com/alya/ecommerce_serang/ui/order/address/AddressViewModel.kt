package com.alya.ecommerce_serang.ui.order.address

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alya.ecommerce_serang.data.api.response.customer.profile.AddressesItem
import com.alya.ecommerce_serang.data.repository.OrderRepository
import kotlinx.coroutines.launch

class AddressViewModel(private val repository: OrderRepository): ViewModel() {

    private val _addresses = MutableLiveData<List<AddressesItem>>()
    val addresses: LiveData<List<AddressesItem>> get() = _addresses

    private val _selectedAddressId = MutableLiveData<Int?>()
    val selectedAddressId: LiveData<Int?> get() = _selectedAddressId

    fun fetchAddresses() {
        viewModelScope.launch {
            val response = repository.getAddress()
            response?.let {
                _addresses.value = it.addresses
            }
        }
    }

    fun selectAddress(id: Int) {
        _selectedAddressId.value = id
    }
}