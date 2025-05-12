package com.alya.ecommerce_serang.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.alya.ecommerce_serang.BuildConfig
import com.alya.ecommerce_serang.data.api.retrofit.ApiConfig
import com.alya.ecommerce_serang.data.repository.Result
import com.alya.ecommerce_serang.data.repository.UserRepository
import com.alya.ecommerce_serang.databinding.ActivityLoginBinding
import com.alya.ecommerce_serang.ui.MainActivity
import com.alya.ecommerce_serang.utils.BaseViewModelFactory
import com.alya.ecommerce_serang.utils.SessionManager
import com.alya.ecommerce_serang.utils.viewmodel.LoginViewModel
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val loginViewModel: LoginViewModel by viewModels{
        BaseViewModelFactory {
            val apiService = ApiConfig.getUnauthenticatedApiService()
            val userRepository = UserRepository(apiService)
            LoginViewModel(userRepository)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Log app and device information for debugging
        logDebugInfo()

        setupListeners()
        observeLoginState()
    }

    private fun logDebugInfo() {
        Log.d("LoginDebug", "App version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
        Log.d("LoginDebug", "Base URL: ${BuildConfig.BASE_URL}")
        Log.d("LoginDebug", "Device: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}")
        Log.d("LoginDebug", "Android version: ${android.os.Build.VERSION.RELEASE} (SDK ${android.os.Build.VERSION.SDK_INT})")

        // Check if a token already exists
        val sessionManager = SessionManager(this)
        val token = sessionManager.getToken()
        Log.d("LoginDebug", "Existing token: ${if (token.isNullOrEmpty()) "None" else "Token exists"}")
    }

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etLoginEmail.text.toString()
            val password = binding.etLoginPassword.text.toString()

            // Log login attempt
            Log.d("LoginDebug", "Login attempt with email: $email")

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else {
                // Show loading indicator
                binding.progressBarLogin.visibility = View.VISIBLE
                binding.btnLogin.isEnabled = false

                loginViewModel.login(email, password)
            }
        }

        // Add debug button for testing connection
        binding.root.post {
            val debugButton = binding.tvForgetPassword // Using the forget password text as debug button
            debugButton.setOnClickListener {
                testServerConnection()
            }
        }
    }

    private fun testServerConnection() {
        Toast.makeText(this, "Testing server connection...", Toast.LENGTH_SHORT).show()

        Thread {
            try {
                val url = java.net.URL(BuildConfig.BASE_URL)
                val connection = url.openConnection() as java.net.HttpURLConnection
                connection.connectTimeout = 5000
                connection.connect()

                val responseCode = connection.responseCode
                val message = "Server connection test: $responseCode"

                runOnUiThread {
                    Log.d("LoginDebug", message)
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

                    AlertDialog.Builder(this)
                        .setTitle("Connection Test")
                        .setMessage("Server response code: $responseCode\nURL: ${BuildConfig.BASE_URL}")
                        .setPositiveButton("OK", null)
                        .show()
                }
            } catch (e: Exception) {
                val errorMessage = "Connection error: ${e.message}"
                Log.e("LoginDebug", errorMessage, e)

                runOnUiThread {
                    Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()

                    AlertDialog.Builder(this)
                        .setTitle("Connection Test Failed")
                        .setMessage("Error: ${e.message}\n\nType: ${e.javaClass.simpleName}")
                        .setPositiveButton("OK", null)
                        .show()
                }
            }
        }.start()
    }

    private fun observeLoginState() {
        loginViewModel.loginState.observe(this) { result ->
            // Hide loading indicator
            binding.progressBarLogin.visibility = View.GONE
            binding.btnLogin.isEnabled = true

            when (result) {
                is com.alya.ecommerce_serang.data.repository.Result.Success -> {
                    val accessToken = result.data.accessToken
                    Log.d("LoginDebug", "Login successful, token received")

                    val sessionManager = SessionManager(this)
                    sessionManager.saveToken(accessToken)

                    Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()

                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                is com.alya.ecommerce_serang.data.repository.Result.Error -> {
                    // Log the full error for debugging
                    Log.e("LoginDebug", "Login error: ${result.exception.message}", result.exception)
                    Log.e("LoginDebug", "Exception type: ${result.exception.javaClass.simpleName}")
                    Log.e("LoginDebug", "Cause: ${result.exception.cause?.javaClass?.simpleName}")

                    val errorMessage = when {
                        // Special handling for the specific error we're seeing
                        result.exception.message?.contains("expected BEGIN_OBJECT but was string") == true ->
                            "Server returned unexpected format. The development team has been notified."

                        // Connection errors
                        result.exception.cause is UnknownHostException ->
                            "Server cannot be reached. Please check your internet connection and server URL."
                        result.exception.cause is ConnectException ->
                            "Could not connect to server. Please check if the server is running."
                        result.exception.cause is SocketTimeoutException ->
                            "Connection timed out. Server might be slow or unavailable."

                        // JSON or parsing errors
                        result.exception.message?.contains("JsonSyntax") == true ->
                            "Server returned invalid data. Please try again later."
                        result.exception.message?.contains("IllegalState") == true ->
                            "Server response error. Please contact support."
                        result.exception.message?.contains("response format") == true ->
                            "Server response format error. Please try again later."

                        // Authentication errors
                        result.exception.message?.contains("401") == true ->
                            "Invalid email or password. Please try again."
                        result.exception.message?.contains("response is empty") == true ->
                            "Login failed. Please check your credentials."

                        // Generic error fallback
                        else -> "Login Failed: ${result.exception.message}"
                    }

                    // Show a dialog with more detailed error information
                    AlertDialog.Builder(this)
                        .setTitle("Login Failed")
                        .setMessage(errorMessage)
                        .setPositiveButton("OK", null)
                        .setNeutralButton("Details") { _, _ ->
                            AlertDialog.Builder(this)
                                .setTitle("Technical Details")
                                .setMessage("Error: ${result.exception.message}\n\n" +
                                           "Type: ${result.exception.javaClass.simpleName}\n\n" +
                                           "Cause: ${result.exception.cause?.javaClass?.simpleName}: ${result.exception.cause?.message}\n\n" +
                                           "URL: ${BuildConfig.BASE_URL}")
                                .setPositiveButton("OK", null)
                                .show()
                        }
                        .show()
                }
                is Result.Loading -> {
                    // Loading state already handled by visibility changes
                    Log.d("LoginDebug", "Login in progress...")
                }
            }
        }
    }
}