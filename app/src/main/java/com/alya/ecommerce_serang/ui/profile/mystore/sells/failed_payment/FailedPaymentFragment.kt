
package com.alya.ecommerce_serang.ui.profile.mystore.sells.failed_payment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.alya.ecommerce_serang.R
import com.alya.ecommerce_serang.databinding.FragmentFailedPaymentBinding
import com.alya.ecommerce_serang.utils.viewmodel.SellsViewModel

class FailedPaymentFragment : Fragment() {
    private lateinit var viewModel: SellsViewModel
    private lateinit var binding: FragmentFailedPaymentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFailedPaymentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(SellsViewModel::class.java)

//        val adapter = SellsAdapter()
//        binding.rvFailedPayment.layoutManager = LinearLayoutManager(context)
//        binding.rvFailedPayment.adapter = adapter
//
//        viewModel.loadOrdersByStatus("failedPayment")
//        viewModel.sellsList.observe(viewLifecycleOwner, Observer { failedPayments ->
//            adapter.submitList(failedPayments)
//        })
    }
}