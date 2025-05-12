package com.alya.ecommerce_serang.ui.order.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.alya.ecommerce_serang.R
import com.alya.ecommerce_serang.data.api.response.customer.order.OrderItemsItem
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton

class OrderProductAdapter : RecyclerView.Adapter<OrderProductAdapter.ProductViewHolder>() {

    private val products = mutableListOf<OrderItemsItem>()

    fun submitList(newProducts: List<OrderItemsItem>) {
        products.clear()
        products.addAll(newProducts)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_order_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(products[position])
    }

    override fun getItemCount(): Int = products.size

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivProductImage: ImageView = itemView.findViewById(R.id.iv_product)
        private val tvProductName: TextView = itemView.findViewById(R.id.tv_product_name)
        private val tvQuantity: TextView = itemView.findViewById(R.id.tv_product_quantity)
        private val tvProductPrice: TextView = itemView.findViewById(R.id.tv_product_price)

        fun bind(product: OrderItemsItem) {
            // Set product name
            tvProductName.text = product.productName

            // Set quantity with suffix
            tvQuantity.text = "${product.quantity} buah"

            // Set price with currency format
            tvProductPrice.text = formatCurrency(product.price)

            // Load product image using Glide
            Glide.with(itemView.context)
                .load(product.productImage)
                .placeholder(R.drawable.placeholder_image)
//                .error(R.drawable.error_image)
                .into(ivProductImage)

        }



        private fun formatCurrency(amount: Int): String {
            // In a real app, you would use NumberFormat for proper currency formatting
            // For simplicity, just return a basic formatted string
            return "Rp${amount}"
        }
    }
}