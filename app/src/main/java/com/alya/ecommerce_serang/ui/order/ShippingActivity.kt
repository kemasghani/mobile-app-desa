package com.alya.ecommerce_serang.ui.order

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.alya.ecommerce_serang.data.api.retrofit.ApiConfig
import com.alya.ecommerce_serang.data.repository.OrderRepository
import com.alya.ecommerce_serang.databinding.ActivityShippingBinding
import com.alya.ecommerce_serang.utils.BaseViewModelFactory
import com.alya.ecommerce_serang.utils.SessionManager

class ShippingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityShippingBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var shippingAdapter: ShippingAdapter
    private val TAG = "ShippingActivity"

    private val viewModel: ShippingViewModel by viewModels {
        BaseViewModelFactory {
            val apiService = ApiConfig.getApiService(sessionManager)
            val repository = OrderRepository(apiService)
            ShippingViewModel(repository)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShippingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize SessionManager
        sessionManager = SessionManager(this)

        // Get data from intent
        val addressId = intent.getIntExtra(EXTRA_ADDRESS_ID, 0)
        val productId = intent.getIntExtra(EXTRA_PRODUCT_ID, 0)
        val quantity = intent.getIntExtra(EXTRA_QUANTITY, 1)

        Log.d(TAG, "Received data: addressId=$addressId, productId=$productId, quantity=$quantity")

        // Validate required information
        if (addressId <= 0 || productId <= 0) {
            Log.e(TAG, "Missing required shipping information: addressId=$addressId, productId=$productId")
            Toast.makeText(this, "Missing required shipping information", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Setup UI components
        setupToolbar()
        setupRecyclerView()
        setupObservers()
        setupRetryButton() // Add a retry button for error cases

        // Load shipping options
        loadShippingOptions(addressId, productId, quantity)
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        shippingAdapter = ShippingAdapter { courierCostsItem, service ->
            // Handle shipping method selection
            Log.d(TAG, "Selected shipping: ${courierCostsItem.courier} - ${service.service} - ${service.cost} - ${service.etd}")
            returnSelectedShipping(
                courierCostsItem.courier,
                service.service,
                service.cost,
                service.etd
            )
        }

        binding.rvShipmentOrder.apply {
            layoutManager = LinearLayoutManager(this@ShippingActivity)
            adapter = shippingAdapter
        }
    }

    private fun setupRetryButton() {
        // If you have a retry button in your layout
//        binding.btnRetry?.setOnClickListener {
//            val addressId = intent.getIntExtra(EXTRA_ADDRESS_ID, 0)
//            val productId = intent.getIntExtra(EXTRA_PRODUCT_ID, 0)
//            val quantity = intent.getIntExtra(EXTRA_QUANTITY, 1)
//            loadShippingOptions(addressId, productId, quantity)
//        }
    }

    private fun loadShippingOptions(addressId: Int, productId: Int, quantity: Int) {
        // Show loading state
        binding.progressBar?.visibility = View.VISIBLE
        binding.rvShipmentOrder.visibility = View.GONE
//        binding.layoutEmptyShipping?.visibility = View.GONE

        // Load shipping options
        Log.d(TAG, "Loading shipping options: addressId=$addressId, productId=$productId, quantity=$quantity")
        viewModel.loadShippingOptions(addressId, productId, quantity)
    }

    private fun setupObservers() {
        // Observe shipping options
        viewModel.shippingOptions.observe(this) { courierOptions ->
            Log.d(TAG, "Received ${courierOptions.size} shipping options")
            shippingAdapter.submitList(courierOptions)
            updateEmptyState(courierOptions.isEmpty() || courierOptions.all { it.services.isEmpty() })
        }

        // Observe loading state
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar?.visibility = if (isLoading) View.VISIBLE else View.GONE
            Log.d(TAG, "Loading state: $isLoading")
        }

        // Observe error messages
        viewModel.errorMessage.observe(this) { message ->
            if (message.isNotEmpty()) {
                Log.e(TAG, "Error: $message")
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

                // Show error view if you have one
//                binding.layoutError?.visibility = View.VISIBLE
//                binding.tvErrorMessage?.text = message
            } else {
//                binding.layoutError?.visibility = View.GONE
            }
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        Log.d(TAG, "Updating empty state: isEmpty=$isEmpty")
//        binding.layoutEmptyShipping?.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.rvShipmentOrder.visibility = if (isEmpty) View.GONE else View.VISIBLE

        // If empty, show appropriate message
        if (isEmpty) {
//            binding.tvEmptyMessage?.text = "No shipping options available for this address and product"
        }
    }

    private fun returnSelectedShipping(
        shipName: String,
        shipService: String,
        shipPrice: Int,
        shipEtd: String
    ) {
        val intent = Intent().apply {
            putExtra(EXTRA_SHIP_NAME, shipName)
            putExtra(EXTRA_SHIP_SERVICE, shipService)
            putExtra(EXTRA_SHIP_PRICE, shipPrice)
            putExtra(EXTRA_SHIP_ETD, shipEtd)
        }
        Log.d(TAG, "Returning selected shipping: name=$shipName, service=$shipService, price=$shipPrice, etd=$shipEtd")
        setResult(RESULT_OK, intent)
        finish()
    }

    companion object {

        // Constants for intent extras
        const val EXTRA_ADDRESS_ID = "extra_address_id"
        const val EXTRA_PRODUCT_ID = "extra_product_id"
        const val EXTRA_QUANTITY = "extra_quantity"
        const val EXTRA_SHIP_NAME = "extra_ship_name"
        const val EXTRA_SHIP_SERVICE = "extra_ship_service"
        const val EXTRA_SHIP_PRICE = "extra_ship_price"
        const val EXTRA_SHIP_ETD = "extra_ship_etd"
    }
}

//val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//    if (result.resultCode == RESULT_OK) {
//        val data = result.data
//        val shipName = data?.getStringExtra("ship_name")
//        val shipPrice = data?.getIntExtra("ship_price", 0)
//        val shipService = data?.getStringExtra("ship_service")
//        // use the data as needed
//    }
//}
//
//// launch the shipping activity
//val intent = Intent(this, ShippingActivity::class.java).apply {
//    putExtra("address_id", addressId)
//    putExtra("product_id", productId)
//    putExtra("quantity", quantity)
//}
//launcher.launch(intent)
