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
import java.util.TimeZone

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

            // Show debugging information
            Log.d(TAG, "Selected date: $selectedDate")

            // Display all top-up dates for debugging
            allTopUps.forEach { topUp ->
                Log.d(TAG, "Top-up ID: ${topUp.id}, transaction_date: ${topUp.transactionDate}, created_at: ${topUp.createdAt}")
            }

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
            Log.d(TAG, "Filtering by date: $dateStr")

            // Parse the selected date - set to start of day
            val cal1 = Calendar.getInstance()
            cal1.time = parseSelectedDate(dateStr)
            cal1.set(Calendar.HOUR_OF_DAY, 0)
            cal1.set(Calendar.MINUTE, 0)
            cal1.set(Calendar.SECOND, 0)
            cal1.set(Calendar.MILLISECOND, 0)

            // Extract the date components we care about (year, month, day)
            val selectedYear = cal1.get(Calendar.YEAR)
            val selectedMonth = cal1.get(Calendar.MONTH)
            val selectedDay = cal1.get(Calendar.DAY_OF_MONTH)

            Log.d(TAG, "Selected date components: Year=$selectedYear, Month=$selectedMonth, Day=$selectedDay")

            // Format for comparing with API dates
            val filtered = allTopUps.filter { topUp ->
                try {
                    // Debug logging
                    Log.d(TAG, "Examining top-up: ID=${topUp.id}")
                    Log.d(TAG, "   - created_at=${topUp.createdAt}")
                    Log.d(TAG, "   - transaction_date=${topUp.transactionDate}")

                    // Try both dates for more flexibility
                    val cal2 = Calendar.getInstance()
                    var matched = false

                    // Try transaction_date first
                    if (topUp.transactionDate.isNotEmpty()) {
                        val transactionDate = parseApiDate(topUp.transactionDate)
                        if (transactionDate != null) {
                            cal2.time = transactionDate
                            val transYear = cal2.get(Calendar.YEAR)
                            val transMonth = cal2.get(Calendar.MONTH)
                            val transDay = cal2.get(Calendar.DAY_OF_MONTH)

                            Log.d(TAG, "   - Transaction date components: Year=$transYear, Month=$transMonth, Day=$transDay")

                            if (transYear == selectedYear &&
                                transMonth == selectedMonth &&
                                transDay == selectedDay) {
                                Log.d(TAG, "   - MATCH on transaction_date")
                                matched = true
                            }
                        }
                    }

                    // If no match yet, try created_at
                    if (!matched && topUp.createdAt.isNotEmpty()) {
                        val createdAtDate = parseApiDate(topUp.createdAt)
                        if (createdAtDate != null) {
                            cal2.time = createdAtDate
                            val createdYear = cal2.get(Calendar.YEAR)
                            val createdMonth = cal2.get(Calendar.MONTH)
                            val createdDay = cal2.get(Calendar.DAY_OF_MONTH)

                            Log.d(TAG, "   - Created date components: Year=$createdYear, Month=$createdMonth, Day=$createdDay")

                            if (createdYear == selectedYear &&
                                createdMonth == selectedMonth &&
                                createdDay == selectedDay) {
                                Log.d(TAG, "   - MATCH on created_at")
                                matched = true
                            }
                        }
                    }

                    // Final result
                    Log.d(TAG, "   - Match result: $matched")
                    matched
                } catch (e: Exception) {
                    Log.e(TAG, "Date parsing error for top-up ${topUp.id}: ${e.message}", e)
                    false
                }
            }

            Log.d(TAG, "Found ${filtered.size} matching records out of ${allTopUps.size}")
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

    private fun parseSelectedDate(dateStr: String): Date {
        // Parse the user-selected date
        try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            return dateFormat.parse(dateStr) ?: Date()
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing selected date: $dateStr", e)
            return Date()
        }
    }

    /**
     * Parse ISO 8601 date format from API (handles multiple formats)
     */
    private fun parseApiDate(dateStr: String): Date? {
        if (dateStr.isEmpty()) return null

        // List of possible date formats to try
        val formats = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",  // Standard ISO with milliseconds
            "yyyy-MM-dd'T'HH:mm:ss'Z'",      // ISO without milliseconds
            "yyyy-MM-dd'T'HH:mm:ss.SSSZ",    // ISO with timezone offset
            "yyyy-MM-dd'T'HH:mm:ssZ",        // ISO with timezone offset, no milliseconds
            "yyyy-MM-dd",                    // Just the date part
            "dd-MM-yyyy"                     // Alternative date format
        )

        for (format in formats) {
            try {
                val sdf = SimpleDateFormat(format, Locale.US)
                sdf.timeZone = TimeZone.getTimeZone("UTC") // Assuming API dates are in UTC
                return sdf.parse(dateStr)
            } catch (e: Exception) {
                // Try next format
                continue
            }
        }

        // If all formats fail, just try to extract the date part and parse it
        try {
            val datePart = dateStr.split("T").firstOrNull() ?: return null
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            return simpleDateFormat.parse(datePart)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse date: $dateStr", e)
            return null
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