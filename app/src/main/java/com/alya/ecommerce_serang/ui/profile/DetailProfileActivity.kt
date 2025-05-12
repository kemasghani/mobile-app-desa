package com.alya.ecommerce_serang.ui.profile

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.alya.ecommerce_serang.R
import com.alya.ecommerce_serang.data.api.dto.UserProfile
import com.alya.ecommerce_serang.data.api.retrofit.ApiConfig
import com.alya.ecommerce_serang.data.api.retrofit.ApiService
import com.alya.ecommerce_serang.data.repository.UserRepository
import com.alya.ecommerce_serang.databinding.ActivityDetailProfileBinding
import com.alya.ecommerce_serang.utils.BaseViewModelFactory
import com.alya.ecommerce_serang.utils.SessionManager
import com.alya.ecommerce_serang.utils.viewmodel.ProfileViewModel
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class DetailProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailProfileBinding
    private lateinit var apiService: ApiService
    private lateinit var sessionManager: SessionManager

    private val viewModel: ProfileViewModel by viewModels {
        BaseViewModelFactory {
            val apiService = ApiConfig.getApiService(sessionManager)
            val userRepository = UserRepository(apiService)
            ProfileViewModel(userRepository)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        apiService = ApiConfig.getApiService(sessionManager)

        enableEdgeToEdge()
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }

        viewModel.loadUserProfile()

        viewModel.userProfile.observe(this){ user ->
            user?.let { updateProfile(it) }
        }

        viewModel.errorMessage.observe(this) { error ->
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateProfile(user: UserProfile){

        binding.tvNameUser.setText(user.name.toString())
        binding.tvUsername.setText(user.username)
        binding.tvEmailUser.setText(user.email)
        Log.d("ProfileActivity", "Raw Birth Date: ${user.birthDate}")
        binding.tvDateBirth.setText(user.birthDate?.let { formatDate(it) } ?: "N/A")
        Log.d("ProfileActivity", "Formatted Birth Date: ${formatDate(user.birthDate)}")
        binding.tvNumberPhoneUser.setText(user.phone)

        if (user.image != null && user.image is String) {
            Glide.with(this)
                .load(user.image)
                .placeholder(R.drawable.baseline_account_circle_24)
                .into(binding.profileImage)
        }
    }

    private fun formatDate(dateString: String?): String {
        if (dateString.isNullOrEmpty()) return "N/A" // Return default if null

        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC") // Ensure parsing in UTC

            val outputFormat = SimpleDateFormat("dd-MM-yy", Locale.getDefault()) // Convert to "dd-MM-yy" format
            val date = inputFormat.parse(dateString)
            outputFormat.format(date ?: return "Invalid Date") // Ensure valid date
        } catch (e: Exception) {
            Log.e("ERROR", "Date parsing error: ${e.message}") // Log errors for debugging
            "Invalid Date"
        }
    }

}