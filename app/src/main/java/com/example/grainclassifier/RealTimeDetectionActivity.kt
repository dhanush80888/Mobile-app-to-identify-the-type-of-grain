package com.example.grainclassifier

import android.Manifest
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.example.grainclassifier.databinding.ActivityRealTimeDetectionBinding
import com.example.grainclassifier.ml.GrainClassifier
import com.google.android.material.snackbar.Snackbar
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Premium fullscreen activity for Real-Time Grain Scanner.
 * Binds CameraX Preview and ImageAnalysis lifecycle, processes frames using
 * GrainImageAnalyzer, stabilizes results using majority voting, and adjusts UX dynamically.
 */
class RealTimeDetectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRealTimeDetectionBinding
    private lateinit var classifier: GrainClassifier
    private lateinit var analysisExecutor: ExecutorService

    // Prediction stabilization history queue (stores the last 10 predictions)
    private val predictionHistory = mutableListOf<String>()
    private var currentStableLabel: String? = null

    // Permission launcher
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            startCamera()
        } else {
            binding.tvStatusText.text = "Permission Denied"
            Snackbar.make(
                binding.root,
                "Camera permission is essential for real-time scanning.",
                Snackbar.LENGTH_INDEFINITE
            ).setAction("Retry") {
                checkCameraPermissionsAndStart()
            }.show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivityRealTimeDetectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set initial status state
        binding.tvStatusText.text = "Initializing Model..."
        
        // Start background threading channel
        analysisExecutor = Executors.newSingleThreadExecutor()

        // Safe model loading sequence
        analysisExecutor.execute {
            try {
                classifier = GrainClassifier(this)
                runOnUiThread {
                    startScanningAnimation()
                    checkCameraPermissionsAndStart()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    binding.tvStatusText.text = "Model Init Failed"
                    Snackbar.make(
                        binding.root,
                        "Failed to initialize neural diagnostics: ${e.message}",
                        Snackbar.LENGTH_INDEFINITE
                    ).show()
                }
            }
        }

        binding.btnBack.setOnClickListener {
            onBackPressed()
        }
    }

    /**
     * Start the premium scanning reticle animation inside the viewfinder.
     */
    private fun startScanningAnimation() {
        binding.scanBar.post {
            val deltaY = binding.ivViewfinder.height.toFloat() - binding.scanBar.height.toFloat()
            val animation = ObjectAnimator.ofFloat(
                binding.scanBar,
                "translationY",
                0f,
                deltaY
            ).apply {
                duration = 2000
                repeatMode = ValueAnimator.REVERSE
                repeatCount = ValueAnimator.INFINITE
                interpolator = AccelerateDecelerateInterpolator()
            }
            animation.start()
        }
    }

    /**
     * Verify camera permissions and start the camera stream.
     */
    private fun checkCameraPermissionsAndStart() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    /**
     * Set up CameraX Preview and Frame Image Analyzer.
     */
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview viewport builder configuration
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }

            // Image analysis builder - Keep only latest frames to avoid queuing lag
            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(analysisExecutor, GrainImageAnalyzer(classifier, analysisExecutor) { prediction ->
                        processPrediction(prediction)
                    })
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Clear any bound elements
                cameraProvider.unbindAll()

                // Register live feeds to lifecycle observer
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalysis
                )

                runOnUiThread {
                    binding.tvStatusText.text = "Camera Ready"
                    binding.badgeStatus.setCardBackgroundColor(getColor(R.color.wheat_bg))
                    binding.tvStatusText.setTextColor(getColor(R.color.primary))
                }

            } catch (exc: Exception) {
                exc.printStackTrace()
                runOnUiThread {
                    binding.tvStatusText.text = "Camera Init Failed"
                    Snackbar.make(binding.root, "Camera initialization failed: ${exc.message}", Snackbar.LENGTH_LONG).show()
                }
            }

        }, ContextCompat.getMainExecutor(this))
    }

    /**
     * Stabilizes noisy frame predictions using Majority Voting over a window of 10 samples.
     * Prevents UI flickering, ignores very low confidence predictions (<50%),
     * and averages metrics to ensure high visual quality.
     */
    private fun processPrediction(prediction: GrainClassifier.Prediction) {
        if (prediction.confidence < 50f) return // Requirement 12: Ignore low confidence predictions

        synchronized(predictionHistory) {
            predictionHistory.add(prediction.label)
            if (predictionHistory.size > 10) {
                predictionHistory.removeAt(0)
            }

            // Perform majority voting over historical values
            val voteCounts = predictionHistory.groupingBy { it }.eachCount()
            val majorityPrediction = voteCounts.maxByOrNull { it.value }?.key

            // Check if majority prediction is valid and has changed
            if (majorityPrediction != null && majorityPrediction != currentStableLabel) {
                currentStableLabel = majorityPrediction
                
                // Parse display labels cleanly e.g. "Rice (Oryza sativa)" -> "Rice"
                val displayLabel = majorityPrediction.substringBefore(" (")
                
                runOnUiThread {
                    updateUI(displayLabel, majorityPrediction, prediction.confidence)
                }
            } else if (majorityPrediction != null && majorityPrediction == currentStableLabel) {
                // Stabilized prediction remains unchanged: smoothly update the confidence metrics
                runOnUiThread {
                    binding.tvConfidencePercent.text = "${prediction.confidence.toInt()}%"
                    binding.progressConfidence.progress = prediction.confidence.toInt()
                }
            }
        }
    }

    /**
     * Updates layout details and transitions theme elements dynamically based on stabilized crop class.
     */
    private fun updateUI(displayLabel: String, fullLabel: String, confidence: Float) {
        binding.tvGrainClass.text = fullLabel
        binding.tvConfidencePercent.text = "${confidence.toInt()}%"
        binding.progressConfidence.progress = confidence.toInt()
        
        // Elevate status to scanning
        binding.tvStatusText.text = "Scanning Grain..."

        // Dynamically style UI cards based on detected grain family (matching ResultActivity)
        when {
            displayLabel.contains("Rice", ignoreCase = true) -> {
                binding.badgeStatus.setCardBackgroundColor(getColor(R.color.rice_bg))
                binding.tvStatusText.setTextColor(getColor(R.color.rice_tag))
                binding.tvConfidencePercent.setTextColor(getColor(R.color.rice_tag))
                binding.progressConfidence.progressTintList = android.content.res.ColorStateList.valueOf(getColor(R.color.rice_tag))
            }
            displayLabel.contains("Wheat", ignoreCase = true) -> {
                binding.badgeStatus.setCardBackgroundColor(getColor(R.color.wheat_bg))
                binding.tvStatusText.setTextColor(getColor(R.color.wheat_tag))
                binding.tvConfidencePercent.setTextColor(getColor(R.color.wheat_tag))
                binding.progressConfidence.progressTintList = android.content.res.ColorStateList.valueOf(getColor(R.color.wheat_tag))
            }
            displayLabel.contains("Maize", ignoreCase = true) -> {
                binding.badgeStatus.setCardBackgroundColor(getColor(R.color.maize_bg))
                binding.tvStatusText.setTextColor(getColor(R.color.maize_tag))
                binding.tvConfidencePercent.setTextColor(getColor(R.color.maize_tag))
                binding.progressConfidence.progressTintList = android.content.res.ColorStateList.valueOf(getColor(R.color.maize_tag))
            }
            displayLabel.contains("Bajra", ignoreCase = true) -> {
                binding.badgeStatus.setCardBackgroundColor(getColor(R.color.bajra_bg))
                binding.tvStatusText.setTextColor(getColor(R.color.bajra_tag))
                binding.tvConfidencePercent.setTextColor(getColor(R.color.bajra_tag))
                binding.progressConfidence.progressTintList = android.content.res.ColorStateList.valueOf(getColor(R.color.bajra_tag))
            }
            displayLabel.contains("Ragi", ignoreCase = true) -> {
                binding.badgeStatus.setCardBackgroundColor(getColor(R.color.ragi_bg))
                binding.tvStatusText.setTextColor(getColor(R.color.ragi_tag))
                binding.tvConfidencePercent.setTextColor(getColor(R.color.ragi_tag))
                binding.progressConfidence.progressTintList = android.content.res.ColorStateList.valueOf(getColor(R.color.ragi_tag))
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Release background worker executors cleanly to prevent memory leaks
        analysisExecutor.shutdown()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}
