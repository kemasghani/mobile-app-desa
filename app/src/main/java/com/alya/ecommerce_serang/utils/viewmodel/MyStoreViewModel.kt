package com.alya.ecommerce_serang.utils.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alya.ecommerce_serang.data.api.dto.Store
import com.alya.ecommerce_serang.data.repository.MyStoreRepository
import com.alya.ecommerce_serang.data.repository.Result
import kotlinx.coroutines.launch

class MyStoreViewModel(private val myStoreRepository: MyStoreRepository): ViewModel() {
    private val _myStoreProfile = MutableLiveData<Store?>()
    val myStoreProfile: LiveData<Store?> = _myStoreProfile

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage : LiveData<String> = _errorMessage

    fun loadMyStore(){
        viewModelScope.launch {
            when (val result = myStoreRepository.fetchMyStoreProfile()){
                is Result.Success -> _myStoreProfile.postValue(result.data)
                is Result.Error -> _errorMessage.postValue(result.exception.message ?: "Unknown Error")
                is Result.Loading -> null
            }
        }
    }
}