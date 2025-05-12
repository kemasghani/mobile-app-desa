package com.alya.ecommerce_serang.ui.order

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.alya.ecommerce_serang.R
import com.alya.ecommerce_serang.data.api.response.customer.product.PaymentInfoItem
import com.alya.ecommerce_serang.databinding.ItemPaymentMethodBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class PaymentMethodAdapter(
    private val paymentMethods: List<PaymentInfoItem>,
    private val onPaymentSelected: (PaymentInfoItem) -> Unit
) : RecyclerView.Adapter<PaymentMethodAdapter.PaymentMethodViewHolder>() {

    // Selected payment name
    private var selectedPaymentName: String? = null

    class PaymentMethodViewHolder(val binding: ItemPaymentMethodBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentMethodViewHolder {
        val binding = ItemPaymentMethodBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PaymentMethodViewHolder(binding)
    }

    override fun getItemCount(): Int = paymentMethods.size

    override fun onBindViewHolder(holder: PaymentMethodViewHolder, position: Int) {
        val payment = paymentMethods[position]

        with(holder.binding) {
            // Set payment method name
            tvPaymentMethodName.text = payment.name

//            // Set bank account number if available
//            if (!payment.bankNum.isNullOrEmpty()) {
//                tvPaymentAccountNumber.visibility = View.VISIBLE
//                tvPaymentAccountNumber.text = payment.bankNum
//            } else {
//                tvPaymentAccountNumber.visibility = View.GONE
//            }

            // Set radio button state based on selected payment name
            rbPaymentMethod.isChecked = payment.name == selectedPaymentName

            // Load payment icon if available
            if (!payment.qrisImage.isNullOrEmpty()) {
                Glide.with(ivPaymentMethod.context)
                    .load(payment.qrisImage)
                    .apply(
                        RequestOptions()
                        .placeholder(R.drawable.outline_store_24)
                        .error(R.drawable.outline_store_24))
                    .into(ivPaymentMethod)
            } else {
                // Default icon for bank transfers
                ivPaymentMethod.setImageResource(R.drawable.outline_store_24)
            }

            // Handle click on the entire item
            root.setOnClickListener {
                onPaymentSelected(payment)
                setSelectedPaymentName(payment.name)
            }

            // Handle click on the radio button
            rbPaymentMethod.setOnClickListener {
                onPaymentSelected(payment)
                setSelectedPaymentName(payment.name)
            }
        }
    }

    // Set selected payment by name and refresh the UI
    fun setSelectedPaymentName(paymentName: String) {
        selectedPaymentName = paymentName
        notifyDataSetChanged() // Update all items to reflect selection change
    }
}