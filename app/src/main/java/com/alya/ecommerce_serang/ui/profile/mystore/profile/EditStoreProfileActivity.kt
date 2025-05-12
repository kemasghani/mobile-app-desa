package com.alya.ecommerce_serang.ui.profile.mystore.profile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.alya.ecommerce_serang.R
import com.alya.ecommerce_serang.data.api.dto.Store
import com.alya.ecommerce_serang.data.api.retrofit.ApiConfig
import com.alya.ecommerce_serang.databinding.ActivityEditStoreProfileBinding
import com.alya.ecommerce_serang.utils.SessionManager
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

class EditStoreProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditStoreProfileBinding
    private lateinit var sessionManager: SessionManager
    private var storeImageUri: Uri? = null
    private lateinit var currentStore: Store

    private val pickImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                storeImageUri = uri
                Log.d("EditStoreProfile", "Image selected: $uri")

                // Set the image to the ImageView for immediate preview
                try {
                    binding.ivStoreImage.setImageURI(null) // Clear any previous image
                    binding.ivStoreImage.setImageURI(uri)

                    // Alternative way using Glide for more reliable preview
                    Glide.with(this)
                        .load(uri)
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.placeholder_image)
                        .into(binding.ivStoreImage)

                    Toast.makeText(this, "Gambar berhasil dipilih", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Log.e("EditStoreProfile", "Error displaying image preview", e)
                    Toast.makeText(this, "Gagal menampilkan preview gambar", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditStoreProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        // Set up header
        binding.header.headerTitle.text = "Edit Profil Toko"
        binding.header.headerLeftIcon.setOnClickListener { finish() }

        loadStoreData()
        setupClickListeners()
    }

    private fun loadStoreData() {
        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val response = ApiConfig.getApiService(sessionManager).getStore()
                binding.progressBar.visibility = View.GONE

                if (response.isSuccessful && response.body() != null) {
                    currentStore = response.body()!!.store
                    populateFields(currentStore)
                } else {
                    showError("Gagal memuat profil toko")
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                showError("Terjadi kesalahan: ${e.message}")
            }
        }
    }

    private fun populateFields(store: Store) {
        // Load store image
        if (store.storeImage != null && store.storeImage.toString().isNotEmpty() && store.storeImage.toString() != "null") {
            Glide.with(this)
                .load(store.storeImage.toString())
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.placeholder_image)
                .into(binding.ivStoreImage)
        }

        // Set other fields
        binding.edtStoreName.setText(store.storeName)
        binding.edtDescription.setText(store.storeDescription)
        binding.edtUserPhone.setText(store.userPhone)

        // Set is on leave
        binding.switchIsOnLeave.isChecked = store.isOnLeave
    }

    private fun setupClickListeners() {
        binding.btnSelectStoreImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImage.launch(intent)
        }

        binding.btnSave.setOnClickListener {
            saveStoreProfile()
        }
    }

    private fun saveStoreProfile() {
        val storeName = binding.edtStoreName.text.toString()
        val storeDescription = binding.edtDescription.text.toString()
        val userPhone = binding.edtUserPhone.text.toString()
        val storeStatus = currentStore.storeStatus // Keep the current status
        val isOnLeave = binding.switchIsOnLeave.isChecked

        if (storeName.isEmpty() || userPhone.isEmpty()) {
            showError("Nama toko dan nomor telepon harus diisi")
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.btnSave.isEnabled = false

        // Show progress indicator on the image if we're uploading one
        if (storeImageUri != null) {
            binding.progressImage.visibility = View.VISIBLE
        }

        lifecycleScope.launch {
            try {
                Log.d("EditStoreProfile", "Starting profile update process")

                // Create multipart request for image if selected
                var storeImagePart: MultipartBody.Part? = null
                if (storeImageUri != null) {
                    try {
                        val storeImageFile = uriToFile(storeImageUri!!)
                        Log.d("EditStoreProfile", "Image file created: ${storeImageFile.name}, size: ${storeImageFile.length()}")

                        // Get the MIME type
                        val mimeType = contentResolver.getType(storeImageUri!!) ?: "image/jpeg"
                        Log.d("EditStoreProfile", "MIME type: $mimeType")

                        val storeImageRequestBody = storeImageFile.asRequestBody(mimeType.toMediaTypeOrNull())
                        storeImagePart = MultipartBody.Part.createFormData("storeimg", storeImageFile.name, storeImageRequestBody)
                        Log.d("EditStoreProfile", "Image part created with name: storeimg, filename: ${storeImageFile.name}")
                    } catch (e: Exception) {
                        Log.e("EditStoreProfile", "Error creating image part", e)
                        runOnUiThread {
                            Toast.makeText(this@EditStoreProfileActivity, "Error preparing image: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                // Create text parts
                val nameRequestBody = storeName.toRequestBody("text/plain".toMediaTypeOrNull())
                val descriptionRequestBody = storeDescription.toRequestBody("text/plain".toMediaTypeOrNull())
                val userPhoneRequestBody = userPhone.toRequestBody("text/plain".toMediaTypeOrNull())
                val statusRequestBody = storeStatus.toRequestBody("text/plain".toMediaTypeOrNull())
                val onLeaveRequestBody = isOnLeave.toString().toRequestBody("text/plain".toMediaTypeOrNull())

                // Log request parameters
                Log.d("EditStoreProfile", "Request parameters: " +
                        "\nstore_name: $storeName" +
                        "\nstore_status: $storeStatus" +
                        "\nstore_description: $storeDescription" +
                        "\nis_on_leave: $isOnLeave" +
                        "\nuser_phone: $userPhone" +
                        "\nimage: ${storeImageUri != null}")

                // Log all parts for debugging
                Log.d("EditStoreProfile", "Request parts:" +
                        "\nstoreName: $nameRequestBody" +
                        "\nstoreStatus: $statusRequestBody" +
                        "\nstoreDescription: $descriptionRequestBody" +
                        "\nisOnLeave: $onLeaveRequestBody" +
                        "\nuserPhone: $userPhoneRequestBody" +
                        "\nstoreimg: ${storeImagePart != null}")

                val response = ApiConfig.getApiService(sessionManager).updateStoreProfileMultipart(
                    storeName = nameRequestBody,
                    storeStatus = statusRequestBody,
                    storeDescription = descriptionRequestBody,
                    isOnLeave = onLeaveRequestBody,
                    cityId = currentStore.cityId.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                    provinceId = currentStore.provinceId.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                    street = currentStore.street.toRequestBody("text/plain".toMediaTypeOrNull()),
                    subdistrict = currentStore.subdistrict.toRequestBody("text/plain".toMediaTypeOrNull()),
                    detail = currentStore.detail.toRequestBody("text/plain".toMediaTypeOrNull()),
                    postalCode = currentStore.postalCode.toRequestBody("text/plain".toMediaTypeOrNull()),
                    latitude = currentStore.latitude.toRequestBody("text/plain".toMediaTypeOrNull()),
                    longitude = currentStore.longitude.toRequestBody("text/plain".toMediaTypeOrNull()),
                    userPhone = userPhoneRequestBody,
                    storeimg = storeImagePart
                )

                Log.d("EditStoreProfile", "Response received: isSuccessful=${response.isSuccessful}, code=${response.code()}")

                runOnUiThread {
                    binding.progressBar.visibility = View.GONE
                    binding.progressImage.visibility = View.GONE
                    binding.btnSave.isEnabled = true

                    if (response.isSuccessful) {
                        Log.d("EditStoreProfile", "Response body: ${response.body()?.toString()}")
                        // Try to log the updated store image URL
                        response.body()?.let { responseBody ->
                            val updatedStoreImage = responseBody.store?.storeImage
                            Log.d("EditStoreProfile", "Updated store image URL: $updatedStoreImage")
                        }
                        showSuccess("Profil toko berhasil diperbarui")
                        setResult(Activity.RESULT_OK)
                        finish()
                    } else {
                        val errorBodyString = response.errorBody()?.string() ?: "Error body is null"
                        Log.e("EditStoreProfile", "Full error response: $errorBodyString")
                        Log.e("EditStoreProfile", "Response headers: ${response.headers()}")
                        showError("Gagal memperbarui profil toko (${response.code()})")
                    }
                }
            } catch (e: Exception) {
                Log.e("EditStoreProfile", "Exception during API call", e)

                runOnUiThread {
                    binding.progressBar.visibility = View.GONE
                    binding.progressImage.visibility = View.GONE
                    binding.btnSave.isEnabled = true
                    showError("Error: ${e.message}")
                }
            }
        }
    }

    private fun uriToFile(uri: Uri): File {
        val contentResolver = applicationContext.contentResolver
        val fileExtension = getFileExtension(contentResolver, uri)
        val timeStamp = System.currentTimeMillis()
        val fileName = "IMG_${timeStamp}.$fileExtension"
        val tempFile = File(cacheDir, fileName)

        Log.d("EditStoreProfile", "Creating temp file: ${tempFile.absolutePath}")

        try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(tempFile).use { outputStream ->
                    val buffer = ByteArray(4 * 1024) // 4k buffer
                    var bytesRead: Int
                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                    }
                    outputStream.flush()
                }
            }

            Log.d("EditStoreProfile", "File created successfully: ${tempFile.name}, size: ${tempFile.length()} bytes")
            return tempFile
        } catch (e: Exception) {
            Log.e("EditStoreProfile", "Error creating file from URI", e)
            throw e
        }
    }

    private fun getFileExtension(contentResolver: android.content.ContentResolver, uri: Uri): String {
        val mimeType = contentResolver.getType(uri)
        return if (mimeType != null) {
            val mime = android.webkit.MimeTypeMap.getSingleton()
            mime.getExtensionFromMimeType(mimeType) ?: "jpg"
        } else {
            // If mime type is null, try to get from URI path
            val path = uri.path
            if (path != null) {
                val extension = android.webkit.MimeTypeMap.getFileExtensionFromUrl(path)
                if (!extension.isNullOrEmpty()) {
                    extension
                } else "jpg"
            } else "jpg"
        }
    }

    private fun showSuccess(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}