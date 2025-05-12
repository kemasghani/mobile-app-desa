package com.alya.ecommerce_serang.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.alya.ecommerce_serang.R
import com.alya.ecommerce_serang.data.api.dto.RegisterRequest
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class OtpBottomSheetDialog(
    private val userData: RegisterRequest, // Store user data
    private val onRegister: (RegisterRequest) -> Unit)
    : BottomSheetDialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_otp, container, false)

        val etOtp = view.findViewById<EditText>(R.id.etOtp)
        val btnSubmit = view.findViewById<Button>(R.id.btnSubmit)

        btnSubmit.setOnClickListener {
            val otp = etOtp.text.toString()
            if (otp.isNotEmpty()) {
                val updatedUserData = userData.copy(otp = otp) // Add OTP to userData
                onRegister(updatedUserData) // Send full data to ViewModel
                dismiss() // Close dialog
            } else {
                Toast.makeText(requireContext(), "Please enter OTP", Toast.LENGTH_SHORT).show()
            }
        }
        return view
    }

//    override fun getTheme(): Int {
//        return R.style.BottomSheetDialogTheme // Optional: Customize style
//    }
}