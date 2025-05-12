package com.alya.ecommerce_serang.ui.order.address

import android.annotation.SuppressLint
import android.content.Intent
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.alya.ecommerce_serang.data.api.dto.CreateAddressRequest
import com.alya.ecommerce_serang.data.api.dto.UserProfile
import com.alya.ecommerce_serang.data.api.response.customer.order.CitiesItem
import com.alya.ecommerce_serang.data.api.response.customer.order.ProvincesItem
import com.alya.ecommerce_serang.data.api.retrofit.ApiConfig
import com.alya.ecommerce_serang.data.api.retrofit.ApiService
import com.alya.ecommerce_serang.data.repository.OrderRepository
import com.alya.ecommerce_serang.data.repository.UserRepository
import com.alya.ecommerce_serang.databinding.ActivityAddAddressBinding
import com.alya.ecommerce_serang.utils.SavedStateViewModelFactory
import com.alya.ecommerce_serang.utils.SessionManager

class AddAddressActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddAddressBinding
    private lateinit var apiService: ApiService
    private lateinit var sessionManager: SessionManager
    private  var profileUser: Int = 1
    private lateinit var locationManager: LocationManager

    private var isRequestingLocation = false

    private var latitude: Double? = null
    private var longitude: Double? = null
    private val provinceAdapter by lazy { ProvinceAdapter(this) }
    private val cityAdapter by lazy { CityAdapter(this) }

    private val viewModel: AddAddressViewModel by viewModels {
        SavedStateViewModelFactory(this) { savedStateHandle ->
            val apiService = ApiConfig.getApiService(sessionManager)
            val orderRepository = OrderRepository(apiService)
            val userRepository = UserRepository(apiService)
            AddAddressViewModel(orderRepository, userRepository, savedStateHandle)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddAddressBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        apiService = ApiConfig.getApiService(sessionManager)
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

//         Get user profile from session manager
//        profileUser =UserProfile.
        viewModel.userProfile.observe(this){ user ->
            user?.let { updateProfile(it) }
        }

        setupToolbar()
        requestLocationPermission()
        setupReloadButtons()
        setupAutoComplete()
        setupButtonListeners()
        setupObservers()



        // Force trigger province loading to ensure it happens
        viewModel.getProvinces()
    }

    private fun updateProfile(userProfile: UserProfile){
        profileUser = userProfile.userId
    }

    // UI setup methods
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupAutoComplete() {
        Log.d(TAG, "Setting up AutoComplete dropdowns")
        // Set adapters
        binding.autoCompleteProvinsi.setAdapter(provinceAdapter)
        binding.autoCompleteKabupaten.setAdapter(cityAdapter)

        // Make dropdown appear on click (not just when typing)
        binding.autoCompleteProvinsi.setOnClickListener {
            Log.d(TAG, "Province dropdown clicked, showing dropdown")
            binding.autoCompleteProvinsi.showDropDown()
        }

        binding.autoCompleteKabupaten.setOnClickListener {
            // Only show dropdown if we have cities loaded
            if (cityAdapter.count > 0) {
                Log.d(TAG, "City dropdown clicked, showing dropdown with ${cityAdapter.count} items")
                binding.autoCompleteKabupaten.showDropDown()
            } else {
                Log.d(TAG, "City dropdown clicked but no cities available")
                Toast.makeText(this, "Pilih provinsi terlebih dahulu", Toast.LENGTH_SHORT).show()
            }
        }

        // Set listeners for selection
        binding.autoCompleteProvinsi.setOnItemClickListener { _, _, position, _ ->
            val provinceId = provinceAdapter.getProvinceId(position)
            Log.d(TAG, "Province selected at position $position, provinceId=$provinceId")

            provinceId?.let { id ->
                Log.d(TAG, "Getting cities for provinceId=$id")
                viewModel.getCities(id)
                binding.autoCompleteKabupaten.text.clear()
            } ?: Log.e(TAG, "Could not get provinceId for position $position")
        }

        binding.autoCompleteKabupaten.setOnItemClickListener { _, _, position, _ ->
            val cityId = cityAdapter.getCityId(position)
            Log.d(TAG, "City selected at position $position, cityId=$cityId")

            cityId?.let { id ->
                Log.d(TAG, "Setting selectedCityId=$id")
                viewModel.selectedCityId = id
            } ?: Log.e(TAG, "Could not get cityId for position $position")
        }
    }

    private fun setupButtonListeners() {
        binding.buttonSimpan.setOnClickListener {
            validateAndSubmitForm()
        }
    }

    private fun setupObservers() {
        Log.d(TAG, "Setting up LiveData observers")

        // Observe provinces
        viewModel.provincesState.observe(this) { state ->
            Log.d(TAG, "Received provincesState update: $state")
            handleProvinceState(state)
        }

        // Observe cities
        viewModel.citiesState.observe(this) { state ->
            Log.d(TAG, "Received citiesState update: $state")
            handleCityState(state)
        }

        // Observe address submission
        viewModel.addressSubmissionState.observe(this) { state ->
            Log.d(TAG, "Received addressSubmissionState update: $state")
            handleAddressSubmissionState(state)
        }
    }

    private fun handleProvinceState(state: ViewState<List<ProvincesItem>>) {
        when (state) {
            is ViewState.Loading -> {
                Log.d("AddAddressActivity", "Loading provinces...")
                // Show loading indicator
            }
            is ViewState.Success -> {
                Log.d("AddAddressActivity", "Provinces loaded: ${state.data.size}")
                // Hide loading indicator
                if (state.data.isNotEmpty()) {
                    provinceAdapter.updateData(state.data)
                } else {
                    showError("No provinces available")
                }
            }
            is ViewState.Error -> {
                // Hide loading indicator
                showError("Failed to load provinces: ${state.message}")
                Log.e("AddAddressActivity", "Province error: ${state.message}")
            }
        }
    }

    private fun handleCityState(state: ViewState<List<CitiesItem>>) {
        when (state) {
            is ViewState.Loading -> {
                Log.d("AddAddressActivity", "Loading cities...")
                binding.cityProgressBar.visibility = View.VISIBLE
            }
            is ViewState.Success -> {
                Log.d("AddAddressActivity", "Cities loaded: ${state.data.size}")
                binding.cityProgressBar.visibility = View.GONE
                cityAdapter.updateData(state.data)
            }
            is ViewState.Error -> {
                binding.cityProgressBar.visibility = View.GONE
                showError("Failed to load cities: ${state.message}")
                Log.e("AddAddressActivity", "City error: ${state.message}")
            }
        }
    }

    private fun handleAddressSubmissionState(state: ViewState<String>) {
        when (state) {
            is ViewState.Loading -> {
                Log.d(TAG, "Address submission: Loading")
                showSubmitLoading(true)
            }
            is ViewState.Success -> {
                Log.d(TAG, "Address submission: Success - ${state.data}")
                showSubmitLoading(false)
                showSuccessAndFinish(state.data)
            }
            is ViewState.Error -> {
                Log.e(TAG, "Address submission: Error - ${state.message}")
                showSubmitLoading(false)
                showError(state.message)
            }
        }
    }

    private fun showSubmitLoading(isLoading: Boolean) {
        binding.buttonSimpan.isEnabled = !isLoading
        binding.buttonSimpan.text = if (isLoading) "Menyimpan..." else "Simpan"
//        binding.submitProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showSuccessAndFinish(message: String) {
        Toast.makeText(this, "Sukses: $message", Toast.LENGTH_SHORT).show()
        setResult(RESULT_OK)
        finish()
    }

    private fun validateAndSubmitForm() {
        Log.d(TAG, "Validating form...")
        Log.d(TAG, "Current location: lat=$latitude, long=$longitude")

        // Check if we have location - always use default if not available
        if (latitude == null || longitude == null) {
            Log.w(TAG, "No location detected, using default location")
            // Default location for Jakarta
            latitude = -6.200000
            longitude = 106.816666
            binding.tvLocationStatus.text = "Menggunakan lokasi default: Jakarta"
        }

        val street = binding.etDetailAlamat.text.toString().trim()
        val subDistrict = binding.etKecamatan.text.toString().trim()
        val postalCode = binding.etKodePos.text.toString().trim()
        val recipient = binding.etNamaPenerima.text.toString().trim()
        val phone = binding.etNomorHp.text.toString().trim()
        val userId = try {
            profileUser
        } catch (e: Exception) {
            Log.w(TAG, "Error getting userId, using default", e)
            1 // Default userId for testing
        }
        val isStoreLocation = false

        val provinceId = viewModel.selectedProvinceId
        val cityId = viewModel.selectedCityId

        Log.d(TAG, "Form data: street=$street, subDistrict=$subDistrict, postalCode=$postalCode, " +
                "recipient=$recipient, phone=$phone, userId=$userId, provinceId=$provinceId, cityId=$cityId, " +
                "lat=$latitude, long=$longitude")

        // Validate required fields
        if (street.isBlank()) {
            Log.w(TAG, "Validation failed: street is blank")
            binding.etDetailAlamat.error = "Alamat tidak boleh kosong"
            binding.etDetailAlamat.requestFocus()
            return
        }

        if (recipient.isBlank()) {
            Log.w(TAG, "Validation failed: recipient is blank")
            binding.etNamaPenerima.error = "Nama penerima tidak boleh kosong"
            binding.etNamaPenerima.requestFocus()
            return
        }

        if (phone.isBlank()) {
            Log.w(TAG, "Validation failed: phone is blank")
            binding.etNomorHp.error = "Nomor HP tidak boleh kosong"
            binding.etNomorHp.requestFocus()
            return
        }

        if (provinceId == null) {
            Log.w(TAG, "Validation failed: provinceId is null")
            showError("Pilih provinsi terlebih dahulu")
            binding.autoCompleteProvinsi.requestFocus()
            return
        }

        if (cityId == null) {
            Log.w(TAG, "Validation failed: cityId is null")
            showError("Pilih kota/kabupaten terlebih dahulu")
            binding.autoCompleteKabupaten.requestFocus()
            return
        }

        // Create request with all fields
        val request = CreateAddressRequest(
            lat = latitude!!,  // Safe to use !! as we've checked above
            long = longitude!!,
            street = street,
            subDistrict = subDistrict,
            cityId = cityId,
            provId = provinceId,
            postCode = postalCode,
            detailAddress = street,
            userId = userId,
            recipient = recipient,
            phone = phone,
            isStoreLocation = isStoreLocation
        )

        Log.d(TAG, "Form validation successful, submitting address: $request")
        viewModel.addAddress(request)
    }

    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                requestLocation()
            } else {
                Toast.makeText(this, "Izin lokasi ditolak", Toast.LENGTH_SHORT).show()
            }
        }

    private fun requestLocationPermission() {
        locationPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
    }

    @SuppressLint("MissingPermission")
    private fun requestLocation() {
        Log.d(TAG, "Requesting device location")

        // Check if we're already requesting location to avoid multiple requests
        if (isRequestingLocation) {
            Log.w(TAG, "Location request already in progress")
            return
        }

        isRequestingLocation = true
        binding.locationProgressBar.visibility = View.VISIBLE
        binding.tvLocationStatus.text = "Mencari lokasi..."

        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        Log.d(TAG, "Location providers: GPS=$isGpsEnabled, Network=$isNetworkEnabled")

        if (!isGpsEnabled && !isNetworkEnabled) {
            Log.w(TAG, "No location providers enabled")
            binding.locationProgressBar.visibility = View.GONE
            binding.tvLocationStatus.text = "Provider lokasi tidak tersedia"
            isRequestingLocation = false
            Toast.makeText(this, "Provider lokasi tidak tersedia", Toast.LENGTH_SHORT).show()
            showEnableLocationDialog()
            return
        }

        // Create location criteria
        val criteria = Criteria()
        criteria.accuracy = Criteria.ACCURACY_FINE
        criteria.isBearingRequired = false
        criteria.isAltitudeRequired = false
        criteria.isSpeedRequired = false
        criteria.powerRequirement = Criteria.POWER_LOW

        // Get the best provider based on criteria
        val provider = locationManager.getBestProvider(criteria, true) ?:
        if (isGpsEnabled) LocationManager.GPS_PROVIDER else LocationManager.NETWORK_PROVIDER

        Log.d(TAG, "Using location provider: $provider")

        // Set timeout for location
        Handler(Looper.getMainLooper()).postDelayed({
            if (isRequestingLocation) {
                Log.w(TAG, "Location timeout, using default")
                binding.locationProgressBar.visibility = View.GONE
                binding.tvLocationStatus.text = "Lokasi default: Jakarta"
                latitude = -6.200000
                longitude = 106.816666
                isRequestingLocation = false
                Toast.makeText(this, "Timeout lokasi, menggunakan lokasi default", Toast.LENGTH_SHORT).show()
            }
        }, 15000) // 15 seconds timeout

        // Try getting last known location first
        try {
            val lastLocation = locationManager.getLastKnownLocation(provider)
            if (lastLocation != null) {
                Log.d(TAG, "Using last known location")
                latitude = lastLocation.latitude
                longitude = lastLocation.longitude
                binding.locationProgressBar.visibility = View.GONE
                binding.tvLocationStatus.text = "Lokasi terdeteksi: ${lastLocation.latitude}, ${lastLocation.longitude}"
                isRequestingLocation = false
                Toast.makeText(this, "Lokasi terdeteksi", Toast.LENGTH_SHORT).show()
                return
            } else {
                Log.d(TAG, "No last known location, requesting updates")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting last known location", e)
        }

        // Create a location listener
        val locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                Log.d(TAG, "onLocationChanged called: lat=${location.latitude}, long=${location.longitude}")
                latitude = location.latitude
                longitude = location.longitude
                binding.locationProgressBar.visibility = View.GONE
                binding.tvLocationStatus.text = "Lokasi terdeteksi: ${location.latitude}, ${location.longitude}"
                isRequestingLocation = false
                Toast.makeText(this@AddAddressActivity, "Lokasi terdeteksi", Toast.LENGTH_SHORT).show()

                // Remove location updates after receiving a location
                try {
                    locationManager.removeUpdates(this)
                } catch (e: Exception) {
                    Log.e(TAG, "Error removing location updates", e)
                }
            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
                Log.d(TAG, "Location provider status changed: provider=$provider, status=$status")
            }

            override fun onProviderEnabled(provider: String) {
                Log.d(TAG, "Location provider enabled: $provider")
            }

            override fun onProviderDisabled(provider: String) {
                Log.w(TAG, "Location provider disabled: $provider")
                binding.locationProgressBar.visibility = View.GONE
                binding.tvLocationStatus.text = "Provider lokasi dimatikan"
                isRequestingLocation = false
                Toast.makeText(this@AddAddressActivity, "Provider $provider dimatikan", Toast.LENGTH_SHORT).show()
            }
        }

        try {
            // Request location updates
            Log.d(TAG, "Requesting location updates from $provider")
            locationManager.requestLocationUpdates(
                provider,
                0,  // minimum time interval between updates (in milliseconds)
                0f, // minimum distance between updates (in meters)
                locationListener,
                Looper.getMainLooper()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Exception requesting location update", e)
            binding.locationProgressBar.visibility = View.GONE
            binding.tvLocationStatus.text = "Error: ${e.message}"
            isRequestingLocation = false
            Toast.makeText(this, "Error mendapatkan lokasi: ${e.message}", Toast.LENGTH_SHORT).show()

            // Set default location
            latitude = -6.200000
            longitude = 106.816666
        }
    }

    private fun showEnableLocationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Aktifkan Lokasi")
            .setMessage("Aplikasi memerlukan akses lokasi. Silakan aktifkan lokasi di pengaturan.")
            .setPositiveButton("Pengaturan") { _, _ ->
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            .setNegativeButton("Batal") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun setupReloadButtons() {
        // Add button to reload provinces (add this button to your layout)

        // Add button to reload location (add this button to your layout)
        binding.btnReloadLocation.setOnClickListener {
            Log.d(TAG, "Reload location button clicked")
            Toast.makeText(this, "Memuat ulang lokasi...", Toast.LENGTH_SHORT).show()
            requestLocation()
        }
    }

    companion object {
        private const val TAG = "AddAddressViewModel"
    }
}
