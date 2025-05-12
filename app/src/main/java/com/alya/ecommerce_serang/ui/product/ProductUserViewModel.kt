package com.alya.ecommerce_serang.ui.product

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alya.ecommerce_serang.data.api.dto.CartItem
import com.alya.ecommerce_serang.data.api.dto.ProductsItem
import com.alya.ecommerce_serang.data.api.response.customer.cart.AddCartResponse
import com.alya.ecommerce_serang.data.api.response.customer.product.Product
import com.alya.ecommerce_serang.data.api.response.customer.product.ReviewsItem
import com.alya.ecommerce_serang.data.api.response.customer.product.StoreProduct
import com.alya.ecommerce_serang.data.repository.ProductRepository
import com.alya.ecommerce_serang.data.repository.Result
import kotlinx.coroutines.launch

class ProductUserViewModel(private val repository: ProductRepository) : ViewModel() {

    private val _productDetail = MutableLiveData<Product?>()
    val productDetail: LiveData<Product?> get() = _productDetail

    private val _storeDetail = MutableLiveData<Result<StoreProduct?>>()
    val storeDetail : LiveData<Result<StoreProduct?>> get() = _storeDetail

    private val _reviewProduct = MutableLiveData<List<ReviewsItem>>()
    val reviewProduct: LiveData<List<ReviewsItem>> get() = _reviewProduct

    private val _otherProducts = MutableLiveData<List<ProductsItem>>()
    val otherProducts: LiveData<List<ProductsItem>> get() = _otherProducts

    private val _addCart = MutableLiveData<Result<AddCartResponse>>()
    val addCart: LiveData<Result<AddCartResponse>> get() = _addCart

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error


    fun loadProductDetail(productId: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = repository.fetchProductDetail(productId)
                _productDetail.value = result?.product

                 //Load store details if product has a store ID
                result?.product?.storeId?.let { storeId ->
                    loadStoreDetail(storeId)
                }
            } catch (e: Exception) {
                Log.e("ProductViewModel", "Error loading product details: ${e.message}")
                _error.value = "Failed to load product details: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadStoreDetail(storeId: Int) {
        viewModelScope.launch {
            try {
                _storeDetail.value = Result.Loading
                val result = repository.fetchStoreDetail(storeId)
                _storeDetail.value = result
            } catch (e: Exception) {
                Log.e("ProductViewModel", "Error loading store details: ${e.message}")
                _storeDetail.value = Result.Error(e)
            }
        }
    }

    fun loadReviews(productId: Int) {
        viewModelScope.launch {
            try {
                val reviews = repository.fetchProductReview(productId)
                _reviewProduct.value = reviews ?: emptyList()
            } catch (e: Exception) {
                Log.e("ProductViewModel", "Error loading reviews: ${e.message}")
                _reviewProduct.value = emptyList()
            }
        }
    }

    fun loadOtherProducts(storeId: Int) {
        viewModelScope.launch {
            try {
                val result = repository.getAllProducts() // Fetch products

                if (result is Result.Success) {
                    val allProducts = result.data // Extract the list
                    val filteredProducts = allProducts.filter {
                        it.storeId == storeId && it.id != _productDetail.value?.productId
                    } // Filter by storeId and exclude current product
                    _otherProducts.value = filteredProducts // Update LiveData
                } else if (result is Result.Error) {
                    Log.e("ProductViewModel", "Error loading other products: ${result.exception.message}")
                    _otherProducts.value = emptyList() // Set empty list on failure
                }
            } catch (e: Exception) {
                Log.e("ProductViewModel", "Exception loading other products: ${e.message}")
                _otherProducts.value = emptyList()
            }
        }
    }
    fun reqCart(request: CartItem){
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = repository.addToCart(request)) {
                is Result.Success -> {
                    _addCart.value = result
                    _isLoading.value = false
                }
                is Result.Error -> {
                    _addCart.value = result
                    _error.value = result.exception.message ?: "Unknown error"
                    _isLoading.value = false
                }
                is Result.Loading -> {
                    _isLoading.value = true
                }
            }
        }
    }

}

//    fun loadStoreDetail(storeId: Int){
//        viewModelScope.launch {
//            val storeResult = repository.fetchStoreDetail(storeId)
//            _storeDetail.value = storeResult
//        }
//    }