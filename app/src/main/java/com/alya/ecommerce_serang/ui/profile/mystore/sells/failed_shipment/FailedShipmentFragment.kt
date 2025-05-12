package com.alya.ecommerce_serang.ui.profile.mystore.sells.failed_shipment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.alya.ecommerce_serang.R
import com.alya.ecommerce_serang.databinding.FragmentFailedShipmentBinding
import com.alya.ecommerce_serang.utils.viewmodel.SellsViewModel

class FailedShipmentFragment : Fragment() {
    private lateinit var viewModel: SellsViewModel
    private lateinit var binding: FragmentFailedShipmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFailedShipmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(SellsViewModel::class.java)

//        val adapter = SellsAdapter()
//        binding.rvFailedShipment.layoutManager = LinearLayoutManager(context)
//        binding.rvFailedShipment.adapter = adapter
//
//        viewModel.loadOrdersByStatus("failedShipment")
//        viewModel.sellsList.observe(viewLifecycleOwner, Observer { failedShipments ->
//            adapter.submitList(failedShipments)
//        })
    }
}