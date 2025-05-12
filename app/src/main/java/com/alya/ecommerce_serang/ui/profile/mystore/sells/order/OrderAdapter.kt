package com.alya.ecommerce_serang.ui.profile.mystore.sells.order

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.alya.ecommerce_serang.R
import com.alya.ecommerce_serang.data.api.dto.OrdersItem

class OrderAdapter : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    private var orderList: List<OrdersItem?>? = emptyList()

    fun submitList(orders: List<OrdersItem?>?) {
        orderList = orders
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): OrderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_sells_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orderList?.get(position)
        holder.bind(order)
    }

    override fun getItemCount(): Int = orderList?.size ?: 0

    class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvOrderNumber: TextView = itemView.findViewById(R.id.tv_order_number)
        private val tvOrderCustomer: TextView = itemView.findViewById(R.id.tv_order_customer)
        private val tvOrderDue: TextView = itemView.findViewById(R.id.tv_order_due)
        private val ivOrderProduct: ImageView = itemView.findViewById(R.id.iv_order_product)
        private val tvOrderProductName: TextView = itemView.findViewById(R.id.tv_order_product_name)
        private val tvOrderProductVariant: TextView = itemView.findViewById(R.id.tv_order_product_variant)
        private val tvOrderProductQty: TextView = itemView.findViewById(R.id.tv_order_product_qty)
        private val tvOrderProductPrice: TextView = itemView.findViewById(R.id.tv_order_product_price)
        private val tvOrderQty: TextView = itemView.findViewById(R.id.tv_order_qty)
        private val tvOrderPrice: TextView = itemView.findViewById(R.id.tv_order_price)
        private val tvSeeMore: TextView = itemView.findViewById(R.id.tv_see_more)
        private val btnEditOrder: Button = itemView.findViewById(R.id.btn_edit_order)
        private val btnConfirmOrder: Button = itemView.findViewById(R.id.btn_confirm_order)

        fun bind(order: OrdersItem?) {
            tvOrderNumber.text = "No. Pesanan: ${order?.orderId}"
            tvOrderCustomer.text = order?.userId.toString()
            tvOrderDue.text = order?.createdAt + 7
            tvOrderQty.text = "${order?.orderItems?.size} produk"
            tvOrderPrice.text = "Rp${order?.totalAmount}"
        }
    }
}