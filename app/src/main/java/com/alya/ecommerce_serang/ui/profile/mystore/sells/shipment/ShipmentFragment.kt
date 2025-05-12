package com.alya.ecommerce_serang.ui.profile.mystore.sells.shipment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.alya.ecommerce_serang.R
import com.alya.ecommerce_serang.databinding.FragmentShipmentBinding
import com.alya.ecommerce_serang.utils.viewmodel.SellsViewModel

class ShipmentFragment : Fragment() {
    private lateinit var viewModel: SellsViewModel
    private lateinit var binding: FragmentShipmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentShipmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(SellsViewModel::class.java)
        val adapter = ShipmentAdapter()

        binding.rvShipment.layoutManager = LinearLayoutManager(context)
//        binding.rvShipment.adapter = adapter
//
//        viewModel.loadOrdersByStatus("processed")
//        viewModel.sellsList.observe(viewLifecycleOwner, Observer { shipments ->
//            adapter.submitList(shipments)
//        })
    }
}