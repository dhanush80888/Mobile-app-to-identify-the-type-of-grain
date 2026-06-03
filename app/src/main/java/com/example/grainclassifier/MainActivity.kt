package com.example.grainclassifier

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.grainclassifier.databinding.ActivityMainBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // 0. Permission Launcher
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val cameraGranted = permissions[Manifest.permission.CAMERA] ?: false
        if (cameraGranted) {
            triggerCamera()
        } else {
            Snackbar.make(binding.root, "Camera permission is required to take photos.", Snackbar.LENGTH_LONG).show()
        }
    }

    // 1. Gallery Result Launcher
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { navigateToPrediction(it) }
    }

    // 2. Camera Result Launcher
    private var cameraImageUri: Uri? = null
    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            cameraImageUri?.let { navigateToPrediction(it) }
        } else {
            Snackbar.make(binding.root, "Photo capture cancelled.", Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Setup ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        // Main action buttons
        binding.btnTakePhoto.setOnClickListener {
            triggerCamera()
        }

        binding.btnUploadImage.setOnClickListener {
            triggerGallery()
        }

        binding.btnRealtimeDetection.setOnClickListener {
            val intent = Intent(this, RealTimeDetectionActivity::class.java)
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        binding.btnViewHistory.setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        // Interactive Grain Cards
        binding.cardRice.setOnClickListener {
            showGrainDetails(
                title = getString(R.string.grain_rice),
                description = getString(R.string.desc_rice),
                details = """
                    • Classification standard: Long Grain / Medium / Short Grain
                    • Target Moisture Threshold: < 14.0%
                    • Average Length: 5.5mm - 7.5mm
                    • Quality Parameters Evaluated: Chalkiness index, head rice yield (HRY), kernel whiteness, broken ratio.
                """.trimIndent()
            )
        }

        binding.cardWheat.setOnClickListener {
            showGrainDetails(
                title = getString(R.string.grain_wheat),
                description = getString(R.string.desc_wheat),
                details = """
                    • Classification standard: Hard Red Winter / Soft White / Durum
                    • Target Moisture Threshold: 12.0% - 13.5%
                    • Average Length: 6.0mm - 8.0mm
                    • Quality Parameters Evaluated: Hectoliter weight, gluten index, foreign organic matter, shriveled seed ratio.
                """.trimIndent()
            )
        }

        binding.cardMaize.setOnClickListener {
            showGrainDetails(
                title = getString(R.string.grain_maize),
                description = getString(R.string.desc_maize),
                details = """
                    • Classification standard: Yellow Dent / Sweet Corn / Flint Corn
                    • Target Moisture Threshold: < 15.0%
                    • Average Diameter: 8.0mm - 10.0mm
                    • Quality Parameters Evaluated: Aflatoxin PPM ratio, thermal damage ratio, cracked kernel index, seed density.
                """.trimIndent()
            )
        }

        binding.cardBajra.setOnClickListener {
            showGrainDetails(
                title = getString(R.string.grain_bajra),
                description = getString(R.string.desc_bajra),
                details = """
                    • Classification standard: Pearl Millet Hybrid / Desi Bajra
                    • Target Moisture Threshold: < 12.0%
                    • Average Diameter: 2.0mm - 3.0mm
                    • Quality Parameters Evaluated: Ergot density percentage, immature green kernels, sand/silica content, seed viability.
                """.trimIndent()
            )
        }

        binding.cardRagi.setOnClickListener {
            showGrainDetails(
                title = getString(R.string.grain_ragi),
                description = getString(R.string.desc_ragi),
                details = """
                    • Classification standard: Finger Millet (Organic Brown / Crimson red)
                    • Target Moisture Threshold: < 11.0%
                    • Average Seed Diameter: 1.5mm - 2.0mm
                    • Quality Parameters Evaluated: Calcium concentration grade, husk retention factor, dust admixture, grain density.
                """.trimIndent()
            )
        }
    }

    private fun showGrainDetails(title: String, description: String, details: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle(title)
            .setMessage("$description\n\nQuality Standards:\n$details")
            .setPositiveButton("Dismiss") { dialog, _ ->
                dialog.dismiss()
            }
            .setIcon(R.drawable.ic_grain_logo)
            .show()
    }

    private fun triggerCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(arrayOf(Manifest.permission.CAMERA))
            return
        }
        try {
            // Create a temporary file in our secure shared cache path directory
            val cachePath = File(cacheDir, "grain_images")
            cachePath.mkdirs()
            val file = File(cachePath, "capture_${System.currentTimeMillis()}.jpg")
            
            cameraImageUri = FileProvider.getUriForFile(
                this,
                "com.example.grainclassifier.fileprovider",
                file
            )
            takePictureLauncher.launch(cameraImageUri)
        } catch (e: Exception) {
            e.printStackTrace()
            Snackbar.make(binding.root, "Camera initialization failed: ${e.message}", Snackbar.LENGTH_LONG).show()
        }
    }

    private fun triggerGallery() {
        pickImageLauncher.launch("image/*")
    }

    private fun navigateToPrediction(uri: Uri) {
        val intent = Intent(this, PredictionActivity::class.java).apply {
            putExtra("EXTRA_IMAGE_URI", uri.toString())
        }
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}
