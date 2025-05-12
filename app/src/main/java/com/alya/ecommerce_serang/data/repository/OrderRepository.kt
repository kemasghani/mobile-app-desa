package com.alya.ecommerce_serang.data.repository

import android.util.Log
import com.alya.ecommerce_serang.data.api.dto.AddEvidenceMultipartRequest
import com.alya.ecommerce_serang.data.api.dto.CompletedOrderRequest
import com.alya.ecommerce_serang.data.api.dto.CourierCostRequest
import com.alya.ecommerce_serang.data.api.dto.CreateAddressRequest
import com.alya.ecommerce_serang.data.api.dto.OrderRequest
import com.alya.ecommerce_serang.data.api.dto.OrderRequestBuy
import com.alya.ecommerce_serang.data.api.dto.OrdersItem
import com.alya.ecommerce_serang.data.api.dto.UserProfile
import com.alya.ecommerce_serang.data.api.response.customer.cart.DataItem
import com.alya.ecommerce_serang.data.api.response.customer.order.CreateOrderResponse
import com.alya.ecommerce_serang.data.api.response.customer.order.OrderDetailResponse
import com.alya.ecommerce_serang.data.api.response.customer.order.OrderListResponse
import com.alya.ecommerce_serang.data.api.response.customer.product.ProductResponse
import com.alya.ecommerce_serang.data.api.response.order.AddEvidenceResponse
import com.alya.ecommerce_serang.data.api.response.order.ComplaintResponse
import com.alya.ecommerce_serang.data.api.response.order.CompletedOrderResponse
import com.alya.ecommerce_serang.data.api.response.customer.order.CourierCostResponse
import com.alya.ecommerce_serang.data.api.response.customer.order.ListCityResponse
import com.alya.ecommerce_serang.data.api.response.customer.order.ListProvinceResponse
import com.alya.ecommerce_serang.data.api.response.customer.product.StoreProduct
import com.alya.ecommerce_serang.data.api.response.customer.product.StoreResponse
import com.alya.ecommerce_serang.data.api.response.customer.profile.AddressResponse
import com.alya.ecommerce_serang.data.api.response.customer.profile.CreateAddressResponse
import com.alya.ecommerce_serang.data.api.retrofit.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import java.io.File

class OrderRepository(private val apiService: ApiService) {

    suspend fun fetchProductDetail(productId: Int): ProductResponse? {
        return try {
            val response = apiService.getDetailProduct(productId)
            if (response.isSuccessful) {
                val productResponse = response.body()
                Log.d("Order Repository", "Product detail fetched successfully: ${productResponse?.product?.productName}")
                productResponse
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Log.e("Order Repository", "Error fetching product detail. Code: ${response.code()}, Error: $errorBody")
                null
            }
        } catch (e: Exception) {
            Log.e("Order Repository", "Exception fetching product", e)
            null
        }
    }

    suspend fun createOrder(orderRequest: OrderRequest): Response<CreateOrderResponse> {
        return try {
            Log.d("Order Repository", "Creating order. Request details: $orderRequest")
            val response = apiService.postOrder(orderRequest)

            if (response.isSuccessful) {
                Log.d("Order Repository", "Order created successfully. Response: ${response.body()}")
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Log.e("Order Repository", "Order creation failed. Code: ${response.code()}, Error: $errorBody")
            }

            response
        } catch (e: Exception) {
            Log.e("Order Repository", "Exception creating order", e)
            throw e
        }
    }

    suspend fun createOrderBuyNow(orderRequestBuy: OrderRequestBuy): Response<CreateOrderResponse> {
        return try {
            Log.d("Order Repository", "Creating buy now order. Request details: $orderRequestBuy")
            val response = apiService.postOrderBuyNow(orderRequestBuy)

            if (response.isSuccessful) {
                Log.d("Order Repository", "Buy now order created successfully. Response: ${response.body()}")
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Log.e("Order Repository", "Buy now order creation failed. Code: ${response.code()}, Error: $errorBody")
            }

            response
        } catch (e: Exception) {
            Log.e("Order Repository", "Exception creating buy now order", e)
            throw e
        }
    }

