package com.example.grainclassifier

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.grainclassifier.databinding.ActivityResultBinding

class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setup ViewBinding
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Extract Intent Parameters (Providing requested fallbacks: Rice & 96% accuracy)
        val imageUriString = intent.getStringExtra("EXTRA_IMAGE_URI")
        val grainName = intent.getStringExtra("EXTRA_GRAIN_NAME") ?: "Rice"
        val confidenceScore = intent.getIntExtra("EXTRA_CONFIDENCE", 96)

        // Bind image resource
        if (imageUriString != null) {
            try {
                val imageUri = Uri.parse(imageUriString)
                binding.ivResultImage.setImageURI(imageUri)
            } catch (e: Exception) {
                e.printStackTrace()
                binding.ivResultImage.setImageResource(R.drawable.ic_grain_logo)
            }
        } else {
            // Fallback placeholder image
            binding.ivResultImage.setImageResource(R.drawable.ic_grain_logo)
        }

        // Bind text data
        binding.tvGrainName.text = grainName
        binding.tvConfidenceValue.text = "$confidenceScore% Accuracy"

        // Dynamic botanical tag matching fallback logic
        when {
            grainName.contains("Rice", ignoreCase = true) -> {
                binding.tvParamBotanical.text = "Oryza sativa"
                binding.badgeConfidence.setCardBackgroundColor(getColor(R.color.rice_bg))
                binding.tvConfidenceValue.setTextColor(getColor(R.color.rice_tag))
            }
            grainName.contains("Wheat", ignoreCase = true) -> {
                binding.tvParamBotanical.text = "Triticum"
                binding.badgeConfidence.setCardBackgroundColor(getColor(R.color.wheat_bg))
                binding.tvConfidenceValue.setTextColor(getColor(R.color.wheat_tag))
            }
            grainName.contains("Maize", ignoreCase = true) -> {
                binding.tvParamBotanical.text = "Zea mays"
                binding.badgeConfidence.setCardBackgroundColor(getColor(R.color.maize_bg))
                binding.tvConfidenceValue.setTextColor(getColor(R.color.maize_tag))
            }
            grainName.contains("Bajra", ignoreCase = true) -> {
                binding.tvParamBotanical.text = "Pennisetum glaucum"
                binding.badgeConfidence.setCardBackgroundColor(getColor(R.color.bajra_bg))
                binding.tvConfidenceValue.setTextColor(getColor(R.color.bajra_tag))
            }
            grainName.contains("Ragi", ignoreCase = true) -> {
                binding.tvParamBotanical.text = "Eleusine coracana"
                binding.badgeConfidence.setCardBackgroundColor(getColor(R.color.ragi_bg))
                binding.tvConfidenceValue.setTextColor(getColor(R.color.ragi_tag))
            }
            else -> {
                binding.tvParamBotanical.text = "Gramineae family"
            }
        }

        setupClickListeners()
    }

    private fun setupClickListeners() {
        // Reset the Activity stack and navigate fresh to MainActivity
        binding.btnAnalyzeAnother.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    override fun onBackPressed() {
        // Safe navigation reset back to MainActivity to prevent visual loops
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
        finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}
