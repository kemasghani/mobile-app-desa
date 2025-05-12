package com.alya.ecommerce_serang.ui.profile.mystore.sells.all_sells

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.alya.ecommerce_serang.databinding.FragmentAllSellsBinding
import com.alya.ecommerce_serang.ui.profile.mystore.sells.SellsAdapter
import com.alya.ecommerce_serang.utils.viewmodel.SellsViewModel

class AllSellsFragment : Fragment() {
    private lateinit var viewModel: SellsViewModel
    private lateinit var binding: FragmentAllSellsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAllSellsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(SellsViewModel::class.java)

        val adapter = SellsAdapter()
        binding.rvAllSells.layoutManager = LinearLayoutManager(context)
        binding.rvAllSells.adapter = adapter

        viewModel.loadOrdersByStatus("all")
        viewModel.sellsList.observe(viewLifecycleOwner, Observer { sells ->
            adapter.submitList(sells)
        })
    }
}
