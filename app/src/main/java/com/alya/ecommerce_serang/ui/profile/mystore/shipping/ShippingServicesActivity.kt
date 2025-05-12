package com.alya.ecommerce_serang.ui.profile.mystore.shipping

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.alya.ecommerce_serang.R
import com.alya.ecommerce_serang.data.api.retrofit.ApiConfig
import com.alya.ecommerce_serang.data.repository.ShippingServicesRepository
import com.alya.ecommerce_serang.databinding.ActivityShippingServicesBinding
import com.alya.ecommerce_serang.utils.BaseViewModelFactory
import com.alya.ecommerce_serang.utils.SessionManager

class ShippingServicesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityShippingServicesBinding
    private lateinit var sessionManager: SessionManager
    private val courierCheckboxes = mutableListOf<Pair<CheckBox, String>>()

    private val viewModel: ShippingServicesViewModel by viewModels {
        BaseViewModelFactory {
            val apiService = ApiConfig.getApiService(sessionManager)
            val repository = ShippingServicesRepository(apiService)
            ShippingServicesViewModel(repository)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShippingServicesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        // Configure header
        val headerTitle = findViewById<TextView>(R.id.header_title)
        headerTitle.text = "Layanan Pengiriman"

        val backButton = findViewById<ImageView>(R.id.header_left_icon)
        backButton.setOnClickListener {
            finish()
        }

        setupCourierCheckboxes()
        setupObservers()

        binding.btnSave.setOnClickListener {
            saveShippingServices()
        }

        // Load shipping services
        viewModel.getAvailableCouriers()
    }

    private fun setupCourierCheckboxes() {
        // Add all courier checkboxes to the list for easy management
        courierCheckboxes.add(Pair(binding.checkboxJne, "jne"))
        courierCheckboxes.add(Pair(binding.checkboxPos, "pos"))
        courierCheckboxes.add(Pair(binding.checkboxTiki, "tiki"))
        courierCheckboxes.add(Pair(binding.checkboxSicepat, "sicepat"))
        courierCheckboxes.add(Pair(binding.checkboxJnt, "j&t"))
        courierCheckboxes.add(Pair(binding.checkboxNinja, "ninja"))
        courierCheckboxes.add(Pair(binding.checkboxAntaraja, "anteraja"))
        courierCheckboxes.add(Pair(binding.checkboxSpx, "spx"))
    }

    private fun setupObservers() {
        viewModel.availableCouriers.observe(this) { couriers ->
            // Check the appropriate checkboxes based on available couriers
            for (pair in courierCheckboxes) {
                val checkbox = pair.first
                val courierCode = pair.second
                checkbox.isChecked = couriers.contains(courierCode)
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.contentLayout.visibility = if (isLoading) View.GONE else View.VISIBLE
        }

        viewModel.errorMessage.observe(this) { errorMessage ->
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
        }

        viewModel.saveSuccess.observe(this) { success ->
            if (success) {
                Toast.makeText(this, "Layanan pengiriman berhasil disimpan", Toast.LENGTH_SHORT).show()
                setResult(Activity.RESULT_OK)
                finish()
            }
        }
    }

    private fun saveShippingServices() {
        val selectedCouriers = mutableListOf<String>()

        for (pair in courierCheckboxes) {
            val checkbox = pair.first
            val courierCode = pair.second
            if (checkbox.isChecked) {
                selectedCouriers.add(courierCode)
            }
        }

        if (selectedCouriers.isEmpty()) {
            Toast.makeText(this, "Pilih minimal satu layanan pengiriman", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.saveShippingServices(selectedCouriers)
    }
}