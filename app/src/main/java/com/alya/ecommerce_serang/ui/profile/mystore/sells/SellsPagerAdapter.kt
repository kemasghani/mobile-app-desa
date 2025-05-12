package com.alya.ecommerce_serang.ui.profile.mystore.sells

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.alya.ecommerce_serang.ui.profile.mystore.sells.all_sells.AllSellsFragment
import com.alya.ecommerce_serang.ui.profile.mystore.sells.cancellation.CancellationFragment
import com.alya.ecommerce_serang.ui.profile.mystore.sells.failed_payment.FailedPaymentFragment
import com.alya.ecommerce_serang.ui.profile.mystore.sells.failed_shipment.FailedShipmentFragment
import com.alya.ecommerce_serang.ui.profile.mystore.sells.completed.CompletedFragment
import com.alya.ecommerce_serang.ui.profile.mystore.sells.order.OrderFragment
import com.alya.ecommerce_serang.ui.profile.mystore.sells.payment.PaymentFragment
import com.alya.ecommerce_serang.ui.profile.mystore.sells.shipment.ShipmentFragment
import com.alya.ecommerce_serang.ui.profile.mystore.sells.shipped.ShippedFragment

class SellsPagerAdapter(fragment: Fragment, private val itemCount: Int) :
    FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = itemCount

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> AllSellsFragment()
            1 -> OrderFragment()
            2 -> PaymentFragment()
            3 -> ShipmentFragment()
            4 -> ShippedFragment()
            5 -> CompletedFragment()
            6 -> CancellationFragment()
            7 -> FailedPaymentFragment()
            8 -> FailedShipmentFragment()
            else -> Fragment()
        }
    }
}
