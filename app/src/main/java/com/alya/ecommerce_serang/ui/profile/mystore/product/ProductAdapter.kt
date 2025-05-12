package com.alya.ecommerce_serang.ui.profile.mystore.product

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.alya.ecommerce_serang.R
import com.alya.ecommerce_serang.data.api.dto.Product
import com.alya.ecommerce_serang.data.api.dto.ProductsItem
import com.bumptech.glide.Glide

class ProductAdapter(
    private val products: List<ProductsItem>,
    private val onItemClick: (ProductsItem) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivProduct: ImageView = itemView.findViewById(R.id.iv_product)
        private val tvProductName: TextView = itemView.findViewById(R.id.tv_product_name)
        private val tvProductPrice: TextView = itemView.findViewById(R.id.tv_product_price)
        private val tvProductStock: TextView = itemView.findViewById(R.id.tv_product_stock)
        private val tvProductStatus: TextView = itemView.findViewById(R.id.tv_product_status)
        private val ivMenu: ImageView = itemView.findViewById(R.id.iv_menu)

        fun bind(product: ProductsItem) {
            tvProductName.text = product.name
            tvProductPrice.text = "Rp${product.price}"
            tvProductStock.text = "Stok: ${product.stock}"

            if (product.status.equals("active",true)) {
                tvProductStatus.text = "Aktif"
                tvProductStatus.setTextColor(ContextCompat.getColor(itemView.context, R.color.darkblue_500))
                tvProductStatus.background = ContextCompat.getDrawable(itemView.context, R.drawable.bg_product_active)
            } else {
                tvProductStatus.text = "Nonaktif"
                tvProductStatus.setTextColor(ContextCompat.getColor(itemView.context, R.color.black_500))
                tvProductStatus.background = ContextCompat.getDrawable(itemView.context, R.drawable.bg_product_inactive)
            }

            Glide.with(itemView.context)
                .load(product.image)
                .placeholder(R.drawable.placeholder_image)
                .into(ivProduct)

            ivMenu.setOnClickListener {
                // Show Bottom Sheet when menu is clicked
                val bottomSheetFragment = ProductOptionsBottomSheetFragment(product)
                bottomSheetFragment.show(
                    (itemView.context as FragmentActivity).supportFragmentManager,
                    bottomSheetFragment.tag
                )
            }

            itemView.setOnClickListener {
                onItemClick(product)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_store_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun getItemCount(): Int = products.size

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(products[position])
    }
}