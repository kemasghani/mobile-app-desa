package com.alya.ecommerce_serang.ui.profile.mystore.sells

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2

import com.google.android.material.tabs.TabLayoutMediator
import com.alya.ecommerce_serang.R
import com.alya.ecommerce_serang.utils.viewmodel.SellsViewModel

class SellsFragment : Fragment() {
    private lateinit var viewModel: SellsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sells, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        val repository = OrderRepository(ApiService.create())
        viewModel = ViewModelProvider(this)[SellsViewModel::class.java]

        val tabs = listOf(
            "Semua Pesanan", "Perlu Tagihan", "Konfirmasi Pembayaran",
            "Perlu Dikirim", "Dikirim", "Selesai",
            "Pembatalan", "Klaim Pembayaran", "Pengiriman Gagal"
        )

        val adapter = SellsPagerAdapter(this, tabs.size)
        val viewPager: ViewPager2 = view.findViewById(R.id.view_pager_sells)
        viewPager.adapter = adapter

        TabLayoutMediator(view.findViewById(R.id.tab_layout_sells), viewPager) { tab, position ->
            tab.text = tabs[position]
        }.attach()
    }
}
