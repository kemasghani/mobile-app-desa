package com.alya.ecommerce_serang.ui.profile.mystore.product

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.alya.ecommerce_serang.R
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.alya.ecommerce_serang.data.api.dto.CategoryItem
import com.alya.ecommerce_serang.data.api.dto.Preorder
import com.alya.ecommerce_serang.data.api.dto.Product
import com.alya.ecommerce_serang.data.api.retrofit.ApiConfig
import com.alya.ecommerce_serang.data.repository.ProductRepository
import com.alya.ecommerce_serang.data.repository.Result
import com.alya.ecommerce_serang.databinding.ActivityDetailStoreProductBinding
import com.alya.ecommerce_serang.utils.viewmodel.ProductViewModel
import com.alya.ecommerce_serang.utils.BaseViewModelFactory
import com.alya.ecommerce_serang.utils.SessionManager
import com.bumptech.glide.Glide
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.io.FileOutputStream
import kotlin.getValue
import androidx.core.net.toUri

class DetailStoreProductActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailStoreProductBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var categoryList: List<CategoryItem>
    private var imageUri: Uri? = null
    private var sppirtUri: Uri? = null
    private var halalUri: Uri? = null
    private var productId: Int? = null

    private val viewModel: ProductViewModel by viewModels {
        BaseViewModelFactory {
            sessionManager = SessionManager(this)
            val apiService = ApiConfig.getApiService(sessionManager)
            val productRepository = ProductRepository(apiService)
            ProductViewModel(productRepository)
        }
    }

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            imageUri = result.data?.data
            imageUri?.let {
                binding.ivPreviewFoto.setImageURI(it)
                binding.switcherFotoProduk.showNext()
            }
            validateForm()
        }
    }

    private val sppirtLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null && isValidFile(uri)) {
            sppirtUri = uri
            binding.tvSppirtName.text = getFileName(uri)
            binding.switcherSppirt.showNext()
        }
    }

    private val halalLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null && isValidFile(uri)) {
            halalUri = uri
            binding.tvHalalName.text = getFileName(uri)
            binding.switcherHalal.showNext()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailStoreProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val isEditing = intent.getBooleanExtra("is_editing", false)
        productId = intent.getIntExtra("product_id", -1)

        binding.header.headerTitle.text = if (isEditing) "Ubah Produk" else "Tambah Produk"

//        if (isEditing && productId != null) {
//            viewModel.productDetail.observe(this) { product ->
//                product?.let {
//                    populateForm(it)
//                }
//            }
//            viewModel.loadProductDetail(productId!!)
//        }

        setupCategorySpinner()
        setupImagePickers()

        var conditionList = listOf("Baru", "Pernah Dipakai")
        val adapterCondition = ArrayAdapter(this, android.R.layout.simple_spinner_item, conditionList)
        adapterCondition.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerKondisiProduk.adapter = adapterCondition

        // Setup Pre-Order visibility
        binding.switchIsPreOrder.setOnCheckedChangeListener { _, isChecked ->
            binding.layoutDurasi.visibility = if (isChecked) View.VISIBLE else View.GONE
            validateForm()
        }

        validateForm()

        binding.btnSaveProduct.setOnClickListener {
            if (isEditing) {
                updateProduct(productId)
            } else {
                addProduct()
            }
        }

        binding.header.headerLeftIcon.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupCategorySpinner() {
        viewModel.loadCategories()
        viewModel.categoryList.observe(this) { result ->
            if (result is Result.Success) {
                categoryList = result.data
                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoryList.map { it.name })
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinnerKategoriProduk.adapter = adapter
            }
        }
    }

    private fun setupImagePickers() {
        binding.tvTambahFoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
            imagePickerLauncher.launch(intent)
        }

        binding.layoutUploadFoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
            imagePickerLauncher.launch(intent)
        }

        binding.btnRemoveFoto.setOnClickListener {
            imageUri = null
            binding.switcherFotoProduk.showPrevious()
            validateForm()
        }

        binding.layoutUploadSppirt.setOnClickListener { sppirtLauncher.launch("*/*") }
        binding.btnRemoveSppirt.setOnClickListener {
            sppirtUri = null
            binding.switcherSppirt.showPrevious()
        }

        binding.layoutUploadHalal.setOnClickListener { halalLauncher.launch("*/*") }
        binding.btnRemoveHalal.setOnClickListener {
            halalUri = null
            binding.switcherHalal.showPrevious()
        }
    }

    private fun populateForm(product: Product?) {
        binding.edtNamaProduk.setText(product?.name)
        binding.edtDeskripsiProduk.setText(product?.description)
        binding.edtHargaProduk.setText(product?.price.toString())
        binding.edtStokProduk.setText(product?.stock.toString())
        binding.edtMinOrder.setText(product?.minOrder.toString())
        binding.edtBeratProduk.setText(product?.weight.toString())
        binding.switchIsPreOrder.isChecked = product?.isPreOrder ?: false
        binding.switchIsActive.isChecked = product?.status == "active"
        binding.spinnerKondisiProduk.setSelection(if (product?.condition == "Baru") 0 else 1)

        product?.categoryId?.let {
            binding.spinnerKategoriProduk.setSelection(categoryList.indexOfFirst { it.id == product.categoryId })
        }

        Glide.with(this).load(product?.image).into(binding.ivPreviewFoto)
        binding.switcherFotoProduk.showNext()

        product?.sppirt?.let {
            binding.tvSppirtName.text = getFileName(it.toUri())
            binding.switcherSppirt.showNext()
        }

        product?.halal?.let {
            binding.tvHalalName.text = getFileName(it.toUri())
            binding.switcherHalal.showNext()
        }

        validateForm()
    }

    private fun isValidFile(uri: Uri): Boolean {
        val mimeType = contentResolver.getType(uri) ?: return false
        return listOf("application/pdf", "image/jpeg", "image/png", "image/jpg").contains(mimeType)
    }

    private fun getFileName(uri: Uri): String {
        return uri.lastPathSegment?.split("/")?.last() ?: "unknown_file"
    }

    private fun uriToNamedFile(uri: Uri, context: Context, prefix: String): File {
        val extension = context.contentResolver.getType(uri)?.substringAfter("/") ?: "jpg"
        val filename = "$prefix-${System.currentTimeMillis()}.$extension"
        val file = File(context.cacheDir, filename)

        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(file).use { output -> input.copyTo(output) }
        }

        return file
    }

    fun getMimeType(file: File): String {
        val extension = file.extension
        return when (extension.lowercase()) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "pdf" -> "application/pdf"
            else -> "application/octet-stream"
        }
    }

    fun createPartFromFile(field: String, file: File?): MultipartBody.Part? {
        return file?.let {
            val mimeType = getMimeType(it).toMediaTypeOrNull()
            val requestBody = RequestBody.create(mimeType, it)
            MultipartBody.Part.createFormData(field, it.name, requestBody)
        }
    }

    private fun validateForm() {
        val valid = binding.edtNamaProduk.text.isNotBlank() &&
                binding.edtDeskripsiProduk.text.isNotBlank() &&
                binding.edtHargaProduk.text.isNotBlank() &&
                binding.edtStokProduk.text.isNotBlank() &&
                binding.edtMinOrder.text.isNotBlank() &&
                binding.edtBeratProduk.text.isNotBlank() &&
                (!binding.switchIsPreOrder.isChecked || binding.edtDurasi.text.isNotBlank()) &&
                imageUri != null

        binding.btnSaveProduct.isEnabled = valid
        binding.btnSaveProduct.setTextColor(
            if (valid) ContextCompat.getColor(this, R.color.white) else ContextCompat.getColor(this, R.color.black_300)
        )
        binding.btnSaveProduct.setBackgroundResource(
            if (valid) R.drawable.bg_button_active else R.drawable.bg_button_disabled
        )
    }

    private fun addProduct() {
        val name = binding.edtNamaProduk.text.toString()
        val description = binding.edtDeskripsiProduk.text.toString()
        val price = binding.edtHargaProduk.text.toString().toInt()
        val stock = binding.edtStokProduk.text.toString().toInt()
        val minOrder = binding.edtMinOrder.text.toString().toInt()
        val weight = binding.edtBeratProduk.text.toString().toInt()
        val isPreOrder = binding.switchIsPreOrder.isChecked
        val duration = if (isPreOrder) binding.edtDurasi.text.toString().toInt() else 0
        val status = if (binding.switchIsActive.isChecked) "active" else "inactive"
        val condition = binding.spinnerKondisiProduk.selectedItem.toString()
        val categoryId = categoryList.getOrNull(binding.spinnerKategoriProduk.selectedItemPosition)?.id ?: 0

        val imageFile = imageUri?.let { uriToNamedFile(it, this, "productimg") }
        val sppirtFile = sppirtUri?.let { uriToNamedFile(it, this, "sppirt") }
        val halalFile = halalUri?.let { uriToNamedFile(it, this, "halal") }

        Log.d("File URI", "SPPIRT URI: ${sppirtUri.toString()}")
        Log.d("File URI", "Halal URI: ${halalUri.toString()}")

        val imagePart = imageFile?.let { createPartFromFile("productimg", it) }
        val sppirtPart = sppirtFile?.let { createPartFromFile("sppirt", it) }
        val halalPart = halalFile?.let { createPartFromFile("halal", it) }

        val preorder = Preorder(productId = productId, duration = duration)

        viewModel.addProduct(
            name, description, price, stock, minOrder, weight, isPreOrder, preorder, categoryId, status, condition, imagePart, sppirtPart, halalPart
        )

        viewModel.productCreationResult.observe(this) { result ->
            when (result) {
                is Result.Loading -> binding.btnSaveProduct.isEnabled = false
                is Result.Success -> {
                    val product = result.data.product
                    Toast.makeText(this, "Product Created: ${product?.name}", Toast.LENGTH_SHORT).show()
                    finish()
                }
                is Result.Error -> {
                    Log.e("ProductDetailActivity", "Error: ${result.exception.message}")
                    binding.btnSaveProduct.isEnabled = true
                }
            }
        }
    }

    private fun updateProduct(productId: Int?) {
        val updatedProduct = mapOf(
            "name" to binding.edtNamaProduk.text.toString(),
            "description" to binding.edtDeskripsiProduk.text.toString(),
            "price" to binding.edtHargaProduk.text.toString(),
            "stock" to binding.edtStokProduk.text.toString().toInt(),
            "min_order" to binding.edtMinOrder.text.toString().toInt(),
            "weight" to binding.edtBeratProduk.text.toString().toInt(),
            "is_pre_order" to binding.switchIsPreOrder.isChecked,
            "duration" to binding.edtDurasi.text.toString().toInt(),
            "category_id" to categoryList[binding.spinnerKategoriProduk.selectedItemPosition].id,
            "status" to if (binding.switchIsActive.isChecked) "active" else "inactive",
            "condition" to binding.spinnerKondisiProduk.selectedItem.toString(),
            "productimg" to imageUri?.path,
            "sppirt" to sppirtUri?.path,
            "halal" to halalUri?.path
        )

        viewModel.updateProduct(productId, updatedProduct)
    }
}