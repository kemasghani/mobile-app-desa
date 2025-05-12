package com.alya.ecommerce_serang.ui.product

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.alya.ecommerce_serang.BuildConfig.BASE_URL
import com.alya.ecommerce_serang.R
import com.alya.ecommerce_serang.data.api.dto.CartItem
import com.alya.ecommerce_serang.data.api.dto.ProductsItem
import com.alya.ecommerce_serang.data.api.response.customer.product.Product
import com.alya.ecommerce_serang.data.api.response.customer.product.ReviewsItem
import com.alya.ecommerce_serang.data.api.response.customer.product.StoreProduct
import com.alya.ecommerce_serang.data.api.retrofit.ApiConfig
import com.alya.ecommerce_serang.data.api.retrofit.ApiService
import com.alya.ecommerce_serang.data.repository.ProductRepository
import com.alya.ecommerce_serang.data.repository.Result
import com.alya.ecommerce_serang.databinding.ActivityDetailProductBinding
import com.alya.ecommerce_serang.ui.home.HorizontalProductAdapter
import com.alya.ecommerce_serang.ui.order.CheckoutActivity
import com.alya.ecommerce_serang.utils.BaseViewModelFactory
import com.alya.ecommerce_serang.utils.SessionManager
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.text.NumberFormat
import java.util.Locale

class DetailProductActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailProductBinding
    private lateinit var apiService: ApiService
    private lateinit var sessionManager: SessionManager
    private var productAdapter: HorizontalProductAdapter? = null
    private var reviewsAdapter: ReviewsAdapter? = null
    private var currentQuantity = 1


    private val viewModel: ProductUserViewModel by viewModels {
        BaseViewModelFactory {
            val apiService = ApiConfig.getApiService(sessionManager)
            val productRepository = ProductRepository(apiService)
            ProductUserViewModel(productRepository)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        apiService = ApiConfig.getApiService(sessionManager)

        setupUI()
        setupObservers()
        loadData()
    }

    private fun loadData() {
        val productId = intent.getIntExtra("PRODUCT_ID", -1)
        if (productId == -1) {
            Log.e("DetailProductActivity", "Invalid Product ID")
            Toast.makeText(this, "Invalid product ID", Toast.LENGTH_SHORT).show()
            finish() // Close activity if no valid ID
            return
        }

        viewModel.loadProductDetail(productId)
        viewModel.loadReviews(productId)
    }

    private fun setupObservers() {
        viewModel.productDetail.observe(this) { product ->
            product?.let {
                updateUI(it)
                viewModel.loadOtherProducts(it.storeId)
            }
        }

        viewModel.storeDetail.observe(this) { result ->
            when (result) {
                is Result.Success -> {
                    updateStoreInfo(result.data)
                }
                is Result.Error -> {
                    // Show error message, maybe a Toast or Snackbar
                    Toast.makeText(this, "Failed to load store: ${result.exception.message}", Toast.LENGTH_SHORT).show()
                }
                is Result.Loading -> {
                    // Show loading indicator if needed
                }
            }
        }

        viewModel.otherProducts.observe(this) { products ->
            updateOtherProducts(products)
        }

        viewModel.reviewProduct.observe(this) { reviews ->
            setupRecyclerViewReviewsProduct(reviews)
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBarDetailProd.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(this) { errorMessage ->
            if (errorMessage.isNotEmpty()) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
            }
        }
        viewModel.addCart.observe(this) { result ->
            when (result) {
                is Result.Success -> {
                    val cartId = result.data.data.cartId
                    Toast.makeText(this, result.data.message, Toast.LENGTH_SHORT).show()
                }
                is Result.Error -> {
                    Toast.makeText(this, "Failed to add to cart: ${result.exception.message}", Toast.LENGTH_SHORT).show()
                }
                is Result.Loading -> {
                    // Show loading indicator if needed
                }
            }
        }
    }

    private fun updateStoreInfo(store: StoreProduct?) {
        store?.let {
            binding.tvSellerName.text = it.storeName
            binding.tvSellerRating.text = it.storeRating
            binding.tvSellerLocation.text = it.storeLocation

            // Load store image using Glide
            val fullImageUrl = when (val img = it.storeImage) {
                is String -> {
                    if (img.startsWith("/")) BASE_URL + img.substring(1) else img
                }
                else -> R.drawable.placeholder_image
            }

            Glide.with(this)
                .load(fullImageUrl)
                .placeholder(R.drawable.placeholder_image)
                .into(binding.ivSellerImage)
        }
    }

    private fun updateOtherProducts(products: List<ProductsItem>) {
        if (products.isEmpty()) {
            binding.recyclerViewOtherProducts.visibility = View.GONE
            binding.tvViewAllProducts.visibility = View.GONE
        } else {
            binding.recyclerViewOtherProducts.visibility = View.VISIBLE
            binding.tvViewAllProducts.visibility = View.VISIBLE
            productAdapter?.updateProducts(products)
        }    }

    private fun setupUI() {
//        binding.btnBack.setOnClickListener {
//            finish()
//        }

        binding.tvViewAllReviews.setOnClickListener {
            viewModel.productDetail.value?.productId?.let { productId ->
                handleAllReviewsClick(productId)
            }
        }

        binding.btnBuyNow.setOnClickListener {
            viewModel.productDetail.value?.productId?.let { id ->
                showBuyNowPopup(id)
            }
        }

        binding.btnAddToCart.setOnClickListener {
            viewModel.productDetail.value?.productId?.let { id ->
                showAddToCartPopup(id)
            }
        }

        setupRecyclerViewOtherProducts()
    }

    private fun updateUI(product: Product){
        binding.tvProductName.text = product.productName
        binding.tvPrice.text = formatCurrency(product.price.toDouble())
        binding.tvSold.text = product.totalSold.toString()
        binding.tvRating.text = product.rating
        binding.tvWeight.text = product.weight.toString()
        binding.tvStock.text = product.stock.toString()
        binding.tvCategory.text = product.productCategory
        binding.tvDescription.text = product.description



        val fullImageUrl = when (val img = product.image) {
            is String -> {
                if (img.startsWith("/")) BASE_URL + img.substring(1) else img
            }
            else -> R.drawable.placeholder_image // Default image for null
        }
        Log.d("ProductAdapter", "Loading image: $fullImageUrl")

        Glide.with(this)
            .load(fullImageUrl)
            .placeholder(R.drawable.placeholder_image)
            .into(binding.ivProductImage)
    }

    private fun handleAllReviewsClick(productId: Int) {
        val intent = Intent(this, ReviewProductActivity::class.java)
        intent.putExtra("PRODUCT_ID", productId) // Pass product ID
        startActivity(intent)
    }

    private fun setupRecyclerViewOtherProducts(){
        productAdapter = HorizontalProductAdapter(
            products = emptyList(),
            onClick = { productsItem -> handleProductClick(productsItem) }
        )

        binding.recyclerViewOtherProducts.apply {
            adapter = productAdapter
            layoutManager = LinearLayoutManager(
                context,
                LinearLayoutManager.HORIZONTAL,
                false
            )
        }
    }

    private fun setupRecyclerViewReviewsProduct(reviewList: List<ReviewsItem>){
        val limitedReviewList = if (reviewList.isNotEmpty()) listOf(reviewList.first()) else emptyList()
        if (reviewList.isEmpty()) {
            binding.recyclerViewReviews.visibility = View.GONE
            binding.tvViewAllReviews.visibility = View.GONE
//            binding.tvNoReviews.visibility = View.VISIBLE
        } else {
            binding.recyclerViewReviews.visibility = View.VISIBLE
            binding.tvViewAllReviews.visibility = View.VISIBLE
        }
//            binding.tvNoReviews.visibility = View.GONE
        reviewsAdapter = ReviewsAdapter(
            reviewList = limitedReviewList
        )

        binding.recyclerViewReviews.apply {
            adapter = reviewsAdapter
            layoutManager = LinearLayoutManager(
                context,
                LinearLayoutManager.VERTICAL,
                false
            )
        }
    }

    private fun handleProductClick(product: ProductsItem) {
        val intent = Intent(this, DetailProductActivity::class.java)
        intent.putExtra("PRODUCT_ID", product.id) // Pass product ID
        startActivity(intent)
    }

    private fun showBuyNowPopup(productId: Int) {
        showQuantityDialog(productId, true)
    }

    private fun showAddToCartPopup(productId: Int) {
        showQuantityDialog(productId, false)
    }

    private fun showQuantityDialog(productId: Int, isBuyNow: Boolean) {
        val bottomSheetDialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.dialog_count_buy, null)
        bottomSheetDialog.setContentView(view)

        val btnDecrease = view.findViewById<Button>(R.id.btnDecrease)
        val btnIncrease = view.findViewById<Button>(R.id.btnIncrease)
        val tvQuantity = view.findViewById<TextView>(R.id.tvQuantity)
        val btnBuyNow = view.findViewById<Button>(R.id.btnBuyNow)
        val btnClose = view.findViewById<ImageButton>(R.id.btnCloseDialog)

        // Set button text based on action
        if (!isBuyNow) {
            btnBuyNow.setText(R.string.add_to_cart)
        }

        currentQuantity = 1
        tvQuantity.text = currentQuantity.toString()

        val maxStock = viewModel.productDetail.value?.stock ?: 1

        btnDecrease.setOnClickListener {
            if (currentQuantity > 1) {
                currentQuantity--
                tvQuantity.text = currentQuantity.toString()
            }
        }

        btnIncrease.setOnClickListener {
            if (currentQuantity < maxStock) {
                currentQuantity++
                tvQuantity.text = currentQuantity.toString()
            } else {
                Toast.makeText(this, "Maximum stock reached", Toast.LENGTH_SHORT).show()
            }
        }

        btnBuyNow.setOnClickListener {
            bottomSheetDialog.dismiss()

            if (isBuyNow) {
                // If it's Buy Now, navigate directly to checkout without adding to cart
                navigateToCheckout()
            } else {
                // If it's Add to Cart, add the item to the cart
                val cartItem = CartItem(
                    productId = productId,
                    quantity = currentQuantity
                )
                viewModel.reqCart(cartItem)
            }
        }

        btnClose.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }

    private fun formatCurrency(amount: Double): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        return formatter.format(amount).replace(",00", "")
    }

    private fun navigateToCheckout() {
        val productDetail = viewModel.productDetail.value ?: return
        val storeDetail = viewModel.storeDetail.value

        if (storeDetail !is Result.Success || storeDetail.data == null) {
            Toast.makeText(this, "Store information not available", Toast.LENGTH_SHORT).show()
            return
        }

        // Start checkout activity with buy now flow
        CheckoutActivity.startForBuyNow(
            context = this,
            storeId = productDetail.storeId,
            storeName = storeDetail.data.storeName,
            productId = productDetail.productId,
            productName = productDetail.productName,
            productImage = productDetail.image,
            quantity = currentQuantity,
            price = productDetail.price.toDouble()
        )
    }

    companion object {
        const val EXTRA_PRODUCT_ID = "extra_product_id"

        fun start(context: Context, productId: Int) {
            val intent = Intent(context, DetailProductActivity::class.java)
            intent.putExtra(EXTRA_PRODUCT_ID, productId)
            context.startActivity(intent)
        }
    }
}