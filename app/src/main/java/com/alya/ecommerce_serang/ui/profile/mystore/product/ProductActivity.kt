package com.alya.ecommerce_serang.ui.profile.mystore.product

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.alya.ecommerce_serang.data.api.retrofit.ApiConfig
import com.alya.ecommerce_serang.data.repository.ProductRepository
import com.alya.ecommerce_serang.data.repository.Result
import com.alya.ecommerce_serang.databinding.ActivityProductBinding
import com.alya.ecommerce_serang.utils.viewmodel.ProductViewModel
import com.alya.ecommerce_serang.utils.BaseViewModelFactory
import com.alya.ecommerce_serang.utils.SessionManager

class ProductActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductBinding
    private lateinit var sessionManager: SessionManager

    private val viewModel: ProductViewModel by viewModels {
        BaseViewModelFactory {
            sessionManager = SessionManager(this)
            val apiService = ApiConfig.getApiService(sessionManager)
            val productRepository = ProductRepository(apiService)
            ProductViewModel(productRepository)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupHeader()
        setupRecyclerView()
        observeViewModel()

        binding.progressBar.visibility = View.VISIBLE
        viewModel.loadMyStoreProducts()

    }

    private fun observeViewModel() {
        viewModel.productList.observe(this) { result ->
            when (result) {
                is Result.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is Result.Success -> {
                    binding.progressBar.visibility = View.GONE
                    val products = result.data
                    binding.rvStoreProduct.adapter = ProductAdapter(products) {
                        Toast.makeText(this, "Clicked: ${it.name}", Toast.LENGTH_SHORT).show()
                    }
                }
                is Result.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, "Gagal memuat produk: ${result.exception.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupHeader() {
        binding.header.headerTitle.text = "Produk Saya"
        binding.header.headerRightText.visibility = View.VISIBLE

        binding.header.headerLeftIcon.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.header.headerRightText.setOnClickListener {
            val intent = Intent(this, DetailStoreProductActivity::class.java)
            intent.putExtra("is_editing", false)
            startActivity(intent)
        }
    }

    private fun setupRecyclerView() {
        binding.rvStoreProduct.layoutManager = LinearLayoutManager(this)
    }
}