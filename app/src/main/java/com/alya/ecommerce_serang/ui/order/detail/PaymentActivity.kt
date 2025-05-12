package com.alya.ecommerce_serang.ui.order.detail

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.alya.ecommerce_serang.data.api.retrofit.ApiConfig
import com.alya.ecommerce_serang.data.repository.OrderRepository
import com.alya.ecommerce_serang.databinding.ActivityPaymentBinding
import com.alya.ecommerce_serang.utils.BaseViewModelFactory
import com.alya.ecommerce_serang.utils.SessionManager
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class PaymentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPaymentBinding
    private lateinit var sessionManager: SessionManager

    companion object {
        private const val TAG = "PaymentActivity"
    }

    private val viewModel: PaymentViewModel by viewModels {
        BaseViewModelFactory {
            val apiService = ApiConfig.getApiService(sessionManager)
            val orderRepository = OrderRepository(apiService)
            PaymentViewModel(orderRepository)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        // Mengambil data dari intent
        val orderId = intent.getIntExtra("ORDER_ID", 0)
        val paymentInfoId = intent.getIntExtra("ORDER_PAYMENT_ID", 0)

        if (orderId == 0) {
            Toast.makeText(this, "ID pesanan tidak valid", Toast.LENGTH_SHORT).show()
            finish()
        }

        // Setup toolbar
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        // Setup petunjuk transfer
        binding.layoutMBankingInstructions.setOnClickListener {
            // Tampilkan instruksi mBanking
            showInstructions("mBanking")
        }

        binding.layoutATMInstructions.setOnClickListener {
            // Tampilkan instruksi ATM
            showInstructions("ATM")
        }

        // Setup button upload bukti bayar
        binding.btnUploadPaymentProof.setOnClickListener {
//             Intent ke activity upload bukti bayar
            val intent = Intent(this, AddEvidencePaymentActivity::class.java)
            intent.putExtra("ORDER_ID", orderId)
            intent.putExtra("PAYMENT_INFO_ID", paymentInfoId)
            intent.putExtra("TOTAL_AMOUNT", binding.tvTotalAmount.text.toString())
            Log.d(TAG, "Received Order ID: $orderId, Payment Info ID: $paymentInfoId, Total Amount: ${binding.tvTotalAmount.text}")

            startActivity(intent)
        }

        // Setup button negosiasi harga
        binding.btnNegotiatePrice.setOnClickListener {
            // Intent ke activity negosiasi harga
//            val intent = Intent(this, NegotiatePriceActivity::class.java)
//            intent.putExtra("ORDER_ID", orderId)
//            startActivity(intent)
        }

        // Observe data
        observeData()

        // Load data
        Log.d(TAG, "Fetching order details for Order ID: $orderId")
        viewModel.getOrderDetails(orderId)
    }

    private fun observeData() {
        // Observe Order Details
        viewModel.orderDetails.observe(this) { order ->
            Log.d(TAG, "Order details received: $order")

            // Set total amount
            binding.tvTotalAmount.text = order.totalAmount ?: "Rp0"
            Log.d(TAG, "Total Amount: ${order.totalAmount}")


            // Set bank information
            binding.tvBankName.text = order.payInfoName ?: "Bank BCA"
            binding.tvAccountNumber.text = order.payInfoNum ?: "0123456789"
            Log.d(TAG, "Bank Name: ${order.payInfoName}, Account Number: ${order.payInfoNum}")


            // Calculate remaining time and due date
            setupPaymentDueDate(order.updatedAt)
        }

        // Observe loading state
        viewModel.isLoading.observe(this) { isLoading ->
            // Show loading indicator if needed
            // binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Observe error
        viewModel.error.observe(this) { error ->
            if (error.isNotEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupPaymentDueDate(createdAt: String) {
        Log.d(TAG, "Setting up payment due date from updated at: $createdAt")

        try {
            // Parse the ISO 8601 date
            val isoDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            isoDateFormat.timeZone = TimeZone.getTimeZone("UTC")
            val createdDate = isoDateFormat.parse(createdAt) ?: return

            // Add 24 hours to get due date
            val calendar = Calendar.getInstance()
            calendar.time = createdDate
            calendar.add(Calendar.HOUR, 24)
            val dueDate = calendar.time

            // Format due date for display
            val dueDateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            binding.tvDueDate.text = "Jatuh tempo: ${dueDateFormat.format(dueDate)}"
            Log.d(TAG, "Due Date: ${dueDateFormat.format(dueDate)}")

            // Calculate remaining time
            val now = Calendar.getInstance().time
            val diff = dueDate.time - now.time

            if (diff > 0) {
                val hours = diff / (60 * 60 * 1000)
                val minutes = (diff % (60 * 60 * 1000)) / (60 * 1000)
                binding.tvRemainingTime.text = "$hours jam $minutes menit"
                Log.d(TAG, "Remaining Time: $hours hours $minutes minutes")
            } else {
                binding.tvRemainingTime.text = "Waktu habis"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing date", e)
            binding.tvDueDate.text = "Jatuh tempo: -"
            binding.tvRemainingTime.text = "-"
        }
    }

    private fun showInstructions(type: String) {
        // Implementasi tampilkan instruksi
        val instructions = when (type) {
            "mBanking" -> listOf(
                "1. Login ke aplikasi mobile banking",
                "2. Pilih menu Transfer",
                "3. Pilih menu Antar Rekening",
                "4. Masukkan nomor rekening tujuan",
                "5. Masukkan nominal transfer sesuai tagihan",
                "6. Konfirmasi dan selesaikan transfer"
            )
            "ATM" -> listOf(
                "1. Masukkan kartu ATM dan PIN",
                "2. Pilih menu Transfer",
                "3. Pilih menu Antar Rekening",
                "4. Masukkan kode bank dan nomor rekening tujuan",
                "5. Masukkan nominal transfer sesuai tagihan",
                "6. Konfirmasi dan selesaikan transfer"
            )
            else -> emptyList()
        }

        // Tampilkan instruksi dalam dialog
        val dialog = AlertDialog.Builder(this)
            .setTitle("Petunjuk Transfer $type")
            .setItems(instructions.toTypedArray(), null)
            .setPositiveButton("Tutup", null)
            .create()
        dialog.show()
    }
}