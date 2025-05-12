package com.alya.ecommerce_serang.ui.profile.mystore.payment

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alya.ecommerce_serang.data.api.dto.PaymentMethod
import com.alya.ecommerce_serang.data.repository.PaymentMethodRepository
import kotlinx.coroutines.launch
import java.io.File

class PaymentMethodViewModel(private val repository: PaymentMethodRepository) : ViewModel() {

    private val TAG = "PaymentMethodViewModel"

    private val _paymentMethods = MutableLiveData<List<PaymentMethod>>()
    val paymentMethods: LiveData<List<PaymentMethod>> = _paymentMethods

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _addPaymentSuccess = MutableLiveData<Boolean>()
    val addPaymentSuccess: LiveData<Boolean> = _addPaymentSuccess

    private val _deletePaymentSuccess = MutableLiveData<Boolean>()
    val deletePaymentSuccess: LiveData<Boolean> = _deletePaymentSuccess

    fun getPaymentMethods() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                Log.d(TAG, "Loading payment methods...")
                val result = repository.getPaymentMethods()

                if (result.isEmpty()) {
                    Log.d(TAG, "No payment methods found")
                } else {
                    Log.d(TAG, "Successfully loaded ${result.size} payment methods")
                    for (method in result) {
                        Log.d(TAG, "Payment method: id=${method.id}, bank=${method.bankName}, account=${method.accountName}")
                    }
                }

                _paymentMethods.value = result
                _isLoading.value = false
            } catch (e: Exception) {
                Log.e(TAG, "Error getting payment methods", e)
                _errorMessage.value = "Gagal memuat metode pembayaran: ${e.message?.take(100) ?: "Unknown error"}"
                _isLoading.value = false
                // Still set empty payment methods to show empty state
                _paymentMethods.value = emptyList()
            }
        }
    }

    fun addPaymentMethod(bankName: String, bankNumber: String, accountName: String, qrisImageUri: Uri?, qrisImageFile: File?) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                Log.d(TAG, "Adding payment method: bankName=$bankName, bankNumber=$bankNumber, accountName=$accountName")
                Log.d(TAG, "Image file: ${qrisImageFile?.absolutePath}, exists: ${qrisImageFile?.exists()}, size: ${qrisImageFile?.length() ?: 0} bytes")

                // Validate the file if it was provided
                if (qrisImageUri != null && qrisImageFile == null) {
                    _errorMessage.value = "Gagal memproses gambar. Silakan pilih gambar lain."
                    _isLoading.value = false
                    _addPaymentSuccess.value = false
                    return@launch
                }

                // If we have a file, make sure it exists and has some content
                if (qrisImageFile != null && (!qrisImageFile.exists() || qrisImageFile.length() == 0L)) {
                    Log.e(TAG, "Image file does not exist or is empty: ${qrisImageFile.absolutePath}")
                    _errorMessage.value = "File gambar tidak valid. Silakan pilih gambar lain."
                    _isLoading.value = false
                    _addPaymentSuccess.value = false
                    return@launch
                }

                val success = repository.addPaymentMethod(bankName, bankNumber, accountName, qrisImageFile)
                _addPaymentSuccess.value = success
                _isLoading.value = false

                if (success) {
                    // Refresh the payment methods list
                    getPaymentMethods()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error adding payment method", e)
                _errorMessage.value = "Gagal menambahkan metode pembayaran: ${e.message?.take(100) ?: "Unknown error"}"
                _isLoading.value = false
                _addPaymentSuccess.value = false
            }
        }
    }

    fun deletePaymentMethod(paymentMethodId: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val success = repository.deletePaymentMethod(paymentMethodId)
                _deletePaymentSuccess.value = success
                _isLoading.value = false
                if (success) {
                    // Refresh the payment methods list
                    getPaymentMethods()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting payment method", e)
                _errorMessage.value = "Gagal menghapus metode pembayaran: ${e.message?.take(100) ?: "Unknown error"}"
                _isLoading.value = false
                _deletePaymentSuccess.value = false
            }
        }
    }
}