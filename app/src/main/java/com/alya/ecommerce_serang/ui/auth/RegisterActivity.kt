package com.alya.ecommerce_serang.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.alya.ecommerce_serang.data.api.dto.RegisterRequest
import com.alya.ecommerce_serang.data.api.retrofit.ApiConfig
import com.alya.ecommerce_serang.data.repository.Result
import com.alya.ecommerce_serang.data.repository.UserRepository
import com.alya.ecommerce_serang.databinding.ActivityRegisterBinding
import com.alya.ecommerce_serang.ui.MainActivity
import com.alya.ecommerce_serang.utils.BaseViewModelFactory
import com.alya.ecommerce_serang.utils.SessionManager
import com.alya.ecommerce_serang.utils.viewmodel.RegisterViewModel

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var sessionManager: SessionManager
    private val registerViewModel: RegisterViewModel by viewModels{
        BaseViewModelFactory {
            val apiService = ApiConfig.getUnauthenticatedApiService()
            val userRepository = UserRepository(apiService)
            RegisterViewModel(userRepository)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sessionManager = SessionManager(this)
        if (!sessionManager.getToken().isNullOrEmpty()) {
            // User already logged in, redirect to MainActivity
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        enableEdgeToEdge()
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Observe OTP state
        observeOtpState()

        binding.btnSignup.setOnClickListener {
            // Retrieve values inside the click listener (so we get latest input)
            val birthDate = binding.etBirthDate.text.toString()
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            val phone = binding.etNumberPhone.text.toString()
            val username = binding.etUsername.text.toString()
            val name = binding.etFullname.text.toString()
            val image = "not yet"

            val userData = RegisterRequest(name, email, password, username, phone, birthDate, image)

            Log.d("RegisterActivity", "Requesting OTP for email: $email")

            // Request OTP and wait for success before showing dialog
            registerViewModel.requestOtp(userData.email.toString())

            // Observe OTP state and show OTP dialog only when successful
            registerViewModel.otpState.observe(this) { result ->
                when (result) {
                    is Result.Success -> {
                        Log.d("RegisterActivity", "OTP sent successfully. Showing OTP dialog.")
                        // Show OTP dialog after OTP is successfully sent
                        val otpBottomSheet = OtpBottomSheetDialog(userData) { fullUserData ->
                            Log.d("RegisterActivity", "OTP entered successfully. Proceeding with registration.")
                            registerViewModel.registerUser(fullUserData) // Send complete data
                        }
                        otpBottomSheet.show(supportFragmentManager, "OtpBottomSheet")
                    }
                    is Result.Error -> {
                        // Show error message if OTP request fails
                        Log.e("RegisterActivity", "Failed to request OTP: ${result.exception.message}")
                        Toast.makeText(this, "Failed to request OTP: ${result.exception.message}", Toast.LENGTH_LONG).show()
                    }
                    is Result.Loading -> {
                        // Optional: Show loading indicator
                    }
                }
            }

            // Observe Register state
            observeRegisterState()
        }

        binding.tvLoginAlt.setOnClickListener{
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

    }

    private fun observeOtpState() {
            registerViewModel.otpState.observe(this) { result ->
                when (result) {
                    is Result.Loading -> {
                        // Show loading indicator
                        binding.progressBarOtp.visibility = android.view.View.VISIBLE
                    }
                    is Result.Success -> {
                        // Hide loading indicator and show success message
                        binding.progressBarOtp.visibility = android.view.View.GONE
//                        Toast.makeText(this@RegisterActivity, result.data, Toast.LENGTH_SHORT).show()
                    }
                    is Result.Error -> {
                        // Hide loading indicator and show error message
                        binding.progressBarOtp.visibility = android.view.View.GONE
                        Toast.makeText(this, "OTP Request Failed: ${result.exception.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

    private fun observeRegisterState() {
            registerViewModel.registerState.observe(this) { result ->
                when (result) {
                    is Result.Loading -> {
                        // Show loading indicator for registration
                        binding.progressBarRegister.visibility = android.view.View.VISIBLE
                    }
                    is Result.Success -> {
                        // Hide loading indicator and show success message
                        binding.progressBarRegister.visibility = android.view.View.GONE
                        Toast.makeText(this, result.data, Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                        // Navigate to another screen if needed
                    }
                    is com.alya.ecommerce_serang.data.repository.Result.Error -> {
                        // Hide loading indicator and show error message
                        binding.progressBarRegister.visibility = android.view.View.GONE
                        Toast.makeText(this, "Registration Failed: ${result.exception.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }
}