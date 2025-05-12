package com.alya.ecommerce_serang.utils.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alya.ecommerce_serang.data.api.dto.RegisterRequest
import com.alya.ecommerce_serang.data.api.response.auth.OtpResponse
import com.alya.ecommerce_serang.data.repository.Result
import com.alya.ecommerce_serang.data.repository.UserRepository
import kotlinx.coroutines.launch

class RegisterViewModel(private val repository: UserRepository) : ViewModel() {

    // MutableLiveData for handling register state (Loading, Success, or Error)
    private val _registerState = MutableLiveData<Result<String>>()
    val registerState: LiveData<Result<String>> = _registerState

    // MutableLiveData for handling OTP request state
    private val _otpState = MutableLiveData<Result<Unit>>()
    val otpState: LiveData<Result<Unit>> = _otpState

    // MutableLiveData to store messages from API responses
    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    /**
     * Function to request OTP by sending an email to the API.
     * - It sets the OTP state to `Loading` before calling the repository.
     * - If successful, it updates `_message` with the response message and signals success.
     * - If an error occurs, it updates `_otpState` with `Result.Error` and logs the failure.
     */
    fun requestOtp(email: String) {
        viewModelScope.launch {
            _otpState.value = Result.Loading // Indicating API call in progress

            try {
                // Call the repository function to request OTP
                val response: OtpResponse = repository.requestOtpRep(email)

                // Log and store success message
                Log.d("RegisterViewModel", "OTP Response: ${response.message}")
                _message.value = response.message // Store the message for UI feedback

                // Update state to indicate success
                _otpState.value = Result.Success(Unit)

            } catch (exception: Exception) {
                // Handle any errors and update state
                _otpState.value = Result.Error(exception)
                _message.value = exception.localizedMessage ?: "Failed to request OTP"

                // Log the error for debugging
                Log.e("RegisterViewModel", "OTP request failed for: $email", exception)
            }
        }
    }

    /**
     * Function to register a new user.
     * - It first sets `_registerState` to `Loading` to indicate the process is starting.
     * - Calls the repository function to handle user registration.
     * - If successful, it updates `_message` and signals success with the response message.
     * - If an error occurs, it updates `_registerState` with `Result.Error` and logs the failure.
     */
    fun registerUser(request: RegisterRequest) {
        viewModelScope.launch {
            _registerState.value = Result.Loading // Indicating API call in progress

            try {
                // Call repository function to register the user
                val message = repository.registerUser(request)

                // Store and display success message
                _message.value = message
                _registerState.value = Result.Success(message) // Store success result

            } catch (exception: Exception) {
                // Handle any errors and update state
                _registerState.value = Result.Error(exception)
                _message.value = exception.localizedMessage ?: "Registration failed"

                // Log the error for debugging
                Log.e("RegisterViewModel", "User registration failed", exception)
            }
        }
    }
}