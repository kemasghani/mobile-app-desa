package com.alya.ecommerce_serang.utils.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alya.ecommerce_serang.data.api.dto.CategoryItem
import com.alya.ecommerce_serang.data.api.dto.Preorder
import com.alya.ecommerce_serang.data.api.dto.ProductsItem
import com.alya.ecommerce_serang.data.api.response.store.product.CreateProductResponse
import com.alya.ecommerce_serang.data.api.response.customer.product.Product
import com.alya.ecommerce_serang.data.api.response.customer.product.ReviewsItem
import com.alya.ecommerce_serang.data.api.response.customer.product.StoreProduct
import com.alya.ecommerce_serang.data.api.response.store.product.UpdateProductResponse
import com.alya.ecommerce_serang.data.repository.ProductRepository
import com.alya.ecommerce_serang.data.repository.Result
import kotlinx.coroutines.launch
import okhttp3.MultipartBody

class ProductViewModel(private val repository: ProductRepository) : ViewModel() {

    private val _productCreationResult = MutableLiveData<Result<CreateProductResponse>>()
    val productCreationResult: LiveData<Result<CreateProductResponse>> get() = _productCreationResult

    private val _productUpdateResult = MutableLiveData<Result<UpdateProductResponse>>()
    val productUpdateResult: LiveData<Result<UpdateProductResponse>> get() = _productUpdateResult

    private val _productDetail = MutableLiveData<Product?>()
    val productDetail: LiveData<Product?> get() = _productDetail

    private val _storeDetail = MutableLiveData<StoreProduct?>()
    val storeDetail : LiveData<StoreProduct?> get() = _storeDetail

    private val _reviewProduct = MutableLiveData<List<ReviewsItem>>()
    val reviewProduct: LiveData<List<ReviewsItem>> get() = _reviewProduct

    private val _productList = MutableLiveData<Result<List<ProductsItem>>>()
    val productList: LiveData<Result<List<ProductsItem>>> get() = _productList

    private val _categoryList = MutableLiveData<Result<List<CategoryItem>>>()
    val categoryList: LiveData<Result<List<CategoryItem>>> get() = _categoryList

    fun loadProductDetail(productId: Int) {
        viewModelScope.launch {
            val result = repository.fetchProductDetail(productId)
            _productDetail.value = result?.product
        }
    }

    fun loadReviews(productId: Int) {
        viewModelScope.launch {
            val reviews = repository.fetchProductReview(productId)
            _reviewProduct.value = reviews ?: emptyList()
        }
    }

    fun loadPreorderProducts(productId: Int) {
        viewModelScope.launch {

        }
    }

    fun loadMyStoreProducts() {
        viewModelScope.launch {
            _productList.value = Result.Loading
            try {
                val result = repository.fetchMyStoreProducts()
                _productList.value = Result.Success(result)
            } catch (e: Exception) {
                _productList.value = Result.Error(e)
            }
        }
    }

    fun loadCategories() {
        viewModelScope.launch {
            _categoryList.value = Result.Loading
            _categoryList.value = repository.getAllCategories()
        }
    }

    fun addProduct(
        name: String,
        description: String,
        price: Int,
        stock: Int,
        minOrder: Int,
        weight: Int,
        isPreOrder: Boolean,
        preorder: Preorder,
        categoryId: Int,
        status: String,
        condition: String,
        imagePart: MultipartBody.Part?,
        sppirtPart: MultipartBody.Part?,
        halalPart: MultipartBody.Part?
    ) {
        _productCreationResult.value = Result.Loading
        viewModelScope.launch {
            val result = repository.addProduct(
                name, description, price, stock, minOrder, weight, isPreOrder, preorder, categoryId, status, condition, imagePart, sppirtPart, halalPart
            )
            _productCreationResult.value = result
        }
    }

    fun updateProduct(productId: Int?, updatedProduct: Map<String, Any?>) {
        _productUpdateResult.value = Result.Loading
        viewModelScope.launch {
            try {
                val response = repository.updateProduct(productId, updatedProduct)
                _productUpdateResult.value = Result.Success(response)
            } catch (e: Exception) {
                _productUpdateResult.value = Result.Error(e)
            }
        }
    }

    fun deleteProduct(productId: Int) {
        viewModelScope.launch {
            val result = repository.deleteProduct(productId)
            // handle the response (loading, success, or error)
        }
    }

    // Optional: for store detail if you need it later
//    fun loadStoreDetail(storeId: Int) {
//        viewModelScope.launch {
//            val storeResult = repository.fetchStoreDetail(storeId)
//            _storeDetail.value = storeResult
//        }
//    }

}