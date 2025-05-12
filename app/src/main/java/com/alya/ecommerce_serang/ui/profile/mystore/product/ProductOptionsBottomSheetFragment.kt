package com.alya.ecommerce_serang.ui.profile.mystore.product

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.alya.ecommerce_serang.data.api.dto.ProductsItem
import com.alya.ecommerce_serang.databinding.FragmentProductOptionsBottomSheetBinding
import com.alya.ecommerce_serang.utils.viewmodel.ProductViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ProductOptionsBottomSheetFragment(private val product: ProductsItem) : BottomSheetDialogFragment() {

    private var _binding: FragmentProductOptionsBottomSheetBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProductOptionsBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnEditProduct.setOnClickListener {
            val intent = Intent(requireContext(), DetailStoreProductActivity::class.java)
            intent.putExtra("product_id", product.id)
            intent.putExtra("is_editing", true)
            startActivity(intent)
            dismiss()
        }

        binding.btnDeleteProduct.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Hapus Produk?")
                .setMessage("Produk yang dihapus tidak dapat dikembalikan.")
                .setPositiveButton("Ya, Hapus") { _, _ ->
                    val viewModel = ViewModelProvider(this).get(ProductViewModel::class.java)
                    viewModel.deleteProduct(product.id)
                    Toast.makeText(context, "Produk berhasil dihapus", Toast.LENGTH_SHORT).show()
                    dismiss()
                }
                .setNegativeButton("Batalkan", null)
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}