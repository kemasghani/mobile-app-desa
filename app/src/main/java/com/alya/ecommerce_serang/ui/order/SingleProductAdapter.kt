package com.alya.ecommerce_serang.ui.order

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.alya.ecommerce_serang.R
import com.alya.ecommerce_serang.data.api.dto.CheckoutData
import com.alya.ecommerce_serang.data.api.dto.OrderRequestBuy
import com.alya.ecommerce_serang.databinding.ItemOrderProductBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import java.text.NumberFormat
import java.util.Locale

class SingleProductAdapter(private val checkoutData: CheckoutData) :
    RecyclerView.Adapter<SingleProductAdapter.ProductViewHolder>() {

    class ProductViewHolder(val binding: ItemOrderProductBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemOrderProductBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ProductViewHolder(binding)
    }

    override fun getItemCount(): Int = 1

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        with(holder.binding) {
            // Set product details
            tvProductName.text = checkoutData.productName

            val quantity = (checkoutData.orderRequest as OrderRequestBuy).quantity
            tvProductQuantity.text = "$quantity buah"

            tvProductPrice.text = formatCurrency(checkoutData.productPrice)

            // Load product image
            Glide.with(ivProduct.context)
                .load(checkoutData.productImageUrl)
                .apply(
                    RequestOptions()
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.placeholder_image))
                .into(ivProduct)
        }
    }

    private fun formatCurrency(amount: Double): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        return formatter.format(amount).replace(",00", "")
    }
}