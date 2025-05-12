package com.alya.ecommerce_serang.ui.profile.mystore.sells.cancellation

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.alya.ecommerce_serang.R
import com.alya.ecommerce_serang.databinding.FragmentCancellationBinding
import com.alya.ecommerce_serang.utils.viewmodel.SellsViewModel

class CancellationFragment : Fragment() {
    private lateinit var viewModel: SellsViewModel
    private lateinit var binding: FragmentCancellationBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCancellationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(SellsViewModel::class.java)

//        val adapter = SellsAdapter()
//        binding.rvCancellation.layoutManager = LinearLayoutManager(context)
//        binding.rvCancellation.adapter = adapter
//
//        viewModel.loadOrdersByStatus("cancelled")
//        viewModel.sellsList.observe(viewLifecycleOwner, Observer { cancellations ->
//            adapter.submitList(cancellations)
//        })
    }
}