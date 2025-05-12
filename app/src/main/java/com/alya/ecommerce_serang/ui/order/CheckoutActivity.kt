package com.alya.ecommerce_serang.ui.order

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alya.ecommerce_serang.data.api.dto.CheckoutData
import com.alya.ecommerce_serang.data.api.dto.OrderRequest
import com.alya.ecommerce_serang.data.api.dto.OrderRequestBuy
import com.alya.ecommerce_serang.data.api.response.customer.product.PaymentInfoItem
import com.alya.ecommerce_serang.data.api.retrofit.ApiConfig
import com.alya.ecommerce_serang.data.repository.OrderRepository
import com.alya.ecommerce_serang.databinding.ActivityCheckoutBinding
import com.alya.ecommerce_serang.ui.order.address.AddressActivity
import com.alya.ecommerce_serang.utils.BaseViewModelFactory
import com.alya.ecommerce_serang.utils.SessionManager
import java.text.NumberFormat
import java.util.Locale

class CheckoutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCheckoutBinding
    private lateinit var sessionManager: SessionManager
    private var paymentAdapter: PaymentMethodAdapter? = null

    private val viewModel: CheckoutViewModel by viewModels {
        BaseViewModelFactory {
            val apiService = ApiConfig.getApiService(sessionManager)
            val orderRepository = OrderRepository(apiService)
            CheckoutViewModel(orderRepository)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)


        // Setup UI components
        setupToolbar()
        setupObservers()
        setupClickListeners()
        processIntentData()
    }

    private fun processIntentData() {
        // Determine if this is Buy Now or Cart checkout
        val isBuyNow = intent.hasExtra(EXTRA_PRODUCT_ID) && !intent.hasExtra(EXTRA_CART_ITEM_IDS)

        if (isBuyNow) {
            // Process Buy Now flow
            viewModel.initializeBuyNow(
                storeId = intent.getIntExtra(EXTRA_STORE_ID, 0),
                storeName = intent.getStringExtra(EXTRA_STORE_NAME),
                productId = intent.getIntExtra(EXTRA_PRODUCT_ID, 0),
                productName = intent.getStringExtra(EXTRA_PRODUCT_NAME),
                productImage = intent.getStringExtra(EXTRA_PRODUCT_IMAGE),
                quantity = intent.getIntExtra(EXTRA_QUANTITY, 1),
                price = intent.getDoubleExtra(EXTRA_PRICE, 0.0)
            )
        } else {
            // Process Cart checkout flow
            val cartItemIds = intent.getIntArrayExtra(EXTRA_CART_ITEM_IDS)?.toList() ?: emptyList()
            if (cartItemIds.isNotEmpty()) {
                viewModel.initializeFromCart(cartItemIds)
            } else {
                Toast.makeText(this, "Error: No cart items specified", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        viewModel.getPaymentMethods { paymentMethods ->
            // Logging is just for debugging
            Log.d("CheckoutActivity", "Loaded ${paymentMethods.size} payment methods")
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupObservers() {
        // Observe checkout data
        viewModel.checkoutData.observe(this) { data ->
            setupProductRecyclerView(data)
            updateOrderSummary()
        }

        // Observe address details
        viewModel.addressDetails.observe(this) { address ->
            binding.tvPlacesAddress.text = address?.recipient
            binding.tvAddress.text = "${address?.street}, ${address?.subdistrict}"
        }

        viewModel.availablePaymentMethods.observe(this) { paymentMethods ->
            if (paymentMethods.isNotEmpty()) {
                setupPaymentMethodsRecyclerView(paymentMethods)
            }
        }

// Observe selected payment
        viewModel.selectedPayment.observe(this) { selectedPayment ->
            if (selectedPayment != null) {
                // Update the adapter to show the selected payment
                paymentAdapter?.setSelectedPaymentName(selectedPayment.name)

                // Optional: Update other UI elements to show the selected payment
                // For example: binding.tvSelectedPaymentMethod.text = selectedPayment.name
            }
        }


        // Observe loading state
        viewModel.isLoading.observe(this) { isLoading ->
            binding.btnPay.isEnabled = !isLoading
            // Show/hide loading indicator if you have one
        }

        // Observe error messages
        viewModel.errorMessage.observe(this) { message ->
            if (message.isNotEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        }

        // Observe order creation
        viewModel.orderCreated.observe(this) { created ->
            if (created) {
                Toast.makeText(this, "Order successfully created!", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
            }
        }
    }

    private fun setupPaymentMethodsRecyclerView(paymentMethods: List<PaymentInfoItem>) {
        if (paymentMethods.isEmpty()) {
            Log.e("CheckoutActivity", "Payment methods list is empty")
            Toast.makeText(this, "No payment methods available", Toast.LENGTH_SHORT).show()
            return
        }

        // Debug logging
        Log.d("CheckoutActivity", "Setting up payment methods: ${paymentMethods.size} methods available")

        paymentAdapter = PaymentMethodAdapter(paymentMethods) { payment ->
            // We're using a hardcoded ID for now
            viewModel.setPaymentMethod(1)
        }

        binding.rvPaymentMethods.apply {
            layoutManager = LinearLayoutManager(this@CheckoutActivity)
            adapter = paymentAdapter
        }
    }

    private fun updatePaymentMethodsAdapter(paymentMethods: List<PaymentInfoItem>, selectedId: Int?) {
        Log.d("CheckoutActivity", "Updating payment adapter with ${paymentMethods.size} methods")

        // Simple test adapter
        val testAdapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                val textView = TextView(parent.context)
                textView.setPadding(16, 16, 16, 16)
                textView.textSize = 16f
                return object : RecyclerView.ViewHolder(textView) {}
            }

            override fun getItemCount() = paymentMethods.size

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                val payment = paymentMethods[position]
                (holder.itemView as TextView).text = "Payment: ${payment.name}"
            }
        }

        binding.rvPaymentMethods.apply {
            layoutManager = LinearLayoutManager(this@CheckoutActivity)
            adapter = testAdapter
        }
    }

    private fun setupProductRecyclerView(checkoutData: CheckoutData) {
        val adapter = if (checkoutData.isBuyNow || checkoutData.cartItems.size <= 1) {
            CheckoutSellerAdapter(checkoutData)
        } else {
            CartCheckoutAdapter(checkoutData)
        }

        binding.rvProductItems.apply {
            layoutManager = LinearLayoutManager(this@CheckoutActivity)
            this.adapter = adapter
            isNestedScrollingEnabled = false
        }
    }

    private fun updateOrderSummary() {
        viewModel.checkoutData.value?.let { data ->
            // Update price information
            binding.tvItemTotal.text = formatCurrency(viewModel.calculateSubtotal())

            // Get shipping price
            val shipPrice = if (data.isBuyNow) {
                (data.orderRequest as OrderRequestBuy).shipPrice.toDouble()
            } else {
                (data.orderRequest as OrderRequest).shipPrice.toDouble()
            }
            binding.tvShippingFee.text = formatCurrency(shipPrice)

            // Update total
            val total = viewModel.calculateTotal()
            binding.tvTotal.text = formatCurrency(total)
            binding.tvBottomTotal.text = formatCurrency(total)
        }
    }

    private fun updateShippingUI(shipName: String, shipService: String, shipEtd: String, shipPrice: Int) {
        if (shipName.isNotEmpty() && shipService.isNotEmpty()) {
            // Display shipping name and service in one line
            binding.tvCourierName.text = "$shipName $shipService"
            binding.tvDeliveryEstimate.text = "$shipEtd hari kerja"
            binding.tvShippingPrice.text = formatCurrency(shipPrice.toDouble())
            binding.rbJne.isChecked = true
        }
    }

    private fun setupClickListeners() {
        // Address selection
        binding.tvChangeAddress.setOnClickListener {
            val intent = Intent(this, AddressActivity::class.java)
            addressSelectionLauncher.launch(intent)
        }

        // Shipping method selection
        binding.layoutShippingMethod.setOnClickListener {
            val addressId = viewModel.addressDetails.value?.id ?: 0
            if (addressId <= 0) {
                Toast.makeText(this, "Please select delivery address first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Launch shipping selection with address and product info
            val intent = Intent(this, ShippingActivity::class.java)
            intent.putExtra(ShippingActivity.EXTRA_ADDRESS_ID, addressId)

            // Add product info for courier cost calculation
            val currentData = viewModel.checkoutData.value
            if (currentData != null) {
                if (currentData.isBuyNow) {
                    val buyRequest = currentData.orderRequest as OrderRequestBuy
                    intent.putExtra(ShippingActivity.EXTRA_PRODUCT_ID, buyRequest.productId)
                    intent.putExtra(ShippingActivity.EXTRA_QUANTITY, buyRequest.quantity)
                } else {
                    // For cart, we'll pass the first item's info
                    val firstItem = currentData.cartItems.firstOrNull()
                    if (firstItem != null) {
                        intent.putExtra(ShippingActivity.EXTRA_PRODUCT_ID, firstItem.productId)
                        intent.putExtra(ShippingActivity.EXTRA_QUANTITY, firstItem.quantity)
                    }
                }
            }

            shippingSelectionLauncher.launch(intent)
        }

        // Create order button
        binding.btnPay.setOnClickListener {
            if (validateOrder()) {
                viewModel.createOrder()
            }
        }

        // Voucher section (if implemented)
        binding.layoutVoucher?.setOnClickListener {
            Toast.makeText(this, "Voucher feature not implemented", Toast.LENGTH_SHORT).show()
        }
    }

    private val addressSelectionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val addressId = result.data?.getIntExtra(AddressActivity.EXTRA_ADDRESS_ID, 0) ?: 0
            if (addressId > 0) {
                viewModel.setSelectedAddress(addressId)

                // You might want to show a toast or some UI feedback
                Toast.makeText(this, "Address selected successfully", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val shippingSelectionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data ?: return@registerForActivityResult
            val shipName = data.getStringExtra(ShippingActivity.EXTRA_SHIP_NAME) ?: return@registerForActivityResult
            val shipService = data.getStringExtra(ShippingActivity.EXTRA_SHIP_SERVICE) ?: return@registerForActivityResult
            val shipPrice = data.getIntExtra(ShippingActivity.EXTRA_SHIP_PRICE, 0)
            val shipEtd = data.getStringExtra(ShippingActivity.EXTRA_SHIP_ETD) ?: ""

            // Update shipping in ViewModel
            viewModel.setShippingMethod(shipName, shipService, shipPrice, shipEtd)

            // Update UI - display shipping name and service in one line
            updateShippingUI(shipName, shipService, shipEtd, shipPrice)
        }
    }

    private fun formatCurrency(amount: Double): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        return formatter.format(amount).replace(",00", "")
    }

    private fun validateOrder(): Boolean {
        // Check if address is selected
        if (viewModel.addressDetails.value == null) {
            Toast.makeText(this, "Silakan pilih alamat pengiriman", Toast.LENGTH_SHORT).show()
            return false
        }

        // Check if shipping is selected
        val checkoutData = viewModel.checkoutData.value ?: return false
        val shipName = if (checkoutData.isBuyNow) {
            (checkoutData.orderRequest as OrderRequestBuy).shipName
        } else {
            (checkoutData.orderRequest as OrderRequest).shipName
        }

        if (shipName.isEmpty()) {
            Toast.makeText(this, "Silakan pilih metode pengiriman", Toast.LENGTH_SHORT).show()
            return false
        }

        // Check if payment method is selected
        if (viewModel.selectedPayment.value == null) {
            Toast.makeText(this, "Silakan pilih metode pembayaran", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    companion object {
        // Intent extras
        const val EXTRA_CART_ITEM_IDS = "extra_cart_item_ids"
        const val EXTRA_STORE_ID = "STORE_ID"
        const val EXTRA_STORE_NAME = "STORE_NAME"
        const val EXTRA_PRODUCT_ID = "PRODUCT_ID"
        const val EXTRA_PRODUCT_NAME = "PRODUCT_NAME"
        const val EXTRA_PRODUCT_IMAGE = "PRODUCT_IMAGE"
        const val EXTRA_QUANTITY = "QUANTITY"
        const val EXTRA_PRICE = "PRICE"

        // Helper methods for starting activity

        // For Buy Now
        fun startForBuyNow(
            context: Context,
            storeId: Int,
            storeName: String?,
            productId: Int,
            productName: String?,
            productImage: String?,
            quantity: Int,
            price: Double
        ) {
            val intent = Intent(context, CheckoutActivity::class.java).apply {
                putExtra(EXTRA_STORE_ID, storeId)
                putExtra(EXTRA_STORE_NAME, storeName)
                putExtra(EXTRA_PRODUCT_ID, productId)
                putExtra(EXTRA_PRODUCT_NAME, productName)
                putExtra(EXTRA_PRODUCT_IMAGE, productImage)
                putExtra(EXTRA_QUANTITY, quantity)
                putExtra(EXTRA_PRICE, price)
            }
            context.startActivity(intent)
        }

        // For Cart checkout
        fun startForCart(
            context: Context,
            cartItemIds: List<Int>
        ) {
            val intent = Intent(context, CheckoutActivity::class.java).apply {
                putExtra(EXTRA_CART_ITEM_IDS, cartItemIds.toIntArray())
            }
            context.startActivity(intent)
        }
    }
}