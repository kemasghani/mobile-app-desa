package com.alya.ecommerce_serang.ui.order

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.alya.ecommerce_serang.R
import com.alya.ecommerce_serang.data.api.response.customer.cart.CartItemsItem
import com.alya.ecommerce_serang.databinding.ItemOrderProductBinding
import com.bumptech.glide.Glide
import java.text.NumberFormat
import java.util.Locale

class SingleCartItemAdapter(private val cartItem: CartItemsItem) :
    RecyclerView.Adapter<SingleCartItemAdapter.CartItemViewHolder>() {

    class CartItemViewHolder(val binding: ItemOrderProductBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartItemViewHolder {
        val binding = ItemOrderProductBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CartItemViewHolder(binding)
    }

    override fun getItemCount(): Int = 1

    override fun onBindViewHolder(holder: CartItemViewHolder, position: Int) {
        with(holder.binding) {
            // Set cart item details
            tvProductName.text = cartItem.productName
            tvProductQuantity.text = "${cartItem.quantity} buah"
            tvProductPrice.text = formatCurrency(cartItem.price.toDouble())

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
