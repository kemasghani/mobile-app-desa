package com.alya.ecommerce_serang.ui.order

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.alya.ecommerce_serang.data.api.response.customer.order.CourierCostsItem
import com.alya.ecommerce_serang.data.api.response.customer.order.ServicesItem
import com.alya.ecommerce_serang.databinding.ItemShippingOrderBinding

class ShippingAdapter(
    private val onItemSelected: (CourierCostsItem, ServicesItem) -> Unit
) : RecyclerView.Adapter<ShippingAdapter.ShippingViewHolder>() {

    private val courierCostsList = mutableListOf<CourierCostsItem>()
    private var selectedPosition = RecyclerView.NO_POSITION
    private var selectedCourierPosition = RecyclerView.NO_POSITION

    fun submitList(courierCostsList: List<CourierCostsItem>) {
        this.courierCostsList.clear()
        this.courierCostsList.addAll(courierCostsList)
        notifyDataSetChanged()
    }

    inner class ShippingViewHolder(
        private val binding: ItemShippingOrderBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(courierCostsItem: CourierCostsItem, service: ServicesItem, isSelected: Boolean) {
            binding.apply {
                // Combine courier name and service
                courierNameCost.text = "${courierCostsItem.courier} - ${service.service}"
                estDate.text = "Estimasi ${service.etd} hari"
                costPrice.text = "Rp${service.cost}"

                // Single click handler for both item and radio button
                val onClickAction = {
                    val newPosition = adapterPosition
                    if (newPosition != RecyclerView.NO_POSITION) {
                        // Update selected position
                        val oldPosition = selectedPosition
                        selectedPosition = newPosition
                        selectedCourierPosition = getParentCourierPosition(courierCostsItem)

                        // Notify only the changed items to improve performance
                        notifyItemChanged(oldPosition)
                        notifyItemChanged(newPosition)

                        // Call the callback with both courier and service
                        onItemSelected(courierCostsItem, service)
                    }
                }

                root.setOnClickListener { onClickAction() }
                radioBtnCost.apply {
                    isChecked = isSelected
                    setOnClickListener { onClickAction() }
                }
            }
        }
    }

    private fun getParentCourierPosition(courierCostsItem: CourierCostsItem): Int {
        return courierCostsList.indexOfFirst { it == courierCostsItem }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShippingViewHolder {
        val binding = ItemShippingOrderBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ShippingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ShippingViewHolder, position: Int) {
        // Flatten the nested structure for binding
        var currentPosition = 0
        for (courierCostsItem in courierCostsList) {
            for (service in courierCostsItem.services) {
                if (currentPosition == position) {
                    holder.bind(
                        courierCostsItem,
                        service,
                        currentPosition == selectedPosition
                    )
                    return
                }
                currentPosition++
            }
        }
    }

    override fun getItemCount(): Int {
        return courierCostsList.sumOf { it.services.size }
    }
}
