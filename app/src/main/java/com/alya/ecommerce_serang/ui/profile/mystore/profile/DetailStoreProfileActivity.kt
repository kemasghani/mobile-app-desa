package com.alya.ecommerce_serang.ui.profile.mystore.profile

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.alya.ecommerce_serang.R
import com.alya.ecommerce_serang.data.api.dto.Store
import com.alya.ecommerce_serang.data.api.retrofit.ApiConfig
import com.alya.ecommerce_serang.data.api.retrofit.ApiService
import com.alya.ecommerce_serang.data.repository.MyStoreRepository
import com.alya.ecommerce_serang.databinding.ActivityDetailStoreProfileBinding
import com.alya.ecommerce_serang.utils.viewmodel.MyStoreViewModel
import com.alya.ecommerce_serang.utils.BaseViewModelFactory
import com.alya.ecommerce_serang.utils.SessionManager
import com.alya.ecommerce_serang.ui.profile.mystore.address.StoreAddressActivity
import com.bumptech.glide.Glide
import kotlin.getValue

class DetailStoreProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailStoreProfileBinding
    private lateinit var apiService: ApiService
    private lateinit var sessionManager: SessionManager

    private val viewModel: MyStoreViewModel by viewModels {
        BaseViewModelFactory {
            val apiService = ApiConfig.getApiService(sessionManager)
            val myStoreRepository = MyStoreRepository(apiService)
            MyStoreViewModel(myStoreRepository)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailStoreProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        apiService = ApiConfig.getApiService(sessionManager)

        enableEdgeToEdge()

        // Set up header title
        val headerTitle = findViewById<TextView>(R.id.header_title)
        headerTitle.text = "Profil Toko"

        // Set up back button
        val backButton = findViewById<ImageView>(R.id.header_left_icon)
        backButton.setOnClickListener {
            finish()
        }

        // Set up edit button
        binding.btnEditStoreProfile.setOnClickListener {
            val intent = Intent(this, EditStoreProfileActivity::class.java)
            startActivityForResult(intent, EDIT_PROFILE_REQUEST_CODE)
        }

        // Set up address layout click listener
        binding.layoutAddress.setOnClickListener {
            val intent = Intent(this, StoreAddressActivity::class.java)
            startActivityForResult(intent, ADDRESS_REQUEST_CODE)
        }

        viewModel.loadMyStore()

        viewModel.myStoreProfile.observe(this){ user ->
            user?.let { updateStoreProfile(it) }
        }

        viewModel.errorMessage.observe(this) { error ->
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == EDIT_PROFILE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // Refresh the profile data
            Toast.makeText(this, "Profil toko berhasil diperbarui", Toast.LENGTH_SHORT).show()
            viewModel.loadMyStore()

            // Pass the result back to parent activity
            setResult(Activity.RESULT_OK)
        } else if (requestCode == ADDRESS_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // Refresh the profile data after address update
            Toast.makeText(this, "Alamat toko berhasil diperbarui", Toast.LENGTH_SHORT).show()
            viewModel.loadMyStore()

            // Pass the result back to parent activity
            setResult(Activity.RESULT_OK)
        }
    }

    companion object {
        private const val EDIT_PROFILE_REQUEST_CODE = 100
        private const val ADDRESS_REQUEST_CODE = 101
    }

    private fun updateStoreProfile(store: Store){
        // Update text fields
        binding.edtNamaToko.setText(store.storeName.toString())
        binding.edtJenisToko.setText(store.storeType.toString())
        binding.edtDeskripsiToko.setText(store.storeDescription.toString())

        // Update store image if available
        if (store.storeImage != null && store.storeImage.toString().isNotEmpty() && store.storeImage.toString() != "null") {
            val imageUrl = "http://192.168.100.31:3000${store.storeImage}"
            Log.d("DetailStoreProfile", "Loading image from: $imageUrl")

            Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.placeholder_image)
                .into(binding.ivProfile)
        } else {
            Log.d("DetailStoreProfile", "No store image available")
        }
    }
}