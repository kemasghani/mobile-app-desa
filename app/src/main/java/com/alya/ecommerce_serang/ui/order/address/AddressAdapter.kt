package com.alya.ecommerce_serang.ui.order.address

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.alya.ecommerce_serang.R
import com.alya.ecommerce_serang.data.api.response.customer.profile.AddressesItem
import com.google.android.material.card.MaterialCardView

class AddressAdapter(
    private val onAddressClick: (AddressesItem) -> Unit
) : ListAdapter<AddressesItem, AddressAdapter.AddressViewHolder>(DIFF_CALLBACK) {

    private var selectedAddressId: Int? = null

    fun setSelectedAddressId(id: Int?) {
        val oldSelectedId = selectedAddressId
        selectedAddressId = id

        // Only refresh the changed items
        if (oldSelectedId != null) {
            val oldPosition = currentList.indexOfFirst { it.id == oldSelectedId }
            if (oldPosition >= 0) notifyItemChanged(oldPosition)
        }

        if (id != null) {
            val newPosition = currentList.indexOfFirst { it.id == id }
            if (newPosition >= 0) notifyItemChanged(newPosition)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddressViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_card_address, parent, false)
        return AddressViewHolder(view)
    }

    override fun onBindViewHolder(holder: AddressViewHolder, position: Int) {
        val address = getItem(position)
        holder.bind(address, selectedAddressId == address.id)
        holder.itemView.setOnClickListener {
            // Pass the whole address object to provide more context
            onAddressClick(address)
        }
    }

    class AddressViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tv_name_address)
        private val tvDetail: TextView = itemView.findViewById(R.id.tv_detail_address)
        private val card: MaterialCardView = itemView as MaterialCardView

        fun bind(address: AddressesItem, isSelected: Boolean) {
            tvName.text = address.recipient
            tvDetail.text = "${address.street}, ${address.subdistrict}, ${address.phone}"

            // Make selection more visible
            card.strokeWidth = if (isSelected) 3 else 0
            card.strokeColor = if (isSelected)
                ContextCompat.getColor(itemView.context, R.color.blue_400)
            else 0

            card.setCardBackgroundColor(
                ContextCompat.getColor(
                    itemView.context,
                    if (isSelected) R.color.blue_50 else R.color.white
                )
            )
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<AddressesItem>() {
            override fun areItemsTheSame(oldItem: AddressesItem, newItem: AddressesItem) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: AddressesItem, newItem: AddressesItem) =
                oldItem == newItem
        }
    }
}
