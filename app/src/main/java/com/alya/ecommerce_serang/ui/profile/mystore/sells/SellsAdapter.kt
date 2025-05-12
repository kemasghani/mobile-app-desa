package com.alya.ecommerce_serang.ui.profile.mystore.sells

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.alya.ecommerce_serang.R
import com.alya.ecommerce_serang.data.api.dto.OrdersItem

class SellsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var orderList: List<OrdersItem?> = emptyList()

    // View Types for different statuses
    private val TYPE_PENDING = 0
    private val TYPE_PAYMENT = 1
    private val TYPE_SHIPMENT = 2
    private val TYPE_COMPLETED = 3
    private val TYPE_FAILED_PAYMENT = 4
    private val TYPE_FAILED_SHIPMENT = 5

    // Method to submit list to the adapter
    fun submitList(orders: List<OrdersItem?>?) {
        orderList = orders ?: emptyList()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_PENDING -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_sells_order, parent, false)
                OrderViewHolder(view)
            }
            TYPE_PAYMENT -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_sells_payment, parent, false)
                PaymentViewHolder(view)
            }
            TYPE_SHIPMENT -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_sells_shipment, parent, false)
                ShipmentViewHolder(view)
            }
//            TYPE_COMPLETED -> {
//                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_sells_completed, parent, false)
//                CompletedViewHolder(view)
//            }
//            TYPE_FAILED_PAYMENT -> {
//                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_sells_failed_payment, parent, false)
//                FailedPaymentViewHolder(view)
//            }
//            TYPE_FAILED_SHIPMENT -> {
//                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_sells_failed_shipment, parent, false)
//                FailedShipmentViewHolder(view)
//            }
            else -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_sells_order, parent, false)
                OrderViewHolder(view)
            }
        }
    }

    // Determine the view type based on the order status
    override fun getItemViewType(position: Int): Int {
        val order = orderList[position]
        return when (order?.shipmentStatus) {
            "pending" -> TYPE_PENDING
            "paid" -> TYPE_PAYMENT
            "shipped" -> TYPE_SHIPMENT
            "completed" -> TYPE_COMPLETED
            "failedPayment" -> TYPE_FAILED_PAYMENT
            "failedShipment" -> TYPE_FAILED_SHIPMENT
            else -> TYPE_PENDING // Default to pending if no status is matched
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val order = orderList[position]
        when (holder) {
            is OrderViewHolder -> holder.bind(order)
            is PaymentViewHolder -> holder.bind(order)
            is ShipmentViewHolder -> holder.bind(order)
            is CompletedViewHolder -> holder.bind(order)
            is FailedPaymentViewHolder -> holder.bind(order)
            is FailedShipmentViewHolder -> holder.bind(order)
        }
    }

    override fun getItemCount(): Int = orderList.size

    // ViewHolder for 'pending' status (Order)
    class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvOrderNumber: TextView = itemView.findViewById(R.id.tv_order_number)
        private val tvOrderCustomer: TextView = itemView.findViewById(R.id.tv_order_customer)
        private val tvOrderPrice: TextView = itemView.findViewById(R.id.tv_order_price)

        fun bind(order: OrdersItem?) {
            tvOrderNumber.text = "Order #${order?.orderId}"
            tvOrderCustomer.text = order?.userId.toString()
            tvOrderPrice.text = "Total: ${order?.totalAmount}"
        }
    }

    // ViewHolder for 'paid' status (Payment)
    class PaymentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvPaymentNumber: TextView = itemView.findViewById(R.id.tv_payment_number)
        private val tvPaymentCustomer: TextView = itemView.findViewById(R.id.tv_payment_customer)
        private val tvPaymentPrice: TextView = itemView.findViewById(R.id.tv_payment_price)

        fun bind(order: OrdersItem?) {
            tvPaymentNumber.text = "Order #${order?.orderId}"
            tvPaymentCustomer.text = order?.userId.toString()
            tvPaymentPrice.text = "Paid: ${order?.totalAmount}"
        }
    }

    // ViewHolder for 'shipped' status (Shipment)
    class ShipmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvShipmentNumber: TextView = itemView.findViewById(R.id.tv_shipment_number)
        private val tvShipmentLocation: TextView = itemView.findViewById(R.id.tv_shipment_location)

        fun bind(order: OrdersItem?) {
            tvShipmentNumber.text = "Shipment #${order?.orderId}"
            tvShipmentLocation.text = "Location: ${order?.addressId.toString()}"
        }
    }

    // ViewHolder for 'completed' status
    class CompletedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //private val tvCompletedNumber: TextView = itemView.findViewById(R.id.tv_completed_number)

        fun bind(order: OrdersItem?) {
          //  tvCompletedNumber.text = "Completed Order #${order?.orderId}"
        }
    }

    // ViewHolder for 'failedPayment' status
    class FailedPaymentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //private val tvFailedPaymentNumber: TextView = itemView.findViewById(R.id.tv_failed_payment_number)

        fun bind(order: OrdersItem?) {
            //tvFailedPaymentNumber.text = "Failed Payment Order #${order?.orderId}"
        }
    }

    // ViewHolder for 'failedShipment' status
    class FailedShipmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //private val tvFailedShipmentNumber: TextView = itemView.findViewById(R.id.tv_failed_shipment_number)

        fun bind(order: OrdersItem?) {
            //tvFailedShipmentNumber.text = "Failed Shipment Order #${order?.orderId}"
        }
    }
}