package com.example.grainclassifier

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.grainclassifier.databinding.ActivitySupportedGrainsBinding

class SupportedGrainsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySupportedGrainsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivitySupportedGrainsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup Toolbar back button navigation
        binding.toolbar.setNavigationOnClickListener {
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}
