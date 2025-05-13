package com.alya.ecommerce_serang.data.api.retrofit

import com.alya.ecommerce_serang.data.api.dto.AddEvidenceRequest
import com.alya.ecommerce_serang.data.api.dto.CartItem
import com.alya.ecommerce_serang.data.api.dto.CityResponse
import com.alya.ecommerce_serang.data.api.dto.CompletedOrderRequest
import com.alya.ecommerce_serang.data.api.dto.CourierCostRequest
import com.alya.ecommerce_serang.data.api.dto.CreateAddressRequest
import com.alya.ecommerce_serang.data.api.dto.GenericResponse
import com.alya.ecommerce_serang.data.api.dto.LoginRequest
import com.alya.ecommerce_serang.data.api.dto.OrderRequest
import com.alya.ecommerce_serang.data.api.dto.OrderRequestBuy
import com.alya.ecommerce_serang.data.api.dto.OtpRequest
import com.alya.ecommerce_serang.data.api.dto.PaymentMethodResponse
import com.alya.ecommerce_serang.data.api.dto.ProvinceResponse
import com.alya.ecommerce_serang.data.api.dto.RegisterRequest
import com.alya.ecommerce_serang.data.api.dto.ShippingService
import com.alya.ecommerce_serang.data.api.dto.ShippingServiceRequest
import com.alya.ecommerce_serang.data.api.dto.StoreAddressResponse
import com.alya.ecommerce_serang.data.api.dto.UpdateCart
import com.alya.ecommerce_serang.data.api.response.store.product.CreateProductResponse
import com.alya.ecommerce_serang.data.api.response.store.product.ViewStoreProductsResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import com.alya.ecommerce_serang.data.api.response.auth.LoginResponse
import com.alya.ecommerce_serang.data.api.response.auth.OtpResponse
import com.alya.ecommerce_serang.data.api.response.auth.RegisterResponse
import com.alya.ecommerce_serang.data.api.response.customer.cart.AddCartResponse
import com.alya.ecommerce_serang.data.api.response.customer.cart.ListCartResponse
import com.alya.ecommerce_serang.data.api.response.customer.cart.UpdateCartResponse
import com.alya.ecommerce_serang.data.api.response.order.AddEvidenceResponse
import com.alya.ecommerce_serang.data.api.response.order.ComplaintResponse
import com.alya.ecommerce_serang.data.api.response.order.CompletedOrderResponse
import com.alya.ecommerce_serang.data.api.response.customer.order.CourierCostResponse
import com.alya.ecommerce_serang.data.api.response.customer.order.CreateOrderResponse
import com.alya.ecommerce_serang.data.api.response.customer.order.ListCityResponse
import com.alya.ecommerce_serang.data.api.response.customer.order.ListProvinceResponse
import com.alya.ecommerce_serang.data.api.response.customer.order.OrderDetailResponse
import com.alya.ecommerce_serang.data.api.response.customer.order.OrderListResponse
import com.alya.ecommerce_serang.data.api.response.customer.product.AllProductResponse
import com.alya.ecommerce_serang.data.api.response.customer.product.CategoryResponse
import com.alya.ecommerce_serang.data.api.response.customer.product.DetailStoreProductResponse
import com.alya.ecommerce_serang.data.api.response.customer.product.ProductResponse
import com.alya.ecommerce_serang.data.api.response.customer.product.ReviewProductResponse
import com.alya.ecommerce_serang.data.api.response.customer.product.StoreResponse
import com.alya.ecommerce_serang.data.api.response.customer.profile.AddressResponse
import com.alya.ecommerce_serang.data.api.response.customer.profile.CreateAddressResponse
import com.alya.ecommerce_serang.data.api.response.customer.profile.ProfileResponse
import com.alya.ecommerce_serang.data.api.response.store.product.DeleteProductResponse
import com.alya.ecommerce_serang.data.api.response.store.product.UpdateProductResponse
import com.alya.ecommerce_serang.data.api.response.store.product.StoreDataResponse
import com.alya.ecommerce_serang.data.api.response.store.product.BalanceTopUpResponse
import com.alya.ecommerce_serang.data.api.dto.AddPaymentMethodResponse
import com.alya.ecommerce_serang.data.api.response.store.StoreResponse as MyStoreResponse
import com.alya.ecommerce_serang.data.api.response.store.TopUpResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @POST("registeruser")
    suspend fun register (
        @Body registerRequest: RegisterRequest
    ): Response<RegisterResponse>

    @POST("otp")
    suspend fun getOTP(
        @Body otpRequest: OtpRequest
    ):OtpResponse

    @POST("login")
    suspend fun login(
        @Body loginRequest: LoginRequest
    ): Response<LoginResponse>

    @GET("category")
    suspend fun allCategory(
    ): Response<CategoryResponse>

    @GET("product")
    suspend fun getAllProduct(): Response<AllProductResponse>

    @GET("product/review/{id}")
    suspend fun getProductReview(
        @Path("id") productId: Int
    ): Response<ReviewProductResponse>

    @GET("product/detail/{id}")
    suspend fun getDetailProduct (
        @Path("id") productId: Int
    ): Response<ProductResponse>

    @GET("profile")
    suspend fun getUserProfile(): Response<ProfileResponse>

    @GET("store/detail/{id}")
    suspend fun getDetailStore (
        @Path("id") storeId: Int
    ): Response<DetailStoreProductResponse>

    @POST("order")
    suspend fun postOrder(
        @Body request: OrderRequest
    ): Response<CreateOrderResponse>

    @GET("order/detail/{id}")
    suspend fun getDetailOrder(
        @Path("id") orderId: Int
    ): Response<OrderDetailResponse>

    @POST("order/addevidence")
    suspend fun addEvidence(
        @Body request : AddEvidenceRequest,
    ): Response<AddEvidenceResponse>

    @Multipart
    @POST("order/addevidence")
    suspend fun addEvidenceMultipart(
        @Part("order_id") orderId: RequestBody,
        @Part("amount") amount: RequestBody,
        @Part evidence: MultipartBody.Part
    ): Response<AddEvidenceResponse>

    @GET("order/{status}")
    suspend fun getOrderList(
        @Path("status") status: String
    ):Response<OrderListResponse>

    @POST("order")
    suspend fun postOrderBuyNow(
        @Body request: OrderRequestBuy
    ): Response<CreateOrderResponse>

    @GET("profile/address")
    suspend fun getAddress(
    ): Response<AddressResponse>

    @POST("profile/addaddress")
    suspend fun createAddress(
        @Body createAddressRequest: CreateAddressRequest
    ): Response<CreateAddressResponse>

    @GET("mystore")
    suspend fun getStore (): Response<StoreResponse>

    @GET("mystore/product") // Replace with actual endpoint
    suspend fun getStoreProduct(): Response<ViewStoreProductsResponse>

    @GET("category")
    fun getCategories(): Call<CategoryResponse>

    @Multipart
    @POST("store/createproduct")
    suspend fun addProduct(
        @Part("name") name: RequestBody,
        @Part("description") description: RequestBody,
        @Part("price") price: RequestBody,
        @Part("stock") stock: RequestBody,
        @Part("min_order") minOrder: RequestBody,
        @Part("weight") weight: RequestBody,
        @Part("is_pre_order") isPreOrder: RequestBody,
        @Part("duration") duration: RequestBody,
        @Part("category_id") categoryId: RequestBody,
        @Part("status") status: RequestBody,
        @Part("condition") condition: RequestBody,
        @Part image: MultipartBody.Part?,
        @Part sppirt: MultipartBody.Part?,
        @Part halal: MultipartBody.Part?
    ): Response<CreateProductResponse>

    @PUT("store/editproduct/{id}")
    suspend fun updateProduct(
        @Path("id") productId: Int?,
        @Body updatedProduct: Map<String, Any?>
    ): Response<UpdateProductResponse>

    @DELETE("store/deleteproduct/{id}")
    suspend fun deleteProduct(
        @Path("id") productId: Int
    ): Response<DeleteProductResponse>

    @GET("cart_item")
    suspend fun getCart (): Response<ListCartResponse>

    @POST("cart/add")
    suspend fun addCart(
        @Body cartRequest: CartItem
    ): Response<AddCartResponse>

    @PUT("cart/update")
    suspend fun updateCart(
        @Body updateCart: UpdateCart
    ): Response<UpdateCartResponse>

    @POST("couriercost")
    suspend fun countCourierCost(
        @Body courierCost : CourierCostRequest
    ): Response<CourierCostResponse>

    @GET("cities/{id}")
    suspend fun getCityProvId(
        @Path("id") provId : Int
    ): Response<ListCityResponse>

    @GET("provinces")
    suspend fun getListProv(
    ): Response<ListProvinceResponse>

    @GET("mystore/orders")
    suspend fun getAllOrders(): Response<OrderListResponse>

    @GET("mystore/orders/{status}")
    suspend fun getOrdersByStatus(
        @Query("status") status: String
    ): Response<OrderListResponse>

    @PUT("store/order/update")
    suspend fun confirmOrder(
        @Body confirmOrder : CompletedOrderRequest
    ): Response<CompletedOrderResponse>

    @Multipart
    @POST("addcomplaint")
    suspend fun addComplaint(
        @Part("order_id") orderId: RequestBody,
        @Part("description") description: RequestBody,
        @Part complaintimg: MultipartBody.Part
    ): Response<ComplaintResponse>

    @GET("mystore")
    suspend fun getStoreData(): Response<StoreDataResponse>

    @GET("mystore")
    suspend fun getMyStoreData(): Response<com.alya.ecommerce_serang.data.api.response.store.StoreResponse>

    @GET("store/topup")
    suspend fun getTopUpHistory(): Response<com.alya.ecommerce_serang.data.api.response.store.TopUpResponse>

    @GET("store/topup")
    suspend fun getFilteredTopUpHistory(@Query("date") date: String): Response<com.alya.ecommerce_serang.data.api.response.store.TopUpResponse>

    @Multipart
    @POST("store/createtopup")
    suspend fun addBalanceTopUp(
        @Part topupimg: MultipartBody.Part,
        @Part("amount") amount: RequestBody,
        @Part("payment_info_id") paymentInfoId: RequestBody,
        @Part("transaction_date") transactionDate: RequestBody,
        @Part("bank_name") bankName: RequestBody,
        @Part("bank_num") bankNum: RequestBody
    ): Response<BalanceTopUpResponse>

    @PUT("mystore/edit")
    suspend fun updateStoreProfile(
        @Body requestBody: okhttp3.RequestBody
    ): Response<StoreDataResponse>

    @Multipart
    @PUT("mystore/edit")
    suspend fun updateStoreProfileMultipart(
        @Part("store_name") storeName: RequestBody,
        @Part("store_status") storeStatus: RequestBody,
        @Part("store_description") storeDescription: RequestBody,
        @Part("is_on_leave") isOnLeave: RequestBody,
        @Part("city_id") cityId: RequestBody,
        @Part("province_id") provinceId: RequestBody,
        @Part("street") street: RequestBody,
        @Part("subdistrict") subdistrict: RequestBody,
        @Part("detail") detail: RequestBody,
        @Part("postal_code") postalCode: RequestBody,
        @Part("latitude") latitude: RequestBody,
        @Part("longitude") longitude: RequestBody,
        @Part("user_phone") userPhone: RequestBody,
        @Part storeimg: MultipartBody.Part?
    ): Response<StoreDataResponse>

    @GET("provinces")
    suspend fun getProvinces(): Response<ProvinceResponse>

    @GET("cities/{provinceId}")
    suspend fun getCities(
        @Path("provinceId") provinceId: String
    ): Response<CityResponse>

    @GET("mystore")
    suspend fun getStoreAddress(): Response<StoreAddressResponse>

    @PUT("mystore/edit")
    suspend fun updateStoreAddress(
        @Body addressData: HashMap<String, Any?>
    ): Response<StoreAddressResponse>

    // Payment Method API endpoints
    @Multipart
    @POST("mystore/payment/add")
    suspend fun addPaymentMethod(
        @Part("bank_name") bankName: RequestBody,
        @Part("bank_num") bankNum: RequestBody,
        @Part("account_name") accountName: RequestBody,
        @Part qris: MultipartBody.Part?
    ): Response<GenericResponse>

    @Multipart
    @POST("mystore/payment/add")
    suspend fun addPaymentMethodDirect(
        @Part("bank_name") bankName: RequestBody,
        @Part("bank_num") bankNum: RequestBody,
        @Part("account_name") accountName: RequestBody,
        @Part qris: MultipartBody.Part?
    ): Response<AddPaymentMethodResponse>

    @DELETE("mystore/payment/delete/{id}")
    suspend fun deletePaymentMethod(
        @Path("id") paymentMethodId: Int
    ): Response<GenericResponse>

    // Shipping Service API endpoints
    @POST("mystore/shipping/add")
    suspend fun addShippingService(
        @Body request: ShippingServiceRequest
    ): Response<GenericResponse>

    @POST("mystore/shipping/delete")
    suspend fun deleteShippingService(
        @Body request: ShippingServiceRequest
    ): Response<GenericResponse>
}