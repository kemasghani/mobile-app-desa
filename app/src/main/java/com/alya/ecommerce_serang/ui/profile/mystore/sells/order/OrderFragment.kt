package com.alya.ecommerce_serang.ui.profile.mystore.sells.order

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.alya.ecommerce_serang.databinding.FragmentOrderBinding
import com.alya.ecommerce_serang.utils.viewmodel.SellsViewModel

class OrderFragment : Fragment() {
    private lateinit var viewModel: SellsViewModel
    private lateinit var binding: FragmentOrderBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentOrderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(SellsViewModel::class.java)
        val adapter = OrderAdapter()

        binding.rvOrder.layoutManager = LinearLayoutManager(context)
        binding.rvOrder.adapter = adapter

        viewModel.loadOrdersByStatus("pending")
        viewModel.sellsList.observe(viewLifecycleOwner, Observer { orders ->
            adapter.submitList(orders)
        })
    }
}