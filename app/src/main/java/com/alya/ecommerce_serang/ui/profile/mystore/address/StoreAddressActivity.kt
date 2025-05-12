package com.alya.ecommerce_serang.ui.profile.mystore.address

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.alya.ecommerce_serang.BuildConfig
import com.alya.ecommerce_serang.R
import com.alya.ecommerce_serang.data.api.dto.City
import com.alya.ecommerce_serang.data.api.dto.Province
import com.alya.ecommerce_serang.data.api.dto.StoreAddress
import com.alya.ecommerce_serang.data.api.retrofit.ApiConfig
import com.alya.ecommerce_serang.data.api.retrofit.ApiService
import com.alya.ecommerce_serang.data.repository.AddressRepository
import com.alya.ecommerce_serang.databinding.ActivityStoreAddressBinding
import com.alya.ecommerce_serang.utils.BaseViewModelFactory
import com.alya.ecommerce_serang.utils.SessionManager
import com.google.android.material.snackbar.Snackbar

class StoreAddressActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStoreAddressBinding
    private lateinit var apiService: ApiService
    private lateinit var sessionManager: SessionManager

    private var selectedProvinceId: String? = null
    private var provinces: List<Province> = emptyList()
    private var cities: List<City> = emptyList()

    private val TAG = "StoreAddressActivity"

    private val viewModel: AddressViewModel by viewModels {
        BaseViewModelFactory {
            val apiService = ApiConfig.getApiService(sessionManager)
            val addressRepository = AddressRepository(apiService)
            AddressViewModel(addressRepository)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStoreAddressBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        apiService = ApiConfig.getApiService(sessionManager)

        // Log the base URL
        Log.d(TAG, "BASE_URL: ${BuildConfig.BASE_URL}")

        // Add error text view
        binding.tvError.visibility = View.GONE

        // Set up header title
        val headerTitle = findViewById<TextView>(R.id.header_title)
        headerTitle.text = "Alamat Toko"

        // Set up back button
        val backButton = findViewById<ImageView>(R.id.header_left_icon)
        backButton.setOnClickListener {
            finish()
        }

        setupSpinners()
        setupObservers()
        setupSaveButton()

        // Add retry button
        binding.btnRetry.setOnClickListener {
            binding.tvError.visibility = View.GONE
            binding.progressBar.visibility = View.VISIBLE
            Log.d(TAG, "Retrying to fetch provinces...")
            viewModel.fetchProvinces()
        }

        // Show loading spinners initially
        showProvinceLoading(true)

        // Load existing address data first
        Log.d(TAG, "Fetching store address...")
        viewModel.fetchStoreAddress()

        // Load provinces data
        Log.d(TAG, "Fetching provinces...")
        viewModel.fetchProvinces()
    }

    private fun setupSpinners() {
        // Province spinner listener
        binding.spinnerProvince.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                Log.d(TAG, "Province selected at position: $position")
                if (position > 0 && provinces.isNotEmpty()) {
                    selectedProvinceId = provinces[position - 1].provinceId
                    Log.d(TAG, "Selected province ID: $selectedProvinceId")
                    selectedProvinceId?.let {
                        Log.d(TAG, "Fetching cities for province ID: $it")
                        showCityLoading(true)
                        viewModel.fetchCities(it)
                    }
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                // Do nothing
            }
        }
    }

    private fun setupObservers() {
        // Observe provinces data
        viewModel.provinces.observe(this) { provinceList ->
            Log.d(TAG, "Received provinces: ${provinceList.size}")
            showProvinceLoading(false)

            if (provinceList.isEmpty()) {
                showError("No provinces available")
                return@observe
            }

            provinces = provinceList
            val provinceNames = mutableListOf("Pilih Provinsi")
            provinceNames.addAll(provinceList.map { it.provinceName })

            Log.d(TAG, "Province names: $provinceNames")

            val adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                provinceNames
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerProvince.adapter = adapter
        }

        // Observe cities data
        viewModel.cities.observe(this) { cityList ->
            Log.d(TAG, "Received cities: ${cityList.size}")
            showCityLoading(false)

            cities = cityList
            val cityNames = mutableListOf("Pilih Kota/Kabupaten")
            cityNames.addAll(cityList.map { it.cityName })

            Log.d(TAG, "City names: $cityNames")

            val adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                cityNames
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerCity.adapter = adapter

            // If we have a stored city_id, select it
            viewModel.storeAddress.value?.let { address ->
                if (address.cityId.isNotEmpty()) {
                    val cityIndex = cities.indexOfFirst { city ->
                        city.cityId == address.cityId
                    }
                    Log.d(TAG, "City index for ID ${address.cityId}: $cityIndex")
                    if (cityIndex != -1) {
                        binding.spinnerCity.setSelection(cityIndex + 1) // +1 because of "Pilih Kota/Kabupaten"
                    }
                }
            }
        }

        // Observe store address data
        viewModel.storeAddress.observe(this) { address ->
            Log.d(TAG, "Received store address: $address")
            address?.let {
                // Set the fields
                binding.edtStreet.setText(address.street)
                binding.edtSubdistrict.setText(address.subdistrict)
                binding.edtDetail.setText(address.detail ?: "")
                binding.edtPostalCode.setText(address.postalCode)

                // Handle latitude and longitude
                val lat = if (address.latitude == null || address.latitude.toString() == "NaN") 0.0 else address.latitude
                val lng = if (address.longitude == null || address.longitude.toString() == "NaN") 0.0 else address.longitude

                binding.edtLatitude.setText(lat.toString())
                binding.edtLongitude.setText(lng.toString())

                // Set selected province ID to trigger city loading
                if (address.provinceId.isNotEmpty()) {
                    selectedProvinceId = address.provinceId

                    // Find province index and select it after provinces are loaded
                    if (provinces.isNotEmpty()) {
                        val provinceIndex = provinces.indexOfFirst { province ->
                            province.provinceId == address.provinceId
                        }
                        Log.d(TAG, "Province index for ID ${address.provinceId}: $provinceIndex")
                        if (provinceIndex != -1) {
                            binding.spinnerProvince.setSelection(provinceIndex + 1) // +1 because of "Pilih Provinsi"

                            // Now fetch cities for this province
                            showCityLoading(true)
                            viewModel.fetchCities(address.provinceId)
                        }
                    }
                }
            }
        }

        // Observe loading state
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Observe error messages
        viewModel.errorMessage.observe(this) { errorMsg ->
            Log.e(TAG, "Error: $errorMsg")
            showError(errorMsg)
        }

        // Observe save success
        viewModel.saveSuccess.observe(this) { success ->
            if (success) {
                Toast.makeText(this, "Alamat berhasil disimpan", Toast.LENGTH_SHORT).show()
                setResult(Activity.RESULT_OK)
                finish()
            }
        }
    }

    private fun showProvinceLoading(isLoading: Boolean) {
        binding.provinceProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.spinnerProvince.visibility = if (isLoading) View.GONE else View.VISIBLE
    }

    private fun showCityLoading(isLoading: Boolean) {
        binding.cityProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.spinnerCity.visibility = if (isLoading) View.GONE else View.VISIBLE
    }

    private fun showError(message: String) {
        binding.progressBar.visibility = View.GONE
        binding.tvError.visibility = View.VISIBLE
        binding.tvError.text = "Error: $message\nURL: ${BuildConfig.BASE_URL}/provinces"
        binding.btnRetry.visibility = View.VISIBLE

        // Also show in a dialog for immediate attention
        AlertDialog.Builder(this)
            .setTitle("Error")
            .setMessage("$message\n\nAPI URL: ${BuildConfig.BASE_URL}/provinces")
            .setPositiveButton("Retry") { _, _ ->
                binding.tvError.visibility = View.GONE
                binding.progressBar.visibility = View.VISIBLE
                viewModel.fetchProvinces()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()

        // Also show a snackbar
        Snackbar.make(binding.root, "Error: $message", Snackbar.LENGTH_LONG)
            .setAction("Retry") {
                binding.tvError.visibility = View.GONE
                binding.progressBar.visibility = View.VISIBLE
                viewModel.fetchProvinces()
            }
            .show()
    }

    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener {
            val street = binding.edtStreet.text.toString()
            val subdistrict = binding.edtSubdistrict.text.toString()
            val detail = binding.edtDetail.text.toString()
            val postalCode = binding.edtPostalCode.text.toString()
            val latitudeStr = binding.edtLatitude.text.toString()
            val longitudeStr = binding.edtLongitude.text.toString()

            // Validate required fields
            if (selectedProvinceId == null || binding.spinnerCity.selectedItemPosition <= 0 ||
                street.isEmpty() || subdistrict.isEmpty() || postalCode.isEmpty()) {
                Toast.makeText(this, "Mohon lengkapi data yang wajib diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Get selected city
            val cityPosition = binding.spinnerCity.selectedItemPosition
            if (cityPosition <= 0 || cities.isEmpty() || cityPosition > cities.size) {
                Toast.makeText(this, "Mohon pilih kota/kabupaten", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedCity = cities[cityPosition - 1]

            // Parse coordinates
            val latitude = latitudeStr.toDoubleOrNull() ?: 0.0
            val longitude = longitudeStr.toDoubleOrNull() ?: 0.0

            // Save address
            viewModel.saveStoreAddress(
                provinceId = selectedProvinceId!!,
                provinceName = provinces.find { it.provinceId == selectedProvinceId }?.provinceName ?: "",
                cityId = selectedCity.cityId,
                cityName = selectedCity.cityName,
                street = street,
                subdistrict = subdistrict,
                detail = detail,
                postalCode = postalCode,
                latitude = latitude,
                longitude = longitude
            )
        }
    }
}