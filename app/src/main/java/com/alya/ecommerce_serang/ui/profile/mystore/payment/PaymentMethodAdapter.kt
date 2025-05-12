package com.alya.ecommerce_serang.ui.profile.mystore.payment

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.alya.ecommerce_serang.R
import com.alya.ecommerce_serang.data.api.dto.PaymentMethod
import com.bumptech.glide.Glide

class PaymentMethodAdapter(
    private val onDeleteClick: (PaymentMethod) -> Unit
) : ListAdapter<PaymentMethod, PaymentMethodAdapter.PaymentMethodViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentMethodViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_payment_method, parent, false)
        return PaymentMethodViewHolder(view)
    }

    override fun onBindViewHolder(holder: PaymentMethodViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PaymentMethodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvBankName: TextView = itemView.findViewById(R.id.tv_bank_name)
        private val tvAccountName: TextView = itemView.findViewById(R.id.tv_account_name)
        private val tvBankNumber: TextView = itemView.findViewById(R.id.tv_bank_number)
        private val ivDelete: ImageView = itemView.findViewById(R.id.iv_delete)
        private val layoutQris: LinearLayout = itemView.findViewById(R.id.layout_qris)
        private val ivQris: ImageView = itemView.findViewById(R.id.iv_qris)

        fun bind(paymentMethod: PaymentMethod) {
            tvBankName.text = paymentMethod.bankName
            tvAccountName.text = paymentMethod.accountName ?: ""
            tvBankNumber.text = paymentMethod.bankNum

            // Handle QRIS image if available
            if (paymentMethod.qrisImage != null && paymentMethod.qrisImage.isNotEmpty() && paymentMethod.qrisImage != "null") {
                layoutQris.visibility = View.VISIBLE
                // Make sure the URL is correct by handling both relative and absolute paths
                val imageUrl = if (paymentMethod.qrisImage.startsWith("http")) {
                    paymentMethod.qrisImage
                } else {
                    "http://192.168.100.31:3000${paymentMethod.qrisImage}"
                }

                Log.d("PaymentMethodAdapter", "Loading QRIS image from: $imageUrl")

                Glide.with(itemView.context)
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.placeholder_image)
                    .into(ivQris)
            } else {
                layoutQris.visibility = View.GONE
            }

            ivDelete.setOnClickListener {
                onDeleteClick(paymentMethod)
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<PaymentMethod>() {
            override fun areItemsTheSame(oldItem: PaymentMethod, newItem: PaymentMethod): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: PaymentMethod, newItem: PaymentMethod): Boolean {
                return oldItem == newItem
            }
        }
    }
}