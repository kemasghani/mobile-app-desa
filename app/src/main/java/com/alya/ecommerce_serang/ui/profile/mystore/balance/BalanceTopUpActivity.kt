package com.alya.ecommerce_serang.ui.profile.mystore.balance

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.alya.ecommerce_serang.R
import com.alya.ecommerce_serang.data.api.dto.PaymentMethod
import com.alya.ecommerce_serang.data.api.response.store.product.Payment
import com.alya.ecommerce_serang.data.api.retrofit.ApiConfig
import com.alya.ecommerce_serang.utils.SessionManager
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class BalanceTopUpActivity : AppCompatActivity() {
    private lateinit var imgPreview: ImageView
    private lateinit var addPhotoTextView: TextView
    private lateinit var edtNominal: EditText
    private lateinit var spinnerPaymentMethod: Spinner
    private lateinit var edtTransactionDate: EditText
    private lateinit var datePickerIcon: ImageView
    private lateinit var btnSend: Button
    private lateinit var sessionManager: SessionManager

    private var selectedImageUri: Uri? = null
    private var paymentMethods: List<PaymentMethod> = emptyList()
    private var selectedPaymentId: Int = -1

    private val calendar = Calendar.getInstance()

    private val getImageContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val imageUri = result.data?.data
            imageUri?.let {
                selectedImageUri = it
                imgPreview.setImageURI(it)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_balance_top_up)

        // Initialize session manager
        sessionManager = SessionManager(this)

        // Initialize views
        imgPreview = findViewById(R.id.img_preview)
        addPhotoTextView = findViewById(R.id.tv_tambah_foto)
        edtNominal = findViewById(R.id.edt_nominal_topup)
        spinnerPaymentMethod = findViewById(R.id.spinner_metode_bayar)
        edtTransactionDate = findViewById(R.id.edt_tgl_transaksi)
        datePickerIcon = findViewById(R.id.img_date_picker)
        btnSend = findViewById(R.id.btn_send)

        // Setup header title
        val headerTitle = findViewById<TextView>(R.id.header_title)
        headerTitle.text = "Isi Ulang Saldo"

        // Setup back button
        val backButton = findViewById<ImageView>(R.id.header_left_icon)
        backButton.setOnClickListener {
            finish()
        }

        // Setup photo selection
        addPhotoTextView.setOnClickListener {
            openGallery()
        }

        imgPreview.setOnClickListener {
            openGallery()
        }

        // Setup date picker
        setupDatePicker()

        // Fetch payment methods
        fetchPaymentMethods()

        // Setup submit button
        btnSend.setOnClickListener {
            submitForm()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        getImageContent.launch(intent)
    }

    private fun setupDatePicker() {
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateInView()
        }

        edtTransactionDate.setOnClickListener {
            showDatePicker(dateSetListener)
        }

        datePickerIcon.setOnClickListener {
            showDatePicker(dateSetListener)
        }
    }

    private fun showDatePicker(dateSetListener: DatePickerDialog.OnDateSetListener) {
        DatePickerDialog(
            this,
            dateSetListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updateDateInView() {
        val format = "yyyy-MM-dd"
        val sdf = SimpleDateFormat(format, Locale.US)
        edtTransactionDate.setText(sdf.format(calendar.time))
    }

    private fun fetchPaymentMethods() {
        lifecycleScope.launch {
            try {
                val response = ApiConfig.getApiService(sessionManager).getStoreData()
                if (response.isSuccessful && response.body() != null) {
                    val storeData = response.body()!!
                    val payments = storeData.payment

                    // Convert Payment objects to PaymentMethod objects
                    paymentMethods = payments.map { payment ->
                        PaymentMethod(
                            id = payment.id,
                            bankNum = payment.bankNum,
                            bankName = payment.bankName,
                            qrisImage = payment.qrisImage,
                            accountName = payment.accountName
                        )
                    }

                    setupPaymentMethodSpinner()
                } else {
                    Toast.makeText(
                        this@BalanceTopUpActivity,
                        "Gagal memuat metode pembayaran",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@BalanceTopUpActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun setupPaymentMethodSpinner() {
        if (paymentMethods.isEmpty()) {
            Toast.makeText(
                this,
                "Tidak ada metode pembayaran tersedia",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // Debug payment methods
        for (payment in paymentMethods) {
            android.util.Log.d("BalanceTopUp", "Payment Option - ID: ${payment.id}, Bank: ${payment.bankName}, Number: ${payment.bankNum}")
        }

        val paymentOptions = paymentMethods.map { "${it.bankName} - ${it.bankNum}" }.toTypedArray()
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, paymentOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinnerPaymentMethod.adapter = adapter
        spinnerPaymentMethod.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedPaymentId = paymentMethods[position].id
                android.util.Log.d("BalanceTopUp", "Selected payment ID: $selectedPaymentId")
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedPaymentId = -1
            }
        }
    }

    private fun submitForm() {
        // Prevent multiple clicks
        if (!btnSend.isEnabled) {
            return
        }

        // Validate inputs
        if (selectedImageUri == null) {
            Toast.makeText(this, "Mohon pilih foto bukti pembayaran", Toast.LENGTH_SHORT).show()
            return
        }

        val nominal = edtNominal.text.toString().trim()
        if (nominal.isEmpty()) {
            Toast.makeText(this, "Mohon isi nominal top up", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            // Validate the amount is a valid number
            val amountValue = nominal.replace("[^0-9]".toRegex(), "").toLong()
            if (amountValue <= 0) {
                Toast.makeText(this, "Nominal harus lebih dari 0", Toast.LENGTH_SHORT).show()
                return
            }
        } catch (e: NumberFormatException) {
            Toast.makeText(this, "Format nominal tidak valid", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedPaymentId == -1) {
            Toast.makeText(this, "Mohon pilih metode pembayaran", Toast.LENGTH_SHORT).show()
            return
        }

        val transactionDate = edtTransactionDate.text.toString().trim()
        if (transactionDate.isEmpty()) {
            Toast.makeText(this, "Mohon pilih tanggal transaksi", Toast.LENGTH_SHORT).show()
            return
        }

        // Show progress indicator
        btnSend.text = "Mengirim..."
        btnSend.isEnabled = false

        // Proceed with the API call
        uploadTopUpData(nominal, selectedPaymentId.toString(), transactionDate)
    }

    private fun uploadTopUpData(amount: String, paymentInfoId: String, transactionDate: String) {
        lifecycleScope.launch {
            try {
                // Log the values being sent
                android.util.Log.d("BalanceTopUp", "Amount: $amount")
                android.util.Log.d("BalanceTopUp", "Payment ID: $paymentInfoId")
                android.util.Log.d("BalanceTopUp", "Transaction Date: $transactionDate")

                // Find the selected payment method to get bank name
                val selectedPayment = paymentMethods.find { it.id.toString() == paymentInfoId }
                if (selectedPayment == null) {
                    Toast.makeText(
                        this@BalanceTopUpActivity,
                        "Metode pembayaran tidak valid",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@launch
                }

                val bankName = selectedPayment.bankName
                val bankNum = selectedPayment.bankNum
                android.util.Log.d("BalanceTopUp", "Bank Name: $bankName")
                android.util.Log.d("BalanceTopUp", "Bank Number: $bankNum")

                // Get the actual file from URI
                val file = uriToFile(selectedImageUri!!)
                android.util.Log.d("BalanceTopUp", "File size: ${file.length()} bytes")
                android.util.Log.d("BalanceTopUp", "File name: ${file.name}")

                // Create multipart file with specific JPEG content type
                val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val imagePart = MultipartBody.Part.createFormData("topupimg", file.name, requestFile)

                // Create other request bodies - ensure proper formatting
                // Make sure amount has no commas, spaces or currency symbols
                val cleanedAmount = amount.replace("[^0-9]".toRegex(), "")
                val amountBody = cleanedAmount.toRequestBody("text/plain".toMediaTypeOrNull())
                val paymentInfoIdBody = paymentInfoId.toRequestBody("text/plain".toMediaTypeOrNull())
                val transactionDateBody = transactionDate.toRequestBody("text/plain".toMediaTypeOrNull())
                val bankNameBody = bankName.toRequestBody("text/plain".toMediaTypeOrNull())
                val bankNumBody = bankNum.toRequestBody("text/plain".toMediaTypeOrNull())

                // Make the API call
                val response = ApiConfig.getApiService(sessionManager).addBalanceTopUp(
                    imagePart,
                    amountBody,
                    paymentInfoIdBody,
                    transactionDateBody,
                    bankNameBody,
                    bankNumBody
                )

                if (response.isSuccessful) {
                    // Log the complete response
                    val responseBody = response.body()
                    android.util.Log.d("BalanceTopUp", "Success response: ${responseBody?.message}")

                    // Show the actual message from backend
                    val successMessage = "Top Up Berhasil"
                    Toast.makeText(
                        this@BalanceTopUpActivity,
                        successMessage,
                        Toast.LENGTH_LONG
                    ).show()

                    // Show a dialog with the success message
                    runOnUiThread {
                        androidx.appcompat.app.AlertDialog.Builder(this@BalanceTopUpActivity)
                            .setTitle("Berhasil")
                            .setMessage(successMessage)
                            .setPositiveButton("OK") { dialog, _ ->
                                dialog.dismiss()
                                // Set result OK so parent activity can refresh data
                                setResult(RESULT_OK)
                                finish()
                            }
                            .show()
                    }
                } else {
                    // Get more detailed error information
                    val errorBody = response.errorBody()?.string()
                    android.util.Log.e("BalanceTopUp", "Error body: $errorBody")
                    android.util.Log.e("BalanceTopUp", "Error code: ${response.code()}")

                    // Try to parse the error body to extract the message
                    var errorMessage = "Gagal mengirim permintaan: ${response.message() ?: "Error ${response.code()}"}"
                    try {
                        val jsonObject = org.json.JSONObject(errorBody ?: "{}")
                        if (jsonObject.has("message")) {
                            errorMessage = jsonObject.getString("message")
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("BalanceTopUp", "Error parsing error body", e)
                    }

                    Toast.makeText(
                        this@BalanceTopUpActivity,
                        errorMessage,
                        Toast.LENGTH_LONG
                    ).show()

                    // Show a dialog with the error message
                    runOnUiThread {
                        androidx.appcompat.app.AlertDialog.Builder(this@BalanceTopUpActivity)
                            .setTitle("Error Response")
                            .setMessage(errorMessage)
                            .setPositiveButton("OK") { dialog, _ ->
                                dialog.dismiss()
                                setResult(RESULT_CANCELED)
                            }
                            .show()
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("BalanceTopUp", "Exception: ${e.message}", e)
                Toast.makeText(
                    this@BalanceTopUpActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                setResult(RESULT_CANCELED)
            } finally {
                // Reset button state
                btnSend.text = "Kirim"
                btnSend.isEnabled = true
            }
        }
    }

    private fun uriToFile(uri: Uri): File {
        val inputStream = contentResolver.openInputStream(uri)
        val tempFile = File.createTempFile("upload", ".jpg", cacheDir)
        tempFile.deleteOnExit()

        inputStream?.use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        // Validate file isn't empty
        if (tempFile.length() == 0L) {
            throw IllegalStateException("File is empty")
        }

        return tempFile
    }
}