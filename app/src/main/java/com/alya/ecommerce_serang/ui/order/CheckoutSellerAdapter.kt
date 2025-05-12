package com.alya.ecommerce_serang.ui.order

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alya.ecommerce_serang.data.api.dto.CheckoutData
import com.alya.ecommerce_serang.databinding.ItemOrderSellerBinding

// Adapter for seller section that contains the product
class CheckoutSellerAdapter(private val checkoutData: CheckoutData) :
    RecyclerView.Adapter<CheckoutSellerAdapter.SellerViewHolder>() {

    class SellerViewHolder(val binding: ItemOrderSellerBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SellerViewHolder {
        val binding = ItemOrderSellerBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return SellerViewHolder(binding)
    }

    override fun getItemCount(): Int = 1 // Only one seller

    override fun onBindViewHolder(holder: SellerViewHolder, position: Int) {
        with(holder.binding) {
            // Set seller name
            tvStoreName.text = checkoutData.sellerName

            // Set up products RecyclerView
            rvSellerOrderProduct.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = if (checkoutData.isBuyNow) {
                    // Single product for Buy Now
                    SingleProductAdapter(checkoutData)
                } else {
                    // Single cart item
                    SingleCartItemAdapter(checkoutData.cartItems.first())
                }
                isNestedScrollingEnabled = false
            }
        }
    }
}