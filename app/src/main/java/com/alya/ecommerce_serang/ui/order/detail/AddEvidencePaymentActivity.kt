package com.alya.ecommerce_serang.ui.order.detail

import android.Manifest
import android.R
import android.app.DatePickerDialog
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.alya.ecommerce_serang.data.api.dto.AddEvidenceMultipartRequest
import com.alya.ecommerce_serang.data.api.retrofit.ApiConfig
import com.alya.ecommerce_serang.data.repository.OrderRepository
import com.alya.ecommerce_serang.data.repository.Result
import com.alya.ecommerce_serang.databinding.ActivityAddEvidencePaymentBinding
import com.alya.ecommerce_serang.utils.BaseViewModelFactory
import com.alya.ecommerce_serang.utils.SessionManager
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddEvidencePaymentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddEvidencePaymentBinding
    private lateinit var sessionManager: SessionManager
    private var orderId: Int = 0
    private var paymentInfoId: Int = 0
    private lateinit var productPrice: String
    private var selectedImageUri: Uri? = null

    private val viewModel: PaymentViewModel by viewModels {
        BaseViewModelFactory {
            val apiService = ApiConfig.getApiService(sessionManager)
            val orderRepository = OrderRepository(apiService)
            PaymentViewModel(orderRepository)
        }
    }

    private val paymentMethods = arrayOf(
        "Pilih metode pembayaran",
        "Transfer Bank",
        "E-Wallet",
        "Virtual Account",
        "Cash on Delivery"
    )

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            binding.ivUploadedImage.setImageURI(selectedImageUri)
            binding.ivUploadedImage.visibility = View.VISIBLE
            binding.layoutUploadPlaceholder.visibility = View.GONE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEvidencePaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        intent.extras?.let { bundle ->
            orderId = bundle.getInt("ORDER_ID", 0)
            paymentInfoId = bundle.getInt("PAYMENT_INFO_ID", 0)
            productPrice = intent.getStringExtra("TOTAL_AMOUNT") ?: "Rp0"

        }

        setupUI()
        viewModel.getOrderDetails(orderId)


        setupListeners()
        setupObservers()


    }

    private fun setupUI() {
        // Set product details\

        // Setup payment methods spinner
        val adapter = ArrayAdapter(this, R.layout.simple_spinner_item, paymentMethods)
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        binding.spinnerPaymentMethod.adapter = adapter
    }

    private fun setupListeners() {

        // Upload image button
        binding.tvAddPhoto.setOnClickListener {
            checkPermissionAndPickImage()
        }

        binding.frameUploadImage.setOnClickListener {
            checkPermissionAndPickImage()
        }

        // Date picker
        binding.tvPaymentDate.setOnClickListener {
            showDatePicker()
        }

        // Payment method spinner
        binding.spinnerPaymentMethod.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Skip the hint (first item)
                if (position > 0) {
                    val selectedMethod = paymentMethods[position]

                    // You can also use it for further processing
                    Log.d(TAG, "Selected payment method: $selectedMethod")
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }

        // Submit button
        binding.btnSubmit.setOnClickListener {
            validateAndUpload()
        }
    }

    private fun setupObservers() {
        viewModel.uploadResult.observe(this) { result ->
            when (result) {
                is com.alya.ecommerce_serang.data.repository.Result.Success -> {
                    Toast.makeText(this, "Bukti pembayaran berhasil dikirim", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "Upload successful: ${result.data}")
                    // Navigate back or to confirmation screen
                    finish()
                }
                is com.alya.ecommerce_serang.data.repository.Result.Error -> {
                    Log.e(TAG, "Upload failed: ${result.exception.message}")
                    Toast.makeText(this, "Gagal mengirim bukti pembayaran: ${result.exception.message}", Toast.LENGTH_SHORT).show()
                }
                is Result.Loading -> {
                    // Show loading indicator if needed
                    Log.d(TAG, "Uploading payment proof...")
                }
            }
        }
    }

    private fun validateAndUpload() {
        // Validate all fields
        if (selectedImageUri == null) {
            Toast.makeText(this, "Silahkan pilih bukti pembayaran", Toast.LENGTH_SHORT).show()
            return
        }

        if (binding.spinnerPaymentMethod.selectedItemPosition == 0) {
            Toast.makeText(this, "Silahkan pilih metode pembayaran", Toast.LENGTH_SHORT).show()
            return
        }

        if (binding.etAccountNumber.text.toString().trim().isEmpty()) {
            Toast.makeText(this, "Silahkan isi nomor rekening/HP", Toast.LENGTH_SHORT).show()
            return
        }

        if (binding.tvPaymentDate.text.toString() == "Pilih tanggal") {
            Toast.makeText(this, "Silahkan pilih tanggal pembayaran", Toast.LENGTH_SHORT).show()
            return
        }

        // All validations passed, proceed with upload
        uploadPaymentProof()

    }

    private fun uploadPaymentProof() {
        selectedImageUri?.let { uri ->
            // Convert URI to File
            val file = getFileFromUri(uri)
            file?.let {
                try {
                    // Create MultipartBody.Part from File
                    val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                    val evidencePart = MultipartBody.Part.createFormData("evidence", file.name, requestFile)

                    // Create RequestBody for order ID and amount
                    val orderIdPart = orderId.toString().toRequestBody("text/plain".toMediaTypeOrNull())

                    // Clean up the price string to get only the numeric value
                    val amountPart = productPrice.replace("Rp", "").replace(".", "").trim()
                        .toRequestBody("text/plain".toMediaTypeOrNull())

                    // Create the request object with the parts we need
                    val request = AddEvidenceMultipartRequest(
                        orderId = orderIdPart,
                        amount = amountPart,
                        evidence = evidencePart
                    )

                    // Log request details for debugging
                    Log.d(TAG, "Uploading payment proof - OrderID: $orderId, Amount: ${productPrice.replace("Rp", "").replace(".", "").trim()}")
                    Log.d(TAG, "File details - Name: ${file.name}, Size: ${file.length()} bytes, MIME: image/jpeg")

                    // Call the viewModel method
                    viewModel.uploadPaymentProof(request)
                } catch (e: Exception) {
                    Log.e(TAG, "Error creating upload request: ${e.message}", e)
                    Toast.makeText(this, "Error preparing upload: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getFileFromUri(uri: Uri): File? {
        val contentResolver = applicationContext.contentResolver
        val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
        val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "jpg"

        Log.d("UploadEvidence", "URI: $uri")
        Log.d("UploadEvidence", "Detected MIME type: $mimeType, extension: $extension")

        // Ensure it's an image (either PNG, JPG, or JPEG)
        if (mimeType != "image/png" && mimeType != "image/jpeg" && mimeType != "image/jpg") {
            Log.e("UploadEvidence", "Invalid image MIME type: $mimeType. Only images are allowed.")
            Toast.makeText(applicationContext, "Only image files are allowed", Toast.LENGTH_SHORT).show()
            return null
        }

        try {
            val inputStream = contentResolver.openInputStream(uri)

            if (inputStream == null) {
                Log.e("UploadEvidence", "Failed to open input stream from URI: $uri")
                return null
            }

            // Create a temporary file with the correct extension
            val tempFile = File.createTempFile("evidence_", ".$extension", cacheDir)
            Log.d("UploadEvidence", "Temp file created at: ${tempFile.absolutePath}")

            // Copy the content from inputStream to the temporary file
            tempFile.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
                inputStream.close()
            }

            // Verify if the file is a valid image
            val bitmap = BitmapFactory.decodeFile(tempFile.absolutePath)
            if (bitmap == null) {
                Log.e("UploadEvidence", "File is not a valid image!")
                tempFile.delete()
                return null
            } else {
                bitmap.recycle()  // Free memory
                Log.d("UploadEvidence", "Valid image detected.")
            }

            Log.d("UploadEvidence", "File copied successfully. Size: ${tempFile.length()} bytes")
            return tempFile
        } catch (e: Exception) {
            Log.e("UploadEvidence", "Error processing file: ${e.message}", e)
            Toast.makeText(applicationContext, "Error processing image: ${e.message}", Toast.LENGTH_SHORT).show()
            return null
        }
    }




    private fun checkPermissionAndPickImage() {
        val permission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), REQUEST_CODE_STORAGE_PERMISSION)
        } else {
            pickImage()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            pickImage()
        } else {
            Toast.makeText(this, "Izin dibutuhkan untuk memilih gambar", Toast.LENGTH_SHORT).show()
        }
    }

    private fun pickImage() {
        getContent.launch("image/*")
    }


    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                calendar.set(selectedYear, selectedMonth, selectedDay)
                val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                binding.tvPaymentDate.text = sdf.format(calendar.time)
            },
            year, month, day
        ).show()
    }


    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
        private const val TAG = "AddEvidenceActivity"
        private const val REQUEST_CODE_STORAGE_PERMISSION = 100

    }
}