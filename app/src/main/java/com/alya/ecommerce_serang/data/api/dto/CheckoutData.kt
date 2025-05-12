package com.alya.ecommerce_serang.data.api.dto

import com.alya.ecommerce_serang.data.api.response.customer.cart.CartItemsItem

data class CheckoutData(
    val orderRequest: Any, // Can be OrderRequest or OrderRequestBuy
    val productName: String? = "",
    val productImageUrl: String = "",
    val productPrice: Double = 0.0,
    val sellerName: String = "",
    val sellerImageUrl: String? = null,
    val sellerId: Int = 0,
    val quantity: Int = 1,
    val isBuyNow: Boolean = false,
    val cartItems: List<CartItemsItem> = emptyList()
)