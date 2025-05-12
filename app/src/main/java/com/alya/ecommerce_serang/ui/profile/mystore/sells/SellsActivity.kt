package com.alya.ecommerce_serang.ui.profile.mystore.sells

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.alya.ecommerce_serang.R
import com.alya.ecommerce_serang.ui.profile.mystore.sells.all_sells.AllSellsFragment

class SellsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sells)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.sells_fragment_container, AllSellsFragment())
                .commit()
        }
    }
}