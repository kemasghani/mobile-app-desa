package com.alya.ecommerce_serang.ui.order.address

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.alya.ecommerce_serang.data.api.retrofit.ApiConfig
import com.alya.ecommerce_serang.data.repository.OrderRepository
import com.alya.ecommerce_serang.databinding.ActivityAddressBinding
import com.alya.ecommerce_serang.utils.BaseViewModelFactory
import com.alya.ecommerce_serang.utils.SessionManager

class AddressActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddressBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: AddressAdapter

    private val viewModel: AddressViewModel by viewModels {
        BaseViewModelFactory {
            val apiService = ApiConfig.getApiService(sessionManager)
            val orderRepository = OrderRepository(apiService)
            AddressViewModel(orderRepository)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddressBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)


        setupToolbar()
        setupRecyclerView()
        setupObservers()

        viewModel.fetchAddresses()
    }

    private fun addAddressClicked(){
        binding.addAddressClick.setOnClickListener{
            val intent = Intent(this, AddAddressActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupToolbar() {
        // Remove duplicate toolbar setup
        addAddressClicked()
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedWithResult()
        }
    }

    private fun setupRecyclerView() {
        adapter = AddressAdapter { address ->
            // Select the address in the ViewModel
            viewModel.selectAddress(address.id)

            // Return immediately with the selected address
            returnResultAndFinish(address.id)
        }

        binding.rvSellerOrder.apply {
            layoutManager = LinearLayoutManager(this@AddressActivity)
            adapter = this@AddressActivity.adapter
        }
    }

    private fun setupObservers() {
        viewModel.addresses.observe(this) { addressList ->
            adapter.submitList(addressList)

            // Show empty state if needed
//            binding.emptyView?.isVisible = addressList.isEmpty()
            binding.rvSellerOrder.isVisible = addressList.isNotEmpty()
        }

        viewModel.selectedAddressId.observe(this) { selectedId ->
            adapter.setSelectedAddressId(selectedId)
        }
    }

    private fun onBackPressedWithResult() {
        // If an address is selected, return it as result
        val selectedId = viewModel.selectedAddressId.value
        if (selectedId != null) {
            returnResultAndFinish(selectedId)
            finish()
        } else {
            // No selection, just finish
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    private fun returnResultAndFinish(addressId: Int) {
        val intent = Intent()
        intent.putExtra(EXTRA_ADDRESS_ID, addressId)
        setResult(RESULT_OK, intent)
    }

    companion object {
        const val EXTRA_ADDRESS_ID = "extra_address_id"
    }
}

//override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//    super.onActivityResult(requestCode, resultCode, data)
//    if (requestCode == REQUEST_ADDRESS && resultCode == RESULT_OK) {
//        val selectedAddressId = data?.getIntExtra("selected_address_id", -1)
//        // Use the selected address ID
//    }
//}
