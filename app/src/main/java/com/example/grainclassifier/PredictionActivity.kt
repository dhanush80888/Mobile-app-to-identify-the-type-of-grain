package com.example.grainclassifier

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.example.grainclassifier.ml.GrainClassifier
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import com.example.grainclassifier.databinding.ActivityPredictionBinding
import com.google.android.material.snackbar.Snackbar

class PredictionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPredictionBinding
    private lateinit var classifier: GrainClassifier
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Setup ViewBinding
        binding = ActivityPredictionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize our Clean TFLite Classifier
        classifier = GrainClassifier(this)

        // Extract Uri passed from MainActivity
        val uriString = intent.getStringExtra("EXTRA_IMAGE_URI")
        if (uriString != null) {
            imageUri = Uri.parse(uriString)
            binding.ivSelectedImage.setImageURI(imageUri)
        } else {
            Snackbar.make(binding.root, "Error: Image failed to load.", Snackbar.LENGTH_LONG).show()
            binding.tvStatus.text = "Error: No image source provided."
            binding.btnPredict.isEnabled = false
        }

        setupClickListeners()
    }

    private fun setupClickListeners() {
        // Back navigation
        binding.btnBack.setOnClickListener {
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        // Run AI Inference
        binding.btnPredict.setOnClickListener {
            val uri = imageUri
            if (uri != null) {
                runInferenceFlow(uri)
            } else {
                Snackbar.make(binding.root, "Invalid image context.", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun runInferenceFlow(uri: Uri) {
        // Step 1: Transition UI to "Analyzing" state
        binding.loadingProgress.visibility = View.VISIBLE
        binding.tvStatus.text = "Running TFLite Inference... Preprocessing tensor buffers..."
        binding.btnPredict.isEnabled = false
        binding.btnPredict.alpha = 0.6f
        binding.cardResult.visibility = View.GONE

        // Step 2: Simulate background processor execution (1.5 seconds)
        Handler(Looper.getMainLooper()).postDelayed({
            try {
                // Convert Uri to Bitmap and run clean classifier inference
                val bitmap = classifier.uriToBitmap(uri) ?: throw Exception("Failed to decode bitmap source.")
                val prediction = classifier.classifyImage(bitmap)

                // Transition UI to finished state
                binding.loadingProgress.visibility = View.GONE
                binding.tvStatus.text = "Analysis complete."

                // Launch ResultActivity with dynamic classification metrics
                val baseLabel = prediction.label.substringBefore(" (")
                val intent = Intent(this@PredictionActivity, ResultActivity::class.java).apply {
                    putExtra("EXTRA_IMAGE_URI", uri.toString())
                    putExtra("EXTRA_GRAIN_NAME", baseLabel)
                    putExtra("EXTRA_CONFIDENCE", prediction.confidence.toInt())
                }
                startActivity(intent)
                finish()
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)

            } catch (e: Exception) {
                e.printStackTrace()
                binding.loadingProgress.visibility = View.GONE
                binding.tvStatus.text = "Inference Error: ${e.message}"
            } finally {
                // Re-enable and restyle prediction button
                binding.btnPredict.isEnabled = true
                binding.btnPredict.alpha = 1.0f
                binding.btnPredict.text = "Re-Analyze Quality"
            }
        }, 1500)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}
