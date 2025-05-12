package com.alya.ecommerce_serang.ui.order

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alya.ecommerce_serang.data.api.dto.CheckoutData
import com.alya.ecommerce_serang.data.api.dto.OrderRequest
import com.alya.ecommerce_serang.data.api.dto.OrderRequestBuy
import com.alya.ecommerce_serang.data.api.response.customer.cart.CartItemsItem
import com.alya.ecommerce_serang.data.api.response.customer.cart.DataItem
import com.alya.ecommerce_serang.data.api.response.customer.product.PaymentInfoItem
import com.alya.ecommerce_serang.data.api.response.customer.profile.AddressesItem
import com.alya.ecommerce_serang.data.repository.OrderRepository
import com.alya.ecommerce_serang.data.repository.Result
import kotlinx.coroutines.launch

class CheckoutViewModel(private val repository: OrderRepository) : ViewModel() {

    private val _checkoutData = MutableLiveData<CheckoutData>()
    val checkoutData: LiveData<CheckoutData> = _checkoutData

    private val _addressDetails = MutableLiveData<AddressesItem?>()
    val addressDetails: LiveData<AddressesItem?> = _addressDetails

    private val _availablePaymentMethods = MutableLiveData<List<PaymentInfoItem>>()
    val availablePaymentMethods: LiveData<List<PaymentInfoItem>> = _availablePaymentMethods

    // Selected payment method
    private val _selectedPayment = MutableLiveData<PaymentInfoItem?>()
    val selectedPayment: LiveData<PaymentInfoItem?> = _selectedPayment

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _orderCreated = MutableLiveData<Boolean>()
    val orderCreated: LiveData<Boolean> = _orderCreated

    // Initialize "Buy Now" checkout
    fun initializeBuyNow(
        storeId: Int,
        storeName: String?,
        productId: Int,
        productName: String?,
        productImage: String?,
        quantity: Int,
        price: Double
    ) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                // Create initial OrderRequestBuy object
                val orderRequest = OrderRequestBuy(
                    addressId = 0, // Will be set when user selects address
                    paymentMethodId = 0, // Will be set when user selects payment
                    shipPrice = 0, // Will be set when user selects shipping
                    shipName = "",
                    shipService = "",
                    isNego = false, // Default value
                    productId = productId,
                    quantity = quantity,
                    shipEtd = ""
                )

