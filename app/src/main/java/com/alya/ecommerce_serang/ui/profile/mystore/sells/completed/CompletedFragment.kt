package com.alya.ecommerce_serang.ui.profile.mystore.sells.completed

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.alya.ecommerce_serang.R
import com.alya.ecommerce_serang.databinding.FragmentCompletedBinding
import com.alya.ecommerce_serang.utils.viewmodel.SellsViewModel

class CompletedFragment : Fragment() {
    private lateinit var viewModel: SellsViewModel
    private lateinit var binding: FragmentCompletedBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCompletedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(SellsViewModel::class.java)

//        val adapter = SellsAdapter()
//        binding.rvCompleted.layoutManager = LinearLayoutManager(context)
//        binding.rvCompleted.adapter = adapter
//
//        viewModel.loadOrdersByStatus("delivered")
//        viewModel.sellsList.observe(viewLifecycleOwner, Observer { completed ->
//            adapter.submitList(completed)
//        })
    }
}