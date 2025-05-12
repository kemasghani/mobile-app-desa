package com.alya.ecommerce_serang.data.repository

import android.util.Log
import com.alya.ecommerce_serang.data.api.dto.CartItem
import com.alya.ecommerce_serang.data.api.dto.CategoryItem
import com.alya.ecommerce_serang.data.api.dto.Preorder
import com.alya.ecommerce_serang.data.api.dto.ProductsItem
import com.alya.ecommerce_serang.data.api.response.store.product.CreateProductResponse
import com.alya.ecommerce_serang.data.api.response.customer.cart.AddCartResponse
import com.alya.ecommerce_serang.data.api.response.customer.product.ProductResponse
import com.alya.ecommerce_serang.data.api.response.customer.product.ReviewsItem
import com.alya.ecommerce_serang.data.api.response.customer.product.StoreProduct
import com.alya.ecommerce_serang.data.api.response.store.product.UpdateProductResponse
import com.alya.ecommerce_serang.data.api.retrofit.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody

class ProductRepository(private val apiService: ApiService) {
    suspend fun getAllProducts(): Result<List<ProductsItem>> =
        withContext(Dispatchers.IO) {
            try {
                Log.d("ProductRepository", "Attempting to fetch products")
                val response = apiService.getAllProduct()

                if (response.isSuccessful) {
                    // Return a Result.Success with the list of products

                    val products = response.body()?.products ?: emptyList()
                    Log.d(TAG, "Products fetched successfully. Total products: ${products.size}")

                    // Optional: Log some product details
                    products.take(3).forEach { product ->
                        Log.d(TAG, "Sample Product - ID: ${product.id}, Name: ${product.name}, Price: ${product.price}")
                    }

                    Result.Success(products)
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    Log.e(TAG, "Failed to fetch products. Code: ${response.code()}, Error: $errorBody")
                    Result.Error(Exception("Failed to fetch products. Code: ${response.code()}"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception while fetching products", e)
                Result.Error(e)
            }
        }

    suspend fun fetchProductDetail(productId: Int): ProductResponse? {
        return try {
            Log.d(TAG, "Fetching product detail for ID: $productId")
            val response = apiService.getDetailProduct(productId)
            if (response.isSuccessful) {
                val productResponse = response.body()
                Log.d(TAG, "Product detail fetched successfully. Product: ${productResponse?.product?.productName}")
                productResponse
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Log.e(TAG, "Error fetching product detail. Code: ${response.code()}, Error: $errorBody")
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getAllCategories(): Result<List<CategoryItem>> =
        withContext(Dispatchers.IO) {
            try {
                Log.d("Categories", "Attempting to fetch categories")
                val response = apiService.allCategory()

                if (response.isSuccessful) {
                    val categories = response.body()?.category ?: emptyList()
                    Log.d("ProductRepository", "Fetched categories: $categories")
                    categories.forEach { Log.d("Category Image", "Category: ${it.name}, Image: ${it.image}") }
                    Result.Success(categories)
                } else {
                    Result.Error(Exception("Failed to fetch categories. Code: ${response.code()}"))
                }
            } catch (e: Exception) {
                Log.e("ProductRepository", "Error fetching categories", e)
                Result.Error(e)
            }
        }

    suspend fun fetchProductReview(productId: Int): List<ReviewsItem>? {
        return try {
            val response = apiService.getProductReview(productId)
            if (response.isSuccessful) {
                response.body()?.reviews // Ambil daftar review dari response
            } else {
                Log.e("ProductRepository", "Error: ${response.errorBody()?.string()}")
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun addToCart(request: CartItem): Result<AddCartResponse> {
        return try {
            val response = apiService.addCart(request)
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.Success(it)
                } ?: Result.Error(Exception("Add Cart failed"))
            } else {
                Log.e("ProductRepository", "Error: ${response.errorBody()?.string()}")
                Result.Error(Exception(response.errorBody()?.string() ?: "Unknown Error"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun fetchStoreDetail(storeId: Int): Result<StoreProduct?> {
        return try {
            val response = apiService.getDetailStore(storeId)
            if (response.isSuccessful) {
                val store = response.body()?.store
                if (store != null) {
                    Result.Success(store)
                } else {
                    Result.Error(Throwable("Empty response body"))
                }
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                Log.e("ProductRepository", "Error: $errorMsg")
                Result.Error(Throwable(errorMsg))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun fetchMyStoreProducts(): List<ProductsItem> {
        return try {
            val response = apiService.getStoreProduct()
            if (response.isSuccessful) {
                response.body()?.products?.filterNotNull() ?: emptyList()
            } else {
                throw Exception("Failed to fetch store products: ${response.message()}")
            }
        } catch (e: Exception) {
            Log.e("ProductRepository", "Error fetching store products", e)
            throw e
        }
    }

    suspend fun addProduct(
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
    ): Result<CreateProductResponse> {
        return try {
            val response = apiService.addProduct(
                name = RequestBody.create("text/plain".toMediaTypeOrNull(), name),
                description = RequestBody.create("text/plain".toMediaTypeOrNull(), description),
                price = RequestBody.create("text/plain".toMediaTypeOrNull(), price.toString()),
                stock = RequestBody.create("text/plain".toMediaTypeOrNull(), stock.toString()),
                minOrder = RequestBody.create("text/plain".toMediaTypeOrNull(), minOrder.toString()),
                weight = RequestBody.create("text/plain".toMediaTypeOrNull(), weight.toString()),
                isPreOrder = RequestBody.create("text/plain".toMediaTypeOrNull(), isPreOrder.toString()),
                duration = RequestBody.create("text/plain".toMediaTypeOrNull(), preorder.duration.toString()),
                categoryId = RequestBody.create("text/plain".toMediaTypeOrNull(), categoryId.toString()),
                status = RequestBody.create("text/plain".toMediaTypeOrNull(), status),
                condition = RequestBody.create("text/plain".toMediaTypeOrNull(), condition),
                image = imagePart,
                sppirt = sppirtPart,
                halal = halalPart
            )

            if (response.isSuccessful) {
                Result.Success(response.body()!!)
            } else {
                Result.Error(Exception("Failed to create product: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun updateProduct(productId: Int?, updatedProduct: Map<String, Any?>) : UpdateProductResponse {
        // Build the request with the updated fields
        val response = apiService.updateProduct(productId, updatedProduct)
        if (response.isSuccessful) {
            return response.body()!!
        } else {
            throw Exception("Gagal memperbarui produk: ${response.code()}")
        }
    }


    suspend fun deleteProduct(productId: Int): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.deleteProduct(productId)
                if (response.isSuccessful) {
                    Result.Success(Unit)
                } else {
                    Result.Error(Exception("Gagal menghapus produk: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }

    companion object {
        private const val TAG = "ProductRepository"
    }
}

//    suspend fun fetchStoreDetail(storeId: Int): Store? {
//        return try {
//            val response = apiService.getStore(storeId)
//            if (response.isSucessful) {
//                response.body()?.store
//            } else {
//                Log.e("ProductRepository", "Error: ${response.errorBody()?.string()}")
//
//                null
//            }
//        } catch (e: Exception) {
//            null
//        }
//    }