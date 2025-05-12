package com.alya.ecommerce_serang.utils.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alya.ecommerce_serang.data.api.dto.UserProfile
import com.alya.ecommerce_serang.data.repository.Result
import com.alya.ecommerce_serang.data.repository.UserRepository
import kotlinx.coroutines.launch

class ProfileViewModel(private val userRepository: UserRepository) : ViewModel() {
    private val _userProfile = MutableLiveData<UserProfile?>()
    val userProfile: LiveData<UserProfile?> = _userProfile

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage : LiveData<String> = _errorMessage

    fun loadUserProfile(){
        viewModelScope.launch {
            when (val result = userRepository.fetchUserProfile()){
                is Result.Success -> _userProfile.postValue(result.data)
                is Result.Error -> _errorMessage.postValue(result.exception.message ?: "Unknown Error")
                is Result.Loading -> null
            }
        }
    }
}