    suspend fun getStore(): StoreResponse? {
        return try {
            val response = apiService.getStore()

            if (response.isSuccessful) {
                val storeResponse = response.body()
                Log.d("Order Repository", "Store information fetched successfully. Store count: ${storeResponse?.store?.storeName}")
                storeResponse
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Log.e("Order Repository", "Error fetching store. Code: ${response.code()}, Error: $errorBody")
                null
            }
        } catch (e: Exception) {
            Log.e("Order Repository", "Exception getting store", e)
            null
        }
    }

    suspend fun getAddress(): AddressResponse? {
        return try {
            val response = apiService.getAddress()

            if (response.isSuccessful) {
                val addressResponse = response.body()
                Log.d("Order Repository", "Address information fetched successfully. Address count: ${addressResponse?.addresses?.size}")
                addressResponse
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Log.e("Order Repository", "Error fetching addresses. Code: ${response.code()}, Error: $errorBody")
                null
            }
        } catch (e: Exception) {
            Log.e("Order Repository", "Exception getting addresses", e)
            null
        }
    }

    suspend fun getCountCourierCost(courierCost: CourierCostRequest): Result<CourierCostResponse> {
        return try {
            Log.d("Order Repository", "Calculating courier cost. Request: $courierCost")
            val response = apiService.countCourierCost(courierCost)

            if (response.isSuccessful) {
                response.body()?.let { courierCostResponse ->
                    Log.d("Order Repository", "Courier cost calculation successful. Courier costs: ${courierCostResponse.courierCosts.size}")
                    Result.Success(courierCostResponse)
                } ?: run {
                    Result.Error(Exception("Failed to get courier cost: Empty response"))
                }
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                Log.e("Order Repository", "Error calculating courier cost. Code: ${response.code()}, Error: $errorMsg")
                Result.Error(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("Order Repository", "Exception calculating courier cost", e)
            Result.Error(e)
        }
    }

    suspend fun getCart(): Result<List<DataItem>> {
        return try {
            val response = apiService.getCart()

            if (response.isSuccessful) {
                val cartData = response.body()?.data
                if (!cartData.isNullOrEmpty()) {
                    Result.Success(cartData)
                } else {
                    Log.e("Order Repository", "Cart data is empty")
                    Result.Error(Exception("Cart is empty"))
                }
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                Log.e("Order Repository", "Error fetching cart: $errorMsg")
                Result.Error(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("Order Repository", "Exception fetching cart", e)
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
                    Result.Error(Exception("Store details not found"))
                }
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                Log.e("Order Repository", "Error fetching store: $errorMsg")
                Result.Error(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("Order Repository", "Exception fetching store details", e)
            Result.Error(e)
        }
    }

    suspend fun addAddress(request: CreateAddressRequest): Result<CreateAddressResponse> {
        return try {
            Log.d("OrderRepository", "Adding address: $request")
            val response = apiService.createAddress(request)

            if (response.isSuccessful) {
                val createAddressResponse = response.body()
                if (createAddressResponse != null) {
                    Log.d("OrderRepository", "Address added successfully: ${createAddressResponse.message}")
                    Result.Success(createAddressResponse)
                } else {
                    Log.e("OrderRepository", "Response body was null")
                    Result.Error(Exception("Empty response from server"))
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Log.e("OrderRepository", "Error adding address: $errorBody")
                Result.Error(Exception(errorBody))
            }
        } catch (e: Exception) {
            Log.e("OrderRepository", "Exception adding address", e)
            Result.Error(e)
        }
    }

    suspend fun getListProvinces(): ListProvinceResponse? {
        val response = apiService.getListProv()
        return if (response.isSuccessful) response.body() else null
    }

    suspend fun getListCities(provId : Int): ListCityResponse?{
        val response = apiService.getCityProvId(provId)
        return if (response.isSuccessful) response.body() else null
    }

    suspend fun fetchSells(): List<OrdersItem> {
        return try {
            val response = apiService.getAllOrders() // Replace with the actual method from your ApiService
            if (response.isSuccessful) {
                response.body()?.orders ?: emptyList() // Assuming the response body has 'orders'
            } else {
                Log.e("OrderRepository", "Error fetching all sells. Code: ${response.code()}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("OrderRepository", "Exception fetching sells", e)
            emptyList()
        }
    }

    suspend fun fetchOrdersByStatus(status: String): List<OrdersItem?> {
        return try {
            val response = apiService.getOrdersByStatus(status) // Replace with actual method for status-based fetch
            if (response.isSuccessful) {
                response.body()?.orders?.filterNotNull() ?: emptyList() // Assuming the response body has 'orders'
            } else {
                Log.e("OrderRepository", "Error fetching orders by status ($status). Code: ${response.code()}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("OrderRepository", "Exception fetching orders by status", e)
            emptyList()
        }
    }
    suspend fun fetchUserProfile(): Result<UserProfile?> {
        return try {
            val response = apiService.getUserProfile()
            if (response.isSuccessful) {
                response.body()?.user?.let {
                    Result.Success(it)  // ✅ Returning only UserProfile
                } ?: Result.Error(Exception("User data not found"))
            } else {
                Result.Error(Exception("Error fetching profile: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun getOrderDetails(orderId: Int): OrderDetailResponse? {
        return try {
            val response = apiService.getDetailOrder(orderId)
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) {
            Log.e("OrderRepository", "Error getting order details", e)
            null
        }
    }

//    suspend fun uploadPaymentProof(request : AddEvidenceRequest): Result<AddEvidenceResponse> {
//        return try {
//            Log.d("OrderRepository", "Add Evidence : $request")
//            val response = apiService.addEvidence(request)
//
//            if (response.isSuccessful) {
//                val addEvidenceResponse = response.body()
//                if (addEvidenceResponse != null) {
//                    Log.d("OrderRepository", "Add Evidence successfully: ${addEvidenceResponse.message}")
//                    Result.Success(addEvidenceResponse)
//                } else {
//                    Log.e("OrderRepository", "Response body was null")
//                    Result.Error(Exception("Empty response from server"))
//                }
//            } else {
//                val errorBody = response.errorBody()?.string() ?: "Unknown error"
//                Log.e("OrderRepository", "Error Add Evidence : $errorBody")
//                Result.Error(Exception(errorBody))
//            }
//        } catch (e: Exception) {
//            Log.e("OrderRepository", "Exception Add Evidence ", e)
//            Result.Error(e)
//        }
//    }
suspend fun uploadPaymentProof(request: AddEvidenceMultipartRequest): Result<AddEvidenceResponse> {
    return try {
        Log.d("OrderRepository", "Uploading payment proof...")

        val response = apiService.addEvidenceMultipart(
            orderId = request.orderId,
            amount = request.amount,
            evidence = request.evidence
        )

        if (response.isSuccessful) {
            val addEvidenceResponse = response.body()
            if (addEvidenceResponse != null) {
                Log.d("OrderRepository", "Payment proof uploaded successfully: ${addEvidenceResponse.message}")
                Result.Success(addEvidenceResponse)
            } else {
                Log.e("OrderRepository", "Response body was null")
                Result.Error(Exception("Empty response from server"))
            }
        } else {
            val errorBody = response.errorBody()?.string() ?: "Unknown error"
            Log.e("OrderRepository", "Error uploading payment proof: $errorBody")
            Result.Error(Exception(errorBody))
        }
    } catch (e: Exception) {
        Log.e("OrderRepository", "Exception uploading payment proof", e)
        Result.Error(e)
    }
}

    suspend fun getOrderList(status: String): Result<OrderListResponse> {
        return try {
            Log.d("OrderRepository", "Add Evidence : $status")
            val response = apiService.getOrderList(status)

            if (response.isSuccessful) {
                val allListOrder = response.body()
                if (allListOrder != null) {
                    Log.d("OrderRepository", "Add Evidence successfully: ${allListOrder.message}")
                    Result.Success(allListOrder)
                } else {
                    Log.e("OrderRepository", "Response body was null")
                    Result.Error(Exception("Empty response from server"))
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Log.e("OrderRepository", "Error Add Evidence : $errorBody")
                Result.Error(Exception(errorBody))
            }
        } catch (e: Exception) {
            Log.e("OrderRepository", "Exception Add Evidence ", e)
            Result.Error(e)
        }
    }

    suspend fun confirmOrderCompleted(request: CompletedOrderRequest): Result<CompletedOrderResponse> {
        return try {
            Log.d("OrderRepository", "Cinfroming order request completed: $request")
            val response = apiService.confirmOrder(request)

            if(response.isSuccessful) {
                val completedOrderResponse = response.body()
                if (completedOrderResponse != null) {
                    Log.d("OrderRepository", "Order confirmed successfully: ${completedOrderResponse.message}")
                    Result.Success(completedOrderResponse)
                } else {
                    Log.e("OrderRepository", "Response body was null")
                    Result.Error(Exception("Empty response from server"))
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown Error"
                Log.e("OrderRepository", "Error confirming order: $errorBody")
                Result.Error(Exception(errorBody))
            }
        } catch (e: Exception){
            Result.Error(e)
        }
    }

    fun submitComplaint(
        orderId: String,
        reason: String,
        imageFile: File?
    ): Flow<Result<ComplaintResponse>> = flow {
        emit(Result.Loading)

        try {
            // Debug logging
            Log.d("OrderRepository", "Submitting complaint for order: $orderId")
            Log.d("OrderRepository", "Reason: $reason")
            Log.d("OrderRepository", "Image file: ${imageFile?.absolutePath ?: "null"}")

            // Create form data for the multipart request
            // Explicitly convert orderId to string to ensure correct formatting
            val orderIdRequestBody = orderId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val reasonRequestBody = reason.toRequestBody("text/plain".toMediaTypeOrNull())

            // Create the image part for the API
            val imagePart = if (imageFile != null && imageFile.exists()) {
                // Use the actual image file
                // Use asRequestBody() for files which is more efficient
                val imageRequestBody = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData(
                    "complaintimg",
                    imageFile.name,
                    imageRequestBody
                )
            } else {
                // Create a simple empty part if no image
                val dummyRequestBody = "".toRequestBody("text/plain".toMediaTypeOrNull())
                MultipartBody.Part.createFormData(
                    "complaintimg",
                    "",
                    dummyRequestBody
                )
            }

            // Log request details before making the API call
            Log.d("OrderRepository", "Making API call to add complaint")
            Log.d("OrderRepository", "orderId: $orderId (as string)")

            val response = apiService.addComplaint(
                orderIdRequestBody,
                reasonRequestBody,
                imagePart
            )

            Log.d("OrderRepository", "Response code: ${response.code()}")
            Log.d("OrderRepository", "Response message: ${response.message()}")

            if (response.isSuccessful && response.body() != null) {
                val complaintResponse = response.body() as ComplaintResponse
                emit(Result.Success(complaintResponse))
            } else {
                // Get the error message from the response if possible
                val errorBody = response.errorBody()?.string()
                val errorMessage = if (!errorBody.isNullOrEmpty()) {
                    "Server error: $errorBody"
                } else {
                    "Failed to submit complaint: ${response.code()} ${response.message()}"
                }
                Log.e("OrderRepository", errorMessage)
                emit(Result.Error(Exception(errorMessage)))
            }
        } catch (e: Exception) {
            Log.e("OrderRepository", "Error submitting complaint: ${e.message}")
            emit(Result.Error(e))
        }
    }.flowOn(Dispatchers.IO)

}