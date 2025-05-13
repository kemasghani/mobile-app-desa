package com.alya.ecommerce_serang.ui.profile.mystore.balance

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.alya.ecommerce_serang.BuildConfig
import com.alya.ecommerce_serang.R
import com.alya.ecommerce_serang.data.api.response.store.TopUp
import com.alya.ecommerce_serang.data.api.retrofit.ApiConfig
import com.alya.ecommerce_serang.databinding.ActivityBalanceBinding
import com.alya.ecommerce_serang.utils.SessionManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class BalanceActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBalanceBinding
    private lateinit var topUpAdapter: TopUpAdapter
    private lateinit var sessionManager: SessionManager
    private val calendar = Calendar.getInstance()
    private var selectedDate: String? = null
    private var allTopUps: List<TopUp> = emptyList()

    private val TAG = "BalanceActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityBalanceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize session manager
        sessionManager = SessionManager(this)

        // Setup header
        val headerTitle = binding.header.headerTitle
        headerTitle.text = "Saldo"

        val backButton = binding.header.headerLeftIcon
        backButton.setOnClickListener {
            finish()
        }

        // Setup RecyclerView
        setupRecyclerView()

        // Setup DatePicker
        setupDatePicker()

        // Add clear filter button
        setupClearFilter()

        // Fetch data
        fetchBalance()
        fetchTopUpHistory()

        // Setup listeners
        setupListeners()
    }

    private fun setupRecyclerView() {
        topUpAdapter = TopUpAdapter()
        binding.rvBalanceTransaction.apply {
            layoutManager = LinearLayoutManager(this@BalanceActivity)
            adapter = topUpAdapter
        }
    }

    private fun setupDatePicker() {
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateInView()

            // Store selected date for filtering
            selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(calendar.time)

            // Apply filter
            filterTopUpsByDate(selectedDate)

            // Show clear filter button
            binding.btnClearFilter.visibility = View.VISIBLE
        }

        binding.edtTglTransaksi.setOnClickListener {
            showDatePicker(dateSetListener)
        }

        binding.imgDatePicker.setOnClickListener {
            showDatePicker(dateSetListener)
        }

        binding.iconDatePicker.setOnClickListener {
            showDatePicker(dateSetListener)
        }
    }

    private fun setupClearFilter() {
        binding.btnClearFilter.setOnClickListener {
            // Clear date selection
            binding.edtTglTransaksi.text = null
            selectedDate = null

            // Reset to show all topups
            if (allTopUps.isNotEmpty()) {
                updateTopUpList(allTopUps)
            } else {
                fetchTopUpHistory()
            }

            // Hide clear button
            binding.btnClearFilter.visibility = View.GONE
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
        val format = "dd MMMM yyyy"
        val sdf = SimpleDateFormat(format, Locale("id"))
        binding.edtTglTransaksi.setText(sdf.format(calendar.time))
    }

    private fun setupListeners() {
        binding.btnTopUp.setOnClickListener {
            val intent = Intent(this, BalanceTopUpActivity::class.java)
            startActivityForResult(intent, TOP_UP_REQUEST_CODE)
        }
    }

    private fun fetchBalance() {
        showLoading(true)
        lifecycleScope.launch {
            try {
                val response = ApiConfig.getApiService(sessionManager).getMyStoreData()
                if (response.isSuccessful && response.body() != null) {
                    val storeData = response.body()!!
                    val balance = storeData.store.balance

                    // Format the balance
                    try {
                        val balanceValue = balance.toDouble()
                        binding.tvBalance.text = String.format("Rp%,.0f", balanceValue)
                    } catch (e: Exception) {
                        binding.tvBalance.text = "Rp$balance"
                    }
                } else {
                    Toast.makeText(
                        this@BalanceActivity,
                        "Gagal memuat data saldo: ${response.message()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching balance", e)
                Toast.makeText(
                    this@BalanceActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun fetchTopUpHistory() {
        showLoading(true)
        lifecycleScope.launch {
            try {
                val response = ApiConfig.getApiService(sessionManager).getTopUpHistory()

                if (response.isSuccessful && response.body() != null) {
                    val topUpData = response.body()!!
                    allTopUps = topUpData.topup

                    // Apply date filter if selected
                    if (selectedDate != null) {
                        filterTopUpsByDate(selectedDate)
                    } else {
                        updateTopUpList(allTopUps)
                    }
                } else {
                    Toast.makeText(
                        this@BalanceActivity,
                        "Gagal memuat riwayat isi ulang: ${response.message()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching top-up history", e)
                Toast.makeText(
                    this@BalanceActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun filterTopUpsByDate(dateStr: String?) {
        if (dateStr == null || allTopUps.isEmpty()) {
            return
        }

        try {
            // Parse the selected date
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val selectedDate = dateFormat.parse(dateStr) ?: Date()

            // Format for comparing with transaction_date
            val filtered = allTopUps.filter { topUp ->
                try {
                    // Parse the transaction date (can be either created_at or transaction_date)
                    val datePart = if (topUp.transactionDate.isNotEmpty()) {
                        // Remove time part if present
                        topUp.transactionDate.split("T").firstOrNull() ?: return@filter false
                    } else {
                        // Remove time part if present
                        topUp.createdAt.split("T").firstOrNull() ?: return@filter false
                    }

                    val transactionDate = dateFormat.parse(datePart) ?: Date()

                    // Compare year, month and day only
                    val cal1 = Calendar.getInstance()
                    val cal2 = Calendar.getInstance()
                    cal1.time = selectedDate
                    cal2.time = transactionDate

                    cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                    cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                    cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH)
                } catch (e: Exception) {
                    Log.e(TAG, "Date parsing error: ${e.message}")
                    false
                }
            }

            updateTopUpList(filtered)
        } catch (e: Exception) {
            Log.e(TAG, "Error filtering by date", e)
            Toast.makeText(
                this@BalanceActivity,
                "Error filtering data: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun updateTopUpList(topUps: List<TopUp>) {
        if (topUps.isEmpty()) {
            binding.rvBalanceTransaction.visibility = View.GONE
            binding.tvEmptyState.visibility = View.VISIBLE
        } else {
            binding.rvBalanceTransaction.visibility = View.VISIBLE
            binding.tvEmptyState.visibility = View.GONE
            topUpAdapter.submitList(topUps)
        }
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.GONE
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == TOP_UP_REQUEST_CODE && resultCode == RESULT_OK) {
            // Refresh balance and top-up history after successful top-up
            fetchBalance()
            fetchTopUpHistory()
            Toast.makeText(this, "Top up berhasil", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val TOP_UP_REQUEST_CODE = 101
    }
}