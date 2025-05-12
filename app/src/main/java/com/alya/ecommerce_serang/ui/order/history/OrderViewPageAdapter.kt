package com.alya.ecommerce_serang.ui.order.history

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class OrderViewPagerAdapter(
    fragmentActivity: FragmentActivity
) : FragmentStateAdapter(fragmentActivity) {

    // Define all possible order statuses
    private val orderStatuses = listOf(
        "all",        // All orders
        "pending",    // Menunggu Tagihan
        "unpaid",     // Belum Dibayar
        "processed",  // Diproses
        "paid",       // Dibayar
        "shipped",    // Dikirim
        "delivered",  // Diterima
        "completed",  // Selesai
        "canceled"    // Dibatalkan
    )

    override fun getItemCount(): Int = orderStatuses.size

    override fun createFragment(position: Int): Fragment {
        // Create a new instance of OrderListFragment with the appropriate status
        return OrderListFragment.newInstance(orderStatuses[position])
    }
}