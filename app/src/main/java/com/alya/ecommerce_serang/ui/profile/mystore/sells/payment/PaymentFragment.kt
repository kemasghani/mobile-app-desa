package com.alya.ecommerce_serang.ui.profile.mystore.sells.payment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.alya.ecommerce_serang.databinding.FragmentPaymentBinding
import com.alya.ecommerce_serang.utils.viewmodel.SellsViewModel

class PaymentFragment : Fragment() {
    private lateinit var viewModel: SellsViewModel
    private lateinit var binding: FragmentPaymentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPaymentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(SellsViewModel::class.java)
        val adapter = PaymentAdapter()

        binding.rvPayment.layoutManager = LinearLayoutManager(context)
        binding.rvPayment.adapter = adapter

        viewModel.loadOrdersByStatus("paid")
        viewModel.sellsList.observe(viewLifecycleOwner, Observer { payments ->
            adapter.submitList(payments)
        })
    }
}