package com.alya.ecommerce_serang.ui.order.address

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alya.ecommerce_serang.data.api.dto.CreateAddressRequest
import com.alya.ecommerce_serang.data.api.dto.UserProfile
import com.alya.ecommerce_serang.data.api.response.customer.order.CitiesItem
import com.alya.ecommerce_serang.data.api.response.customer.order.ProvincesItem
import com.alya.ecommerce_serang.data.repository.OrderRepository
import com.alya.ecommerce_serang.data.repository.Result
import com.alya.ecommerce_serang.data.repository.UserRepository
import kotlinx.coroutines.launch

class AddAddressViewModel(private val repository: OrderRepository, private val userRepo: UserRepository, private val savedStateHandle: SavedStateHandle): ViewModel() {
    private val _addressSubmissionState = MutableLiveData<ViewState<String>>()
    val addressSubmissionState: LiveData<ViewState<String>> = _addressSubmissionState

    private val _userProfile = MutableLiveData<UserProfile?>()
    val userProfile: LiveData<UserProfile?> = _userProfile

    private val _errorMessageUser = MutableLiveData<String>()
    val errorMessageUser : LiveData<String> = _errorMessageUser

    private val _provincesState = MutableLiveData<ViewState<List<ProvincesItem>>>()
    val provincesState: LiveData<ViewState<List<ProvincesItem>>> = _provincesState

    private val _citiesState = MutableLiveData<ViewState<List<CitiesItem>>>()
    val citiesState: LiveData<ViewState<List<CitiesItem>>> = _citiesState

    // Stored in SavedStateHandle for configuration changes
    var selectedProvinceId: Int?
        get() = savedStateHandle.get<Int>("selectedProvinceId")
        set(value) { savedStateHandle["selectedProvinceId"] = value }

    var selectedCityId: Int?
        get() = savedStateHandle.get<Int>("selectedCityId")
        set(value) { savedStateHandle["selectedCityId"] = value }

    init {
        // Load provinces on initialization
        getProvinces()
    }

    fun addAddress(request: CreateAddressRequest) {
        Log.d(TAG, "Starting address submission process")
        _addressSubmissionState.value = ViewState.Loading
        viewModelScope.launch {
            try {
                Log.d(TAG, "Calling repository.addAddress with request: $request")
                val result = repository.addAddress(request)

                when (result) {
                    is Result.Success -> {
                        val message = result.data.message
                        Log.d(TAG, "Address added successfully: $message")
                        _addressSubmissionState.postValue(ViewState.Success(message))
                    }
                    is Result.Error -> {
                        val errorMsg = result.exception.message ?: "Unknown error"
                        Log.e(TAG, "Error from repository: $errorMsg", result.exception)
                        _addressSubmissionState.postValue(ViewState.Error(errorMsg))
                    }
                    is Result.Loading -> {
                        Log.d(TAG, "Repository returned Loading state")
                        // We already set Loading at the beginning
                    }
                    else -> {
                        Log.e(TAG, "Repository returned unexpected result type: $result")
                        _addressSubmissionState.postValue(ViewState.Error("Unexpected error occurred"))
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception occurred during address submission", e)
                val errorMessage = e.message ?: "Unknown error occurred"
                Log.e(TAG, "Error message: $errorMessage")

                // Log the exception stack trace
                e.printStackTrace()

                _addressSubmissionState.postValue(ViewState.Error(errorMessage))
            }
        }
    }

    fun getProvinces() {
        _provincesState.value = ViewState.Loading
        viewModelScope.launch {
            try {
                val result = repository.getListProvinces()
                if (result?.provinces != null) {
                    _provincesState.postValue(ViewState.Success(result.provinces))
                    Log.d(TAG, "Provinces loaded: ${result.provinces.size}")
                } else {
                    _provincesState.postValue(ViewState.Error("Failed to load provinces"))
                    Log.e(TAG, "Province result was null or empty")
                }
            } catch (e: Exception) {
                _provincesState.postValue(ViewState.Error(e.message ?: "Error loading provinces"))
                Log.e(TAG, "Error fetching provinces", e)
            }
        }
    }

    fun getCities(provinceId: Int){
        _citiesState.value = ViewState.Loading
        viewModelScope.launch {
            try {
                selectedProvinceId = provinceId
                val result = repository.getListCities(provinceId)
                result?.let {
                    _citiesState.postValue(ViewState.Success(it.cities))
                    Log.d(TAG, "Cities loaded for province $provinceId: ${it.cities.size}")
                } ?: run {
                    _citiesState.postValue(ViewState.Error("Failed to load cities"))
                    Log.e(TAG, "City result was null for province $provinceId")
                }
            } catch (e: Exception) {
                _citiesState.postValue(ViewState.Error(e.message ?: "Error loading cities"))
                Log.e(TAG, "Error fetching cities for province $provinceId", e)
            }
        }
    }

    fun setSelectedProvinceId(id: Int) {
        selectedProvinceId = id
    }

    fun setSelectedCityId(id: Int) {
        selectedCityId = id
    }

    fun loadUserProfile(){
        viewModelScope.launch {
            when (val result = userRepo.fetchUserProfile()){
                is Result.Success -> _userProfile.postValue(result.data)
                is Result.Error -> _errorMessageUser.postValue(result.exception.message ?: "Unknown Error")
                is Result.Loading -> null
            }
        }
    }

    companion object {
        private const val TAG = "AddAddressViewModel"
    }
}

sealed class ViewState<out T> {
    object Loading : ViewState<Nothing>()
    data class Success<T>(val data: T) : ViewState<T>()
    data class Error(val message: String) : ViewState<Nothing>()
}