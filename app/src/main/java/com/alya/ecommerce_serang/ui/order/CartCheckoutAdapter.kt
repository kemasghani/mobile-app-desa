package com.alya.ecommerce_serang.ui.order

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alya.ecommerce_serang.R
import com.alya.ecommerce_serang.data.api.dto.CheckoutData
import com.alya.ecommerce_serang.data.api.response.customer.cart.CartItemsItem
import com.alya.ecommerce_serang.databinding.ItemOrderProductBinding
import com.alya.ecommerce_serang.databinding.ItemOrderSellerBinding
import com.bumptech.glide.Glide
import java.text.NumberFormat
import java.util.Locale

class CartCheckoutAdapter(private val checkoutData: CheckoutData) :
    RecyclerView.Adapter<CartCheckoutAdapter.SellerViewHolder>() {

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

            // Set up products RecyclerView with multiple items
            rvSellerOrderProduct.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = MultiCartItemsAdapter(checkoutData.cartItems)
                isNestedScrollingEnabled = false
            }
        }
    }
}

class MultiCartItemsAdapter(private val cartItems: List<CartItemsItem>) :
    RecyclerView.Adapter<MultiCartItemsAdapter.CartItemViewHolder>() {

    class CartItemViewHolder(val binding: ItemOrderProductBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartItemViewHolder {
        val binding = ItemOrderProductBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CartItemViewHolder(binding)
    }

    override fun getItemCount(): Int = cartItems.size

    override fun onBindViewHolder(holder: CartItemViewHolder, position: Int) {
        val item = cartItems[position]

        with(holder.binding) {
            // Set cart item details
            tvProductName.text = item.productName
            tvProductQuantity.text = "${item.quantity} buah"
            tvProductPrice.text = formatCurrency(item.price.toDouble())

            // Load placeholder image
            Glide.with(ivProduct.context)
                .load(R.drawable.placeholder_image)
                .into(ivProduct)
        }
    }

    private fun formatCurrency(amount: Double): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        return formatter.format(amount).replace(",00", "")
    }
}