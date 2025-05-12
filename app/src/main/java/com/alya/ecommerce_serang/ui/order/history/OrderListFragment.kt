package com.alya.ecommerce_serang.ui.order.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.alya.ecommerce_serang.data.api.dto.OrdersItem
import com.alya.ecommerce_serang.data.api.retrofit.ApiConfig
import com.alya.ecommerce_serang.data.repository.OrderRepository
import com.alya.ecommerce_serang.data.repository.Result
import com.alya.ecommerce_serang.databinding.FragmentOrderListBinding
import com.alya.ecommerce_serang.ui.order.address.ViewState
import com.alya.ecommerce_serang.utils.BaseViewModelFactory
import com.alya.ecommerce_serang.utils.SessionManager

class OrderListFragment : Fragment() {

    private var _binding: FragmentOrderListBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager


    private val viewModel: HistoryViewModel by viewModels {
        BaseViewModelFactory {
            val apiService = ApiConfig.getApiService(sessionManager)
            val orderRepository = OrderRepository(apiService)
            HistoryViewModel(orderRepository)
        }
    }
    private lateinit var orderAdapter: OrderHistoryAdapter

    private var status: String = "all"

    companion object {
        private const val ARG_STATUS = "status"

        fun newInstance(status: String): OrderListFragment {
            return OrderListFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_STATUS, status)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sessionManager = SessionManager(requireContext())
        arguments?.let {
            status = it.getString(ARG_STATUS) ?: "all"
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeOrderList()
        observeOrderCompletionStatus()
        loadOrders()
    }

    private fun setupRecyclerView() {
        orderAdapter = OrderHistoryAdapter(
            onOrderClickListener = { order ->
                // Handle order click
                navigateToOrderDetail(order)
            },
            viewModel = viewModel // Pass the ViewModel to the adapter
        )

        orderAdapter.setFragmentStatus(status)

        binding.rvOrders.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = orderAdapter
        }
    }

    private fun observeOrderList() {
        viewModel.orders.observe(viewLifecycleOwner) { result ->
            when (result) {
                is ViewState.Success -> {
                    binding.progressBar.visibility = View.GONE

                    if (result.data.isNullOrEmpty()) {
                        binding.tvEmptyState.visibility = View.VISIBLE
                        binding.rvOrders.visibility = View.GONE
                    } else {
                        binding.tvEmptyState.visibility = View.GONE
                        binding.rvOrders.visibility = View.VISIBLE
                        orderAdapter.submitList(result.data)
                    }
                }
                is ViewState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.tvEmptyState.visibility = View.VISIBLE
                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
                }
                is ViewState.Loading -> {
                    null
                }
            }
        }
    }

    private fun loadOrders() {
        viewModel.getOrderList(status)
    }

    private fun navigateToOrderDetail(order: OrdersItem) {
        // In a real app, you would navigate to order detail screen
        // For example: findNavController().navigate(OrderListFragmentDirections.actionToOrderDetail(order.orderId))
        Toast.makeText(requireContext(), "Order ID: ${order.orderId}", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun observeOrderCompletionStatus(){
        viewModel.orderCompletionStatus.observe(viewLifecycleOwner){ result ->
            when(result){
                is Result.Loading -> {

                }
                is Result.Success -> {
                    Toast.makeText(requireContext(), "Order completed successfully!", Toast.LENGTH_SHORT).show()
                    loadOrders()
                }
                is Result.Error -> {
                    Toast.makeText(requireContext(), "Failed to complete order: ${result.exception.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}