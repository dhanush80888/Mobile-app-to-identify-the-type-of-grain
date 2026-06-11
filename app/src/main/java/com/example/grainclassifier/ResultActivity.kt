package com.example.grainclassifier

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.grainclassifier.data.db.AppDatabase
import com.example.grainclassifier.data.entity.ClassificationHistory
import com.example.grainclassifier.data.repository.HistoryRepository
import com.example.grainclassifier.databinding.ActivityResultBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
        if (grainName.contains("Unknown", ignoreCase = true)) {
            binding.badgeConfidence.visibility = android.view.View.GONE
        } else {
            binding.badgeConfidence.visibility = android.view.View.VISIBLE
            binding.tvConfidenceValue.text = "$confidenceScore% Accuracy"
        }

        // Dynamic botanical tag matching fallback logic
        when {
            grainName.contains("Rice", ignoreCase = true) -> {
                binding.tvParamBotanical.text = "Oryza sativa"
                binding.badgeConfidence.setCardBackgroundColor(getColor(R.color.rice_bg))
                binding.tvConfidenceValue.setTextColor(getColor(R.color.rice_tag))
                binding.tvParamStatus.text = "Safe (Optimal Moisture)"
            }
            grainName.contains("Wheat", ignoreCase = true) -> {
                binding.tvParamBotanical.text = "Triticum"
                binding.badgeConfidence.setCardBackgroundColor(getColor(R.color.wheat_bg))
                binding.tvConfidenceValue.setTextColor(getColor(R.color.wheat_tag))
                binding.tvParamStatus.text = "Safe (Optimal Moisture)"
            }
            grainName.contains("Maize", ignoreCase = true) -> {
                binding.tvParamBotanical.text = "Zea mays"
                binding.badgeConfidence.setCardBackgroundColor(getColor(R.color.maize_bg))
                binding.tvConfidenceValue.setTextColor(getColor(R.color.maize_tag))
                binding.tvParamStatus.text = "Safe (Optimal Moisture)"
            }
            grainName.contains("Bajra", ignoreCase = true) -> {
                binding.tvParamBotanical.text = "Pennisetum glaucum"
                binding.badgeConfidence.setCardBackgroundColor(getColor(R.color.bajra_bg))
                binding.tvConfidenceValue.setTextColor(getColor(R.color.bajra_tag))
                binding.tvParamStatus.text = "Safe (Optimal Moisture)"
            }
            grainName.contains("Ragi", ignoreCase = true) -> {
                binding.tvParamBotanical.text = "Eleusine coracana"
                binding.badgeConfidence.setCardBackgroundColor(getColor(R.color.ragi_bg))
                binding.tvConfidenceValue.setTextColor(getColor(R.color.ragi_tag))
                binding.tvParamStatus.text = "Safe (Optimal Moisture)"
            }
            else -> {
                binding.tvParamBotanical.text = "N/A"
                binding.badgeConfidence.setCardBackgroundColor(getColor(R.color.background))
                binding.tvConfidenceValue.setTextColor(getColor(R.color.text_secondary))
                binding.tvParamStatus.text = "N/A"
            }
        }

        setupClickListeners()

        // Automatically save a history record on background thread
        saveClassificationToHistory(grainName, confidenceScore, imageUriString)
    }

    /**
     * Persists the classification record locally in Room Database using background Coroutine.
     */
    private fun saveClassificationToHistory(
        grainName: String,
        confidenceScore: Int,
        imageUriString: String?
    ) {
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = HistoryRepository(database.historyDao())
        val record = ClassificationHistory(
            grainName = grainName,
            confidencePercentage = confidenceScore,
            timestamp = System.currentTimeMillis(),
            imagePath = imageUriString
        )
        lifecycleScope.launch(Dispatchers.IO) {
            repository.insert(record)
        }
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
