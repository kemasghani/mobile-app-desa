package com.alya.ecommerce_serang.ui.product

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.alya.ecommerce_serang.data.api.retrofit.ApiConfig
import com.alya.ecommerce_serang.data.api.retrofit.ApiService
import com.alya.ecommerce_serang.data.repository.ProductRepository
import com.alya.ecommerce_serang.databinding.ActivityReviewProductBinding
import com.alya.ecommerce_serang.utils.BaseViewModelFactory
import com.alya.ecommerce_serang.utils.SessionManager

class ReviewProductActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReviewProductBinding
    private lateinit var apiService: ApiService
    private var reviewsAdapter: ReviewsAdapter? = null
    private lateinit var sessionManager: SessionManager
    private val viewModel: ProductUserViewModel by viewModels {
        BaseViewModelFactory {
            val apiService = ApiConfig.getApiService(sessionManager)
            val productRepository = ProductRepository(apiService)
            ProductUserViewModel(productRepository)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReviewProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        enableEdgeToEdge()

        sessionManager = SessionManager(this)
        apiService = ApiConfig.getApiService(sessionManager)

        val productId = intent.getIntExtra("PRODUCT_ID", -1) // Get the product ID
        if (productId == -1) {
            Log.e("ReviewProductActivity", "Invalid Product ID")
            finish() // Close the activity if the product ID is invalid
            return
        }

        setupRecyclerView()
        viewModel.loadReviews(productId) // Fetch reviews using productId

        observeReviews() // Observe review data
    }

    private fun observeReviews() {
        viewModel.reviewProduct.observe(this) { reviews ->
            if (reviews.isNotEmpty()) {
                reviewsAdapter?.setReviews(reviews)
                binding.tvNoReviews.visibility = View.GONE
            } else {
                binding.tvNoReviews.visibility = View.VISIBLE // Show "No Reviews" message
            }
        }
    }

    private fun setupRecyclerView() {
        reviewsAdapter = ReviewsAdapter(
            reviewList = emptyList()
        )

        binding.rvReviewsProduct.apply {
            adapter = reviewsAdapter
            layoutManager = LinearLayoutManager(
                context,
                LinearLayoutManager.VERTICAL,
                false
            )
        }
    }
}
