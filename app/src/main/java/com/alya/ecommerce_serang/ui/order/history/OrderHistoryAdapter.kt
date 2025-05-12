package com.alya.ecommerce_serang.ui.order.history

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alya.ecommerce_serang.R
import com.alya.ecommerce_serang.data.api.dto.OrdersItem
import com.alya.ecommerce_serang.ui.order.detail.PaymentActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class OrderHistoryAdapter(
    private val onOrderClickListener: (OrdersItem) -> Unit,
    private val viewModel: HistoryViewModel // Add this parameter
) : RecyclerView.Adapter<OrderHistoryAdapter.OrderViewHolder>() {

    private val orders = mutableListOf<OrdersItem>()

    private var fragmentStatus: String = "all"

    fun setFragmentStatus(status: String) {
        fragmentStatus = status
    }

    fun submitList(newOrders: List<OrdersItem>) {
        orders.clear()
        orders.addAll(newOrders)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_order_history, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(orders[position])
    }

    override fun getItemCount(): Int = orders.size

    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvStoreName: TextView = itemView.findViewById(R.id.tvStoreName)
        private val rvOrderItems: RecyclerView = itemView.findViewById(R.id.rvOrderItems)
        private val tvShowMore: TextView = itemView.findViewById(R.id.tvShowMore)
        private val tvTotalAmount: TextView = itemView.findViewById(R.id.tvTotalAmount)
        private val tvItemCountLabel: TextView = itemView.findViewById(R.id.tv_count_total_item)
//        private val tvDeadlineDate: TextView = itemView.findViewById(R.id.tvDeadlineDate)

        fun bind(order: OrdersItem) {
            // Get store name from the first order item
            val storeName = if (order.orderItems.isNotEmpty()) order.orderItems[0].storeName else ""
            tvStoreName.text = storeName

            // Set total amount
            tvTotalAmount.text = order.totalAmount

            // Set item count
            val itemCount = order.orderItems.size
            tvItemCountLabel.text = itemView.context.getString(R.string.item_count_prod, itemCount)

            // Set deadline date, adjust to each status
//            tvDeadlineDate.text = formatDate(order.updatedAt)

            // Set up the order items RecyclerView
            val productAdapter = OrderProductAdapter()
            rvOrderItems.apply {
                layoutManager = LinearLayoutManager(itemView.context)
                adapter = productAdapter
            }

            // Display only the first product and show "View more" for the rest
            if (order.orderItems.isNotEmpty()) {
                productAdapter.submitList(order.orderItems.take(1))

                // Show or hide the "View more" text based on number of items
                if (order.orderItems.size > 1) {
                    val itemString = order.orderItems.size - 1
                    tvShowMore.visibility = View.VISIBLE
                    tvShowMore.text = itemView.context.getString(R.string.show_more_product, itemString)
                } else {
                    tvShowMore.visibility = View.GONE
                }
            } else {
                tvShowMore.visibility = View.GONE
            }

            // Set click listener for the entire order item
            itemView.setOnClickListener {
                onOrderClickListener(order)
            }

            //adjust each fragment
            adjustButtonsAndText(fragmentStatus, order)

        }

        private fun adjustButtonsAndText(status: String, order: OrdersItem) {
            Log.d("OrderHistoryAdapter", "Adjusting buttons for status: $status")
            // Mendapatkan referensi ke tombol-tombol
            val btnLeft = itemView.findViewById<MaterialButton>(R.id.btn_left)
            val btnRight = itemView.findViewById<MaterialButton>(R.id.btn_right)
            val statusOrder = itemView.findViewById<TextView>(R.id.tvOrderStatus)
            val deadlineLabel = itemView.findViewById<TextView>(R.id.tvDeadlineLabel)
            val deadlineDate = itemView.findViewById<TextView>(R.id.tvDeadlineDate)

            // Reset visibility
            btnLeft.visibility = View.GONE
            btnRight.visibility = View.GONE
            statusOrder.visibility = View.GONE
            deadlineLabel.visibility = View.GONE

            when (status) {
                "pending" -> {
                    statusOrder.apply {
                        visibility = View.VISIBLE
                        text = itemView.context.getString(R.string.pending_orders)
                    }
                    deadlineLabel.apply {
                        visibility = View.VISIBLE
                        text = itemView.context.getString(R.string.dl_pending)
                    }
                    btnLeft.apply {
                        visibility = View.VISIBLE
                        text = itemView.context.getString(R.string.canceled_order_btn)
                        setOnClickListener {
                            showCancelOrderDialog(order.orderId.toString())
                        }
                    }
                    deadlineDate.apply {
                        visibility = View.VISIBLE
                        text = formatDate(order.createdAt)
                    }
                }
                "unpaid" -> {
                    statusOrder.apply {
                        visibility = View.VISIBLE
                        text = itemView.context.getString(R.string.unpaid_orders)
                    }
                    deadlineLabel.apply {
                        visibility = View.VISIBLE
                        text = itemView.context.getString(R.string.dl_unpaid)
                    }
                    btnLeft.apply {
                        visibility = View.VISIBLE
                        text = itemView.context.getString(R.string.canceled_order_btn)
                        setOnClickListener {
                            showCancelOrderDialog(order.orderId.toString())
                        }
                    }

                    btnRight.apply {
                        visibility = View.VISIBLE
                        text = itemView.context.getString(R.string.sent_evidence)
                        setOnClickListener {
                            val intent = Intent(itemView.context, PaymentActivity::class.java)
                            // Menambahkan data yang diperlukan
                            intent.putExtra("ORDER_ID", order.orderId)
                            intent.putExtra("ORDER_PAYMENT_ID", order.paymentInfoId)

                            // Memulai aktivitas
                            itemView.context.startActivity(intent)
                        }
                    }
                    deadlineDate.apply {
                        visibility = View.VISIBLE
                        text = formatDatePay(order.updatedAt)
                    }
                }
                "processed" -> {
                    // Untuk status processed, tampilkan "Hubungi Penjual"
                    statusOrder.apply {
                        visibility = View.VISIBLE
                        text = itemView.context.getString(R.string.processed_orders)
                    }
                    deadlineLabel.apply {
                        visibility = View.VISIBLE
                        text = itemView.context.getString(R.string.dl_processed)
                    }
                    btnLeft.apply {
                        visibility = View.VISIBLE
                        text = itemView.context.getString(R.string.canceled_order_btn)
                        setOnClickListener {
                            showCancelOrderDialog(order.orderId.toString())
                        }
                    }
                }
                "shipped" -> {
                    // Untuk status shipped, tampilkan "Lacak Pengiriman" dan "Terima Barang"
                    statusOrder.apply {
                        visibility = View.VISIBLE
                        text = itemView.context.getString(R.string.shipped_orders)
                    }
                    deadlineLabel.apply {
                        visibility = View.VISIBLE
                        text = itemView.context.getString(R.string.dl_shipped)
                    }
                    btnLeft.apply {
                        visibility = View.VISIBLE
                        text = itemView.context.getString(R.string.claim_complaint)
                        setOnClickListener {
                            showCancelOrderDialog(order.orderId.toString())
                            // Handle click event
                        }
                    }
                    btnRight.apply {
                        visibility = View.VISIBLE
                        text = itemView.context.getString(R.string.claim_order)
                        setOnClickListener {
                            // Handle click event
                            viewModel.confirmOrderCompleted(order.orderId, "completed")

                        }
                    }
                    deadlineDate.apply {
                        visibility = View.VISIBLE
                        text = formatShipmentDate(order.updatedAt, order.etd.toInt())
                    }
                }
                "delivered" -> {
                    // Untuk status delivered, tampilkan "Beri Ulasan"
                    btnRight.apply {
                        visibility = View.VISIBLE
                        text = itemView.context.getString(R.string.add_review)
                        setOnClickListener {
                            // Handle click event
                        }
                    }
                }
                "completed" -> {
                    statusOrder.apply {
                        visibility = View.VISIBLE
                        text = itemView.context.getString(R.string.shipped_orders)
                    }
                    deadlineLabel.apply {
                        visibility = View.VISIBLE
                        text = itemView.context.getString(R.string.dl_shipped)
                    }
                    btnRight.apply {
                        visibility = View.VISIBLE
                        text = itemView.context.getString(R.string.add_review)
                        setOnClickListener {
                            // Handle click event
                        }
                    }
                }
                "canceled" -> {
                    statusOrder.apply {
                        visibility = View.VISIBLE
                        text = itemView.context.getString(R.string.canceled_orders)
                    }
                }
            }
        }

        private fun formatDate(dateString: String): String {
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                inputFormat.timeZone = TimeZone.getTimeZone("UTC")

                val outputFormat = SimpleDateFormat("HH:mm dd MMMM yyyy", Locale("id", "ID"))

                val date = inputFormat.parse(dateString)

                date?.let {
                    val calendar = Calendar.getInstance()
                    calendar.time = it
                    calendar.set(Calendar.HOUR_OF_DAY, 23)
                    calendar.set(Calendar.MINUTE, 59)

                    outputFormat.format(calendar.time)
                } ?: dateString
            } catch (e: Exception) {
                Log.e("DateFormatting", "Error formatting date: ${e.message}")
                dateString
            }
        }

        private fun formatDatePay(dateString: String): String {
            return try {
                // Parse the ISO 8601 date
                val isoDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                isoDateFormat.timeZone = TimeZone.getTimeZone("UTC")

                val createdDate = isoDateFormat.parse(dateString)

                // Add 24 hours to get due date
                val calendar = Calendar.getInstance()
                calendar.time = createdDate
                calendar.add(Calendar.HOUR, 24)
                val dueDate = calendar.time

                // Format due date for display
                val dueDateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                dueDateFormat.format(calendar.time)

            } catch (e: Exception) {
                Log.e("DateFormatting", "Error formatting date: ${e.message}")
                dateString
            }
        }

        private fun formatShipmentDate(dateString: String, estimate: Int): String {
            return try {
                // Parse the input date
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                inputFormat.timeZone = TimeZone.getTimeZone("UTC")

                // Output format
                val outputFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))

                // Parse the input date
                val date = inputFormat.parse(dateString)

                date?.let {
                    val calendar = Calendar.getInstance()
                    calendar.time = it

                    // Add estimated days
                    calendar.add(Calendar.DAY_OF_MONTH, estimate)
                    outputFormat.format(calendar.time)
                } ?: dateString
            } catch (e: Exception) {
                Log.e("ShipmentDateFormatting", "Error formatting shipment date: ${e.message}")
                dateString
            }
        }

        private fun showCancelOrderDialog(orderId: String) {
            val context = itemView.context
            val dialog = Dialog(context)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.dialog_cancel_order)
            dialog.setCancelable(true)

            // Set the dialog width to match parent
            val window = dialog.window
            window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

            // Get references to the views in the dialog
            val spinnerCancelReason = dialog.findViewById<AutoCompleteTextView>(R.id.spinnerCancelReason)
            val tilCancelReason = dialog.findViewById<TextInputLayout>(R.id.tilCancelReason)
            val btnCancelDialog = dialog.findViewById<MaterialButton>(R.id.btnCancelDialog)
            val btnConfirmCancel = dialog.findViewById<MaterialButton>(R.id.btnConfirmCancel)
            val ivComplaintImage = dialog.findViewById<ImageView>(R.id.ivComplaintImage)
            val tvSelectImage = dialog.findViewById<TextView>(R.id.tvSelectImage)

            // Set up the reasons dropdown
            val reasons = context.resources.getStringArray(R.array.cancellation_reasons)
            val adapter = ArrayAdapter(context, android.R.layout.simple_dropdown_item_1line, reasons)
            spinnerCancelReason.setAdapter(adapter)

            // For storing the selected image URI
            var selectedImageUri: Uri? = null

            // Set click listener for image selection
            ivComplaintImage.setOnClickListener {
                // Create an intent to open the image picker
                val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                (context as? Activity)?.startActivityForResult(galleryIntent, REQUEST_IMAGE_PICK)

                // Set up result handler in the activity
                val activity = context as? Activity
                activity?.let {
                    // Remove any existing callbacks to avoid memory leaks
                    if (imagePickCallback != null) {
                        imagePickCallback = null
                    }

                    // Create a new callback for this specific dialog
                    imagePickCallback = { uri ->
                        selectedImageUri = uri

                        // Load and display the selected image
                        ivComplaintImage.setImageURI(uri)
                        tvSelectImage.visibility = View.GONE
                    }
                }
            }

            // Set click listeners for buttons
            btnCancelDialog.setOnClickListener {
                dialog.dismiss()
            }

            btnConfirmCancel.setOnClickListener {
                val reason = spinnerCancelReason.text.toString().trim()

                if (reason.isEmpty()) {
                    tilCancelReason.error = context.getString(R.string.please_select_cancellation_reason)
                    return@setOnClickListener
                }

                // Clear error if any
                tilCancelReason.error = null

                // Convert selected image to file if available
                val imageFile = selectedImageUri?.let { uri ->
                    try {
                        // Get the file path from URI
                        val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
                        val cursor = context.contentResolver.query(uri, filePathColumn, null, null, null)
                        cursor?.use {
                            if (it.moveToFirst()) {
                                val columnIndex = it.getColumnIndex(filePathColumn[0])
                                val filePath = it.getString(columnIndex)
                                return@let File(filePath)
                            }
                        }
                        null
                    } catch (e: Exception) {
                        Log.e("OrderHistoryAdapter", "Error getting file from URI: ${e.message}")
                        null
                    }
                }

                // Show loading indicator
                val loadingView = View(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    setBackgroundColor(Color.parseColor("#80000000"))

                    val progressBar = ProgressBar(context).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                    }

//                    addView(progressBar)
//                    (progressBar.layoutParams as? ViewGroup.MarginLayoutParams)?.apply {
//                        gravity = Gravity.CENTER
//                    }
                }

                dialog.addContentView(loadingView, loadingView.layoutParams)

                // Call the ViewModel to cancel the order with image
                viewModel.cancelOrderWithImage(orderId, reason, imageFile)

                // Observe for success/failure
                viewModel.isSuccess.observe(itemView.findViewTreeLifecycleOwner()!!) { isSuccess ->
                    // Remove loading indicator
                    (loadingView.parent as? ViewGroup)?.removeView(loadingView)

                    if (isSuccess) {
                        Toast.makeText(context, context.getString(R.string.order_canceled_successfully), Toast.LENGTH_SHORT).show()
                        dialog.dismiss()

                        // Find the order in the list and remove it or update its status
                        val position = orders.indexOfFirst { it.orderId.toString() == orderId }
                        if (position != -1) {
                            orders.removeAt(position)
                            notifyItemRemoved(position)
                            notifyItemRangeChanged(position, orders.size)
                        }
                    } else {
                        Toast.makeText(context, viewModel.message.value ?: context.getString(R.string.failed_to_cancel_order), Toast.LENGTH_SHORT).show()
                    }
                }
            }
            dialog.show()
        }
    }

    companion object {
        private const val REQUEST_IMAGE_PICK = 100
        private var imagePickCallback: ((Uri) -> Unit)? = null

        // This method should be called from the activity's onActivityResult
        fun handleImageResult(requestCode: Int, resultCode: Int, data: Intent?) {
            if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK && data != null) {
                val selectedImageUri = data.data
                selectedImageUri?.let { uri ->
                    imagePickCallback?.invoke(uri)
                }
            }
        }
    }
}