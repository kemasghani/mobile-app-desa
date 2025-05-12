package com.alya.ecommerce_serang.ui.profile.mystore.sells.payment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.alya.ecommerce_serang.R
import com.alya.ecommerce_serang.data.api.dto.OrdersItem

class PaymentAdapter : RecyclerView.Adapter<PaymentAdapter.PaymentViewHolder>() {

    private var paymentList: List<OrdersItem?>? = emptyList()

    fun submitList(orders: List<OrdersItem?>?) {
        paymentList = orders
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PaymentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_sells_payment, parent, false)
        return PaymentViewHolder(view)
    }

    override fun onBindViewHolder(holder: PaymentViewHolder, position: Int) {
        val order = paymentList?.get(position)
        holder.bind(order)
    }

    override fun getItemCount(): Int = paymentList?.size ?: 0

    class PaymentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        private val tvPaymentNumber: TextView = itemView.findViewById(R.id.tv_payment_number)
        private val tvPaymentDue: TextView = itemView.findViewById(R.id.tv_payment_due)
        private val ivPaymentProduct: ImageView = itemView.findViewById(R.id.iv_payment_product)
        private val tvPaymentProductName: TextView = itemView.findViewById(R.id.tv_payment_product_name)
        private val tvPaymentProductVariant: TextView = itemView.findViewById(R.id.tv_payment_product_variant)
        private val tvPaymentProductQty: TextView = itemView.findViewById(R.id.tv_payment_product_qty)
        private val tvPaymentProductPrice: TextView = itemView.findViewById(R.id.tv_payment_product_price)
        private val tvPaymentQty: TextView = itemView.findViewById(R.id.tv_payment_qty)
        private val tvPaymentPrice: TextView = itemView.findViewById(R.id.tv_payment_price)
        private val tvPaymentCustomer: TextView = itemView.findViewById(R.id.tv_payment_customer)
        private val tvPaymentLocation: TextView = itemView.findViewById(R.id.tv_payment_location)
        private val tvSeeMore: TextView = itemView.findViewById(R.id.tv_see_more)
        private val btnConfirmPayment: Button = itemView.findViewById(R.id.btn_confirm_payment)

        fun bind(order: OrdersItem?) {
            tvPaymentNumber.text = "No. Pesanan: ${order?.orderId}"
            tvPaymentDue.text = order?.createdAt + 7
            tvPaymentQty.text = "${order?.orderItems?.size} produk"
            tvPaymentPrice.text = "Rp${order?.totalAmount}"
            tvPaymentCustomer.text = order?.userId.toString()
            tvPaymentLocation.text = order?.addressId.toString()
        }
    }
}