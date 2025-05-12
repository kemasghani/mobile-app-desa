package com.alya.ecommerce_serang.data.repository

import android.util.Log
import com.alya.ecommerce_serang.data.api.dto.LoginRequest
import com.alya.ecommerce_serang.data.api.dto.OtpRequest
import com.alya.ecommerce_serang.data.api.dto.RegisterRequest
import com.alya.ecommerce_serang.data.api.dto.UserProfile
import com.alya.ecommerce_serang.data.api.response.auth.LoginResponse
import com.alya.ecommerce_serang.data.api.response.auth.OtpResponse
import com.alya.ecommerce_serang.data.api.retrofit.ApiService
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class UserRepository(private val apiService: ApiService) {

    //post data without message/response
    suspend fun requestOtpRep(email: String): OtpResponse {
        return apiService.getOTP(OtpRequest(email))
    }

    suspend fun registerUser(request: RegisterRequest): String {
        val response = apiService.register(request) // API call

        if (response.isSuccessful) {
            val responseBody = response.body() ?: throw Exception("Empty response body")
            return responseBody.message // Get the message from RegisterResponse
        } else {
            throw Exception("Registration failed: ${response.errorBody()?.string()}")
        }
    }

    suspend fun login(email: String, password: String): Result<LoginResponse> {
        Log.d("LoginDebug", "UserRepository: Attempting login with email: $email")

        return try {
            Log.d("LoginDebug", "UserRepository: Creating login request")
            val loginRequest = LoginRequest(email, password)
            Log.d("LoginDebug", "UserRepository: Sending API request")

            val response = apiService.login(loginRequest)
            Log.d("LoginDebug", "UserRepository: Received response, code: ${response.code()}")

            if (response.isSuccessful) {
                try {
                    val body = response.body()
                    Log.d("LoginDebug", "UserRepository: Response successful, body: ${body != null}")

                    if (body != null) {
                        Log.d("LoginDebug", "UserRepository: Login successful, token obtained")
                        Result.Success(body)
                    } else {
                        Log.e("LoginDebug", "UserRepository: Response body is null")
                        Result.Error(Exception("Login response is empty"))
                    }
                } catch (e: JsonSyntaxException) {
                    // Specific handling for JSON parsing errors
                    Log.e("LoginDebug", "UserRepository: JSON parsing error", e)
                    Result.Error(Exception("Error parsing login response: ${e.message}", e))
                } catch (e: IllegalStateException) {
                    // Handle specific case of plain text response instead of JSON
                    Log.e("LoginDebug", "UserRepository: IllegalStateException", e)

                    if (e.message?.contains("expected BEGIN_OBJECT but was string") == true) {
                        // Try to get the raw response string
                        try {
                            val rawResponse = response.errorBody()?.string() ?:
                                             (response.raw().body as? ResponseBody)?.string() ?:
                                              "Unknown server response"

                            Log.e("LoginDebug", "Raw response from server: $rawResponse")

                            // If the raw response contains a token, try to parse it manually
                            if (rawResponse.contains("token") || rawResponse.contains("access")) {
                                try {
                                    // Try to parse it as JSON using JSONObject instead of JsonParser
                                    val jsonObject = JSONObject(rawResponse)
                                    val token = when {
                                        jsonObject.has("accessToken") -> jsonObject.getString("accessToken")
                                        jsonObject.has("access_token") -> jsonObject.getString("access_token")
                                        jsonObject.has("token") -> jsonObject.getString("token")
                                        else -> null
                                    }

                                    if (token != null) {
                                        Log.d("LoginDebug", "Managed to extract token manually")
                                        // Create a manual LoginResponse with the required parameters
                                        val loginResponse = LoginResponse(
                                            role = "user",  // Default role
                                            message = "Login successful",
                                            accessToken = token
                                        )
                                        return Result.Success(loginResponse)
                                    }
                                } catch (e2: Exception) {
                                    Log.e("LoginDebug", "Failed to manually parse response", e2)
                                }
                            }

                            Result.Error(Exception("Server returned invalid format: $rawResponse", e))
                        } catch (e2: Exception) {
                            Log.e("LoginDebug", "Failed to read raw response", e2)
                            Result.Error(Exception("Server returned invalid format and could not read response", e))
                        }
                    } else {
                        // Specific handling for other IllegalStateException errors
                        Result.Error(Exception("Invalid server response format: ${e.message}", e))
                    }
                }
            } else {
                // Handle error responses
                val errorBody = try {
                    val errorString = response.errorBody()?.string() ?: "Unknown error"
                    Log.e("LoginDebug", "Error response: code=${response.code()}, body=$errorString")
                    errorString
                } catch (e: IOException) {
                    Log.e("LoginDebug", "Error reading error response", e)
                    "Error reading error response: ${e.message}"
                } catch (e: Exception) {
                    Log.e("LoginDebug", "Unknown error parsing error response", e)
                    "Unknown error: ${e.message}"
                }
                Result.Error(Exception("Server error (${response.code()}): $errorBody"))
            }
        } catch (e: UnknownHostException) {
            // This happens when the host cannot be resolved (no internet or wrong URL)
            Log.e("LoginDebug", "UnknownHostException", e)
            Result.Error(Exception("Cannot reach server. Check your internet connection or server URL", e))
        } catch (e: ConnectException) {
            // This happens when the connection cannot be established
            Log.e("LoginDebug", "ConnectException", e)
            Result.Error(Exception("Connection failed. Server may be down or unreachable", e))
        } catch (e: SocketTimeoutException) {
            // This happens when the connection times out
            Log.e("LoginDebug", "SocketTimeoutException", e)
            Result.Error(Exception("Connection timed out. Server is taking too long to respond", e))
        } catch (e: HttpException) {
            // For HTTP errors that Retrofit might throw
            Log.e("LoginDebug", "HttpException: ${e.code()}", e)
            Result.Error(Exception("HTTP Error: ${e.code()} - ${e.message()}", e))
        } catch (e: IllegalStateException) {
            // This might happen if there's an issue with the response content
            Log.e("LoginDebug", "IllegalStateException", e)
            if (e.message?.contains("expected BEGIN_OBJECT but was string") == true) {
                Result.Error(Exception("Server returned plain text instead of JSON. Contact server administrator.", e))
            } else {
                Result.Error(Exception("Server response format error: ${e.message}", e))
            }
        } catch (e: JsonSyntaxException) {
            // For JSON parsing errors
            Log.e("LoginDebug", "JsonSyntaxException", e)
            Result.Error(Exception("Invalid JSON in response: ${e.message}", e))
        } catch (e: IOException) {
            // General network errors
            Log.e("LoginDebug", "IOException", e)
            Result.Error(Exception("Network error: ${e.message}", e))
        } catch (e: Exception) {
            // General exception handler
            Log.e("LoginDebug", "Unknown exception during login", e)
            Result.Error(Exception("Login failed: ${e.message}", e))
        }
    }

    suspend fun fetchUserProfile(): Result<UserProfile?> {
        return try {
            val response = apiService.getUserProfile()
            if (response.isSuccessful) {
                response.body()?.user?.let {
                    Result.Success(it)  // ✅ Returning only UserProfile
                } ?: Result.Error(Exception("User data not found"))
            } else {
                Result.Error(Exception("Error fetching profile: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }


}

sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable) : Result<Nothing>()
    object Loading : Result<Nothing>()
}