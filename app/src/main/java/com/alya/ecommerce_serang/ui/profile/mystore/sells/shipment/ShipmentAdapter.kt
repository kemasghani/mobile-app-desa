package com.alya.ecommerce_serang.ui.profile.mystore.sells.shipment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.alya.ecommerce_serang.R
import com.alya.ecommerce_serang.data.api.dto.OrdersItem

class ShipmentAdapter : RecyclerView.Adapter<ShipmentAdapter.ShipmentViewHolder>() {

    private var shipmentList: List<OrdersItem> = emptyList()

    fun submitList(orders: List<OrdersItem>) {
        shipmentList = orders
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ShipmentAdapter.ShipmentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_sells_shipment, parent, false)
        return ShipmentViewHolder(view)
    }

    override fun onBindViewHolder(holder: ShipmentAdapter.ShipmentViewHolder, position: Int) {
        val order = shipmentList[position]
        holder.bind(order)
    }

    override fun getItemCount(): Int = shipmentList.size

    class ShipmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvShipmentNumber: TextView = itemView.findViewById(R.id.tv_shipment_number)
        private val tvShipmentDue: TextView = itemView.findViewById(R.id.tv_shipment_due)
        private val ivShipmentProduct: ImageView = itemView.findViewById(R.id.iv_shipment_product)
        private val tvShipmentProductName: TextView = itemView.findViewById(R.id.tv_shipment_product_name)
        private val tvShipmentProductVariant: TextView = itemView.findViewById(R.id.tv_shipment_product_variant)
        private val tvShipmentProductQty: TextView = itemView.findViewById(R.id.tv_shipment_product_qty)
        private val tvShipmentCustomer: TextView = itemView.findViewById(R.id.tv_shipment_customer)
        private val tvShipmentLocation: TextView = itemView.findViewById(R.id.tv_shipment_location)
        private val tvSeeMore: TextView = itemView.findViewById(R.id.tv_see_more)
        private val btnConfirmPayment: Button = itemView.findViewById(R.id.btn_confirm_payment)

        fun bind(order: OrdersItem) {
            tvShipmentNumber.text = "No. Pesanan: ${order.orderId}"
            tvShipmentDue.text = order.createdAt + 7
            tvShipmentCustomer.text = order.userId.toString()
            tvShipmentLocation.text = order.addressId.toString()
        }
    }
}