                // Create checkout data
                _checkoutData.value = CheckoutData(
                    orderRequest = orderRequest,
                    productName = productName,
                    productImageUrl = productImage ?: "",
                    productPrice = price,
                    sellerName = storeName ?: "",
                    sellerId = storeId,
                    quantity = quantity,
                    isBuyNow = true
                )

            } catch (e: Exception) {
                Log.e(TAG, "Error initializing Buy Now data", e)
                _errorMessage.value = "Failed to initialize checkout: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Initialize checkout from cart
    fun initializeFromCart(cartItemIds: List<Int>) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                // Get cart data
                val cartResult = repository.getCart()

                if (cartResult is Result.Success) {
                    // Find matching cart items
                    val matchingItems = mutableListOf<CartItemsItem>()
                    var storeData: DataItem? = null

                    for (store in cartResult.data) {
                        val storeItems = store.cartItems.filter { it.cartItemId in cartItemIds }
                        if (storeItems.isNotEmpty()) {
                            matchingItems.addAll(storeItems)
                            storeData = store
                            break
                        }
                    }

                    if (matchingItems.isNotEmpty() && storeData != null) {
                        // Create initial OrderRequest object
                        val orderRequest = OrderRequest(
                            addressId = 0, // Will be set when user selects address
                            paymentMethodId = 0, // Will be set when user selects payment
                            shipPrice = 0, // Will be set when user selects shipping
                            shipName = "",
                            shipService = "",
                            isNego = false,
                            cartItemId = cartItemIds,
                            shipEtd = ""
                        )

                        // Create checkout data
                        _checkoutData.value = CheckoutData(
                            orderRequest = orderRequest,
                            productName = matchingItems.first().productName,
                            sellerName = storeData.storeName,
                            sellerId = storeData.storeId,
                            isBuyNow = false,
                            cartItems = matchingItems
                        )
                    } else {
                        _errorMessage.value = "No matching cart items found"
                    }
                } else if (cartResult is Result.Error) {
                    _errorMessage.value = "Failed to fetch cart items: ${cartResult.exception.message}"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing cart checkout", e)
                _errorMessage.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getPaymentMethods(callback: (List<PaymentInfoItem>) -> Unit) {
        viewModelScope.launch {
            try {
                val storeId = _checkoutData.value?.sellerId ?: return@launch

                // Use fetchStoreDetail instead of getStore
                val storeResult = repository.fetchStoreDetail(storeId)

                if (storeResult is Result.Success && storeResult.data != null) {
                    // For now, we'll use hardcoded payment ID (1) for all payment methods
                    // This will be updated once the backend provides proper IDs
                    val paymentMethodsList = storeResult.data.paymentInfo.map { paymentInfo ->
                        PaymentInfoItem(
                            id = 1, // Hardcoded payment ID
                            name = paymentInfo.name,
                            bankNum = paymentInfo.bankNum,
                            qrisImage = paymentInfo.qrisImage
                        )
                    }

                    Log.d(TAG, "Fetched ${paymentMethodsList.size} payment methods")

                    _availablePaymentMethods.value = paymentMethodsList
                    callback(paymentMethodsList)
                } else {
                    _availablePaymentMethods.value = emptyList()
                    callback(emptyList())
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching payment methods", e)
                _availablePaymentMethods.value = emptyList()
                callback(emptyList())
            }
        }
    }

    // Updated setPaymentMethod function
    fun setPaymentMethod(paymentId: Int) {
        // We'll use the hardcoded ID (1) for now
        val currentPaymentId = 1

        viewModelScope.launch {
            try {
                // Get the available payment methods
                val paymentMethods = _availablePaymentMethods.value

                if (paymentMethods.isNullOrEmpty()) {
                    // If no payment methods available, try to fetch them
                    getPaymentMethods { /* do nothing here */ }
                    return@launch
                }

                // Use the first payment method (or specific one if you prefer)
                val selectedPayment = paymentMethods.first()

                // Set the selected payment
                _selectedPayment.value = selectedPayment
                Log.d(TAG, "Payment selected: Name=${selectedPayment.name}")

                // Update the order request with the payment method ID (hardcoded for now)
                val currentData = _checkoutData.value ?: return@launch

                // Different handling for Buy Now vs Cart checkout
                if (currentData.isBuyNow) {
                    // For Buy Now checkout
                    val buyRequest = currentData.orderRequest as OrderRequestBuy
                    val updatedRequest = buyRequest.copy(paymentMethodId = currentPaymentId)
                    _checkoutData.value = currentData.copy(orderRequest = updatedRequest)
                } else {
                    // For Cart checkout
                    val cartRequest = currentData.orderRequest as OrderRequest
                    val updatedRequest = cartRequest.copy(paymentMethodId = currentPaymentId)
                    _checkoutData.value = currentData.copy(orderRequest = updatedRequest)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error setting payment method: ${e.message}"
                Log.e(TAG, "Error setting payment method", e)
            }
        }
    }

    // Set selected address
    fun setSelectedAddress(addressId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Get address details from API
                val addressResponse = repository.getAddress()
                if (addressResponse != null && !addressResponse.addresses.isNullOrEmpty()) {
                    val address = addressResponse.addresses.find { it.id == addressId }
                    if (addressResponse != null && !addressResponse.addresses.isNullOrEmpty()) {
                        val address = addressResponse.addresses.find { it.id == addressId }
                        // No need for null check since _addressDetails now accepts nullable values
                        _addressDetails.value = address

                        // Update order request with address ID only if address isn't null
                        if (address != null) {
                            val currentData = _checkoutData.value ?: return@launch
                            if (currentData.isBuyNow) {
                                val buyRequest = currentData.orderRequest as OrderRequestBuy
                                val updatedRequest = buyRequest.copy(addressId = addressId)
                                _checkoutData.value = currentData.copy(orderRequest = updatedRequest)
                            } else {
                                val cartRequest = currentData.orderRequest as OrderRequest
                                val updatedRequest = cartRequest.copy(addressId = addressId)
                                _checkoutData.value = currentData.copy(orderRequest = updatedRequest)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error loading address: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Set shipping method
    fun setShippingMethod(shipName: String, shipService: String, shipPrice: Int, shipEtd: String) {
        val currentData = _checkoutData.value ?: return

        if (currentData.isBuyNow) {
            val buyRequest = currentData.orderRequest as OrderRequestBuy
            val updatedRequest = buyRequest.copy(
                shipName = shipName,
                shipService = shipService,
                shipPrice = shipPrice,
                shipEtd = shipEtd
            )
            _checkoutData.value = currentData.copy(orderRequest = updatedRequest)
        } else {
            val cartRequest = currentData.orderRequest as OrderRequest
            val updatedRequest = cartRequest.copy(
                shipName = shipName,
                shipService = shipService,
                shipPrice = shipPrice,
                shipEtd = shipEtd
            )
            _checkoutData.value = currentData.copy(orderRequest = updatedRequest)
        }
    }

    // Create order
    fun createOrder() {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                val data = _checkoutData.value ?: throw Exception("No checkout data available")

                val response = if (data.isBuyNow) {
                    // For Buy Now, use the dedicated endpoint
                    val buyRequest = data.orderRequest as OrderRequestBuy
                    repository.createOrderBuyNow(buyRequest)
                } else {
                    // For Cart checkout, use the standard order endpoint
                    val cartRequest = data.orderRequest as OrderRequest
                    repository.createOrder(cartRequest)
                }

                if (response.isSuccessful) {
                    _orderCreated.value = true
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                    _errorMessage.value = "Failed to create order: $errorMsg"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error creating order: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Calculate total price (subtotal + shipping)
    fun calculateTotal(): Double {
        val data = _checkoutData.value ?: return 0.0

        return calculateSubtotal() + getShippingPrice()
    }

    // Calculate subtotal (without shipping)
    fun calculateSubtotal(): Double {
        val data = _checkoutData.value ?: return 0.0

        return if (data.isBuyNow) {
            // For Buy Now, use product price * quantity
            val buyRequest = data.orderRequest as OrderRequestBuy
            data.productPrice * buyRequest.quantity
        } else {
            // For Cart, sum all items
            data.cartItems.sumOf { it.price * it.quantity.toDouble() }
        }
    }

    // Get shipping price
    private fun getShippingPrice(): Double {
        val data = _checkoutData.value ?: return 0.0

        return if (data.isBuyNow) {
            (data.orderRequest as OrderRequestBuy).shipPrice.toDouble()
        } else {
            (data.orderRequest as OrderRequest).shipPrice.toDouble()
        }
    }

    companion object {
        private const val TAG = "CheckoutViewModel"
    }